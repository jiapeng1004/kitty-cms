package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.ProbeResult;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import org.springframework.stereotype.Component;

@Component
public class IfStepExecutor implements StepExecutor {

    @Override
    public String getType() {
        return StepExecutorType.IF.getCode();
    }

    @Override
    public String execute(String inputPath, StrategyStepVO step, String stepSuffix, StepContext context) throws Exception {
        if (context == null) throw new IllegalStateException("if 步骤需要 StepContext");
        String condition = step.getCondition();
        if (condition == null || condition.isBlank()) {
            String fallback = step.getStrategyIdWhenFalse() != null ? step.getStrategyIdWhenFalse() : step.getStrategyIdWhenTrue();
            if (fallback == null || fallback.isBlank()) return inputPath;
            return context.runStrategy(fallback, inputPath);
        }
        int inputStepIndex = context.getInputStepIndex();
        ProbeResult probe = inputStepIndex >= 0 ? context.getProbeResult(inputStepIndex) : null;
        if (probe == null) {
            if (step.getStrategyIdWhenFalse() != null)
                return context.runStrategy(step.getStrategyIdWhenFalse(), inputPath);
            return inputPath;
        }
        boolean result = evaluate(condition.trim(), probe);
        String strategyId = result ? step.getStrategyIdWhenTrue() : step.getStrategyIdWhenFalse();
        if (strategyId == null || strategyId.isBlank()) return inputPath;
        return context.runStrategy(strategyId, inputPath);
    }

    private boolean evaluate(String expr, ProbeResult p) {
        if (expr.isEmpty()) return false;
        String op = null;
        int opIdx = -1;
        for (String o : new String[]{">=", "<=", "!=", "==", ">", "<"}) {
            int i = expr.indexOf(o);
            if (i > 0) {
                op = o;
                opIdx = i;
                break;
            }
        }
        if (op == null) return false;
        String field = expr.substring(0, opIdx).trim().toLowerCase();
        String valueStr = expr.substring(opIdx + op.length()).trim();
        double value;
        try {
            value = Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return false;
        }
        double actual = getProbeValue(field, p);
        if (Double.isNaN(actual)) return false;
        return switch (op) {
            case ">" -> actual > value;
            case "<" -> actual < value;
            case ">=" -> actual >= value;
            case "<=" -> actual <= value;
            case "==" -> Math.abs(actual - value) < 1e-9;
            case "!=" -> Math.abs(actual - value) >= 1e-9;
            default -> false;
        };
    }

    private double getProbeValue(String field, ProbeResult p) {
        return switch (field) {
            case "width" -> p.getWidth() != null ? p.getWidth() : Double.NaN;
            case "height" -> p.getHeight() != null ? p.getHeight() : Double.NaN;
            case "bitrate" -> p.getBitrate() != null ? p.getBitrate() : Double.NaN;
            case "durationms" -> p.getDurationMs() != null ? p.getDurationMs() : Double.NaN;
            case "framerate" -> p.getFrameRate() != null ? p.getFrameRate() : Double.NaN;
            default -> Double.NaN;
        };
    }
}
