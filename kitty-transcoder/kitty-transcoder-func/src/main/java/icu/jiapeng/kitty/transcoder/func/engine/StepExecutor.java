package icu.jiapeng.kitty.transcoder.func.engine;

import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 原子步骤执行器：单一输入、单一输出，由上层编排输入输出与依赖。
 * 通过 {@link #getType()} 与策略步骤的 type 对应，编排层按 step.type 选择执行器。
 */
public interface StepExecutor extends InitializingBean {


    /**
     * 类型字符串
     */
    String getType();

    @Override
    default void afterPropertiesSet() {
        Factory.register(this);
    }

    /**
     * 执行单一步骤。
     *
     * @param inputPath 输入路径（文件或目录，可由输入模板解析得到）
     * @param step      步骤参数
     * @return 本步骤输出路径（文件或目录）
     */
    String execute(@NotBlank String inputPath, String outputPath, StrategyStepVO step) throws Exception;

    /**
     *
     *
     * @author jiapeng
     * @since 2026/3/1
     */
    @Slf4j
    class Factory {
        private static final ConcurrentMap<String, StepExecutor> executors = new ConcurrentHashMap<>() {
        };

        /**
         * 执行器注册
         */
        public static void register(StepExecutor executor) {
            register(executor.getType(), executor);
        }

        /**
         * 执行器注册
         */
        public static void register(String type, StepExecutor executor) {
            StepExecutor old = executors.put(type, executor);
            if (old != null) {
                log.warn("已存在类型为 {} 的执行器，已替换{}==>{}", executor.getType(), old.getClass().getName(), executor.getClass().getName());
            }
        }

        /**
         * 解析执行器不报异常
         *
         * @param type 类型
         * @return 执行器
         */
        public static StepExecutor resolve(String type) {
            return executors.get(type);
        }

        /**
         * 解析执行器,不存在报异常
         *
         * @param type 类型
         * @return 执行器
         */
        public static StepExecutor resolveOrFail(String type) {
            StepExecutor executor = resolve(type);
            if (executor == null) {
                throw new IllegalArgumentException("未注册 " + type + " 执行器");
            }
            return executor;
        }
    }
}
