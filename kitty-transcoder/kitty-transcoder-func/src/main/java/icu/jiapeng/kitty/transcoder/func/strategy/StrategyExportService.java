package icu.jiapeng.kitty.transcoder.func.strategy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import icu.jiapeng.kitty.transcoder.api.CreateStrategyRequest;
import icu.jiapeng.kitty.transcoder.api.StrategyStepDTO;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 策略导出/导入服务。使用 YAML 语义化格式，与数据库结构解耦。
 * 支持 format_version，未知字段忽略，缺失字段使用默认值。
 * 导出含 strategy_id，导入时保持 ID 不变（存在则更新，不存在则创建后设置）。
 */
@Service
public class StrategyExportService {

    @Resource
    private StrategyService strategyService;

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(
            YAMLFactory.builder()
                    .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                    .enable(YAMLGenerator.Feature.INDENT_ARRAYS)
                    .build()
    ).configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 导出策略为 YAML 字符串（语义化、可读性好）
     */
    public String exportToYaml(StrategyVO strategy) {
        try {
            StrategyExportFormat fmt = toExportFormat(strategy);
            return YAML_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(fmt);
        } catch (Exception e) {
            throw new IllegalArgumentException("导出失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从 YAML 字符串导入。若含 strategy_id：存在则更新，不存在则创建后设置 ID。
     * 返回最终策略 ID。
     */
    public String importFromString(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("导入内容为空");
        }
        try {
            StrategyExportFormat fmt = YAML_MAPPER.readValue(content, StrategyExportFormat.class);
            CreateStrategyRequest req = toCreateRequest(fmt);
            String desiredIdStr = fmt.getStrategyId() != null && !fmt.getStrategyId().isBlank() ? fmt.getStrategyId().trim() : null;
            if (desiredIdStr != null) {
                Long desiredId = Long.parseLong(desiredIdStr);
                StrategyVO existing = strategyService.getStrategy(desiredId);
                if (existing != null) {
                    strategyService.updateStrategy(desiredId, req);
                    return String.valueOf(desiredId);
                }
                Long newId = strategyService.createStrategy(req);
                strategyService.updateStrategyId(newId, desiredId);
                return String.valueOf(desiredId);
            }
            return String.valueOf(strategyService.createStrategy(req));
        } catch (Exception e) {
            throw new IllegalArgumentException("导入解析失败: " + e.getMessage(), e);
        }
    }

    private StrategyExportFormat toExportFormat(StrategyVO vo) {
        StrategyExportFormat fmt = new StrategyExportFormat();
        fmt.setFormatVersion(1);
        fmt.setStrategyId(vo.getId() != null ? String.valueOf(vo.getId()) : null);
        fmt.setName(vo.getName());
        fmt.setWorkDir(vo.getWorkDir());
        if (vo.getSteps() != null && !vo.getSteps().isEmpty()) {
            List<StrategyExportFormat.StrategyStepExport> steps = new ArrayList<>();
            for (StrategyStepVO s : vo.getSteps()) {
                steps.add(toStepExport(s));
            }
            fmt.setSteps(steps);
        }
        return fmt;
    }

    private StrategyExportFormat.StrategyStepExport toStepExport(StrategyStepVO s) {
        StrategyExportFormat.StrategyStepExport e = new StrategyExportFormat.StrategyStepExport();
        e.setStepId(s.getStepId());
        e.setType(s.getType());
        e.setDepends(s.getDepends());
        e.setInputTemplate(s.getInputTemplate());
        e.setOutputTemplate(s.getOutputTemplate());
        e.setTargetFormat(s.getTargetFormat());
        e.setResolution(s.getResolution());
        e.setBitrate(s.getBitrate());
        e.setFrameRate(s.getFrameRate());
        e.setEncoder(s.getEncoder());
        e.setFrameInterval(s.getFrameInterval());
        e.setExtractFrameCount(s.getExtractFrameCount());
        e.setExtractOutputFormat(s.getExtractOutputFormat());
        e.setSpriteColumns(s.getSpriteColumns());
        e.setSpriteRows(s.getSpriteRows());
        e.setSpriteScale(s.getSpriteScale());
        e.setImageTargetFormat(s.getImageTargetFormat());
        e.setImageQuality(s.getImageQuality());
        e.setImageResize(s.getImageResize());
        e.setCondition(s.getCondition());
        e.setStrategyIdWhenTrue(s.getStrategyIdWhenTrue());
        e.setStrategyIdWhenFalse(s.getStrategyIdWhenFalse());
        return e;
    }

    private CreateStrategyRequest toCreateRequest(StrategyExportFormat fmt) {
        CreateStrategyRequest req = new CreateStrategyRequest();
        req.setName(fmt.getName() != null ? fmt.getName().trim() : "");
        req.setWorkDir(fmt.getWorkDir() != null && !fmt.getWorkDir().isBlank() ? fmt.getWorkDir().trim() : null);
        if (fmt.getSteps() != null && !fmt.getSteps().isEmpty()) {
            List<StrategyStepDTO> dtos = new ArrayList<>();
            for (StrategyExportFormat.StrategyStepExport e : fmt.getSteps()) {
                dtos.add(toStepDto(e));
            }
            req.setSteps(dtos);
        }
        return req;
    }

    private StrategyStepDTO toStepDto(StrategyExportFormat.StrategyStepExport e) {
        StrategyStepDTO dto = new StrategyStepDTO();
        dto.setType(e.getType() != null ? e.getType().trim() : "transcode");
        dto.setDepends(e.getDepends() != null && !e.getDepends().isBlank() ? e.getDepends().trim() : null);
        dto.setInputTemplate(e.getInputTemplate() != null && !e.getInputTemplate().isBlank() ? e.getInputTemplate().trim() : null);
        dto.setOutputTemplate(e.getOutputTemplate() != null && !e.getOutputTemplate().isBlank() ? e.getOutputTemplate().trim() : null);
        dto.setTargetFormat(e.getTargetFormat());
        dto.setResolution(e.getResolution());
        dto.setBitrate(e.getBitrate());
        dto.setFrameRate(e.getFrameRate());
        dto.setEncoder(e.getEncoder());
        dto.setFrameInterval(e.getFrameInterval());
        dto.setExtractFrameCount(e.getExtractFrameCount());
        dto.setExtractOutputFormat(e.getExtractOutputFormat());
        dto.setSpriteColumns(e.getSpriteColumns());
        dto.setSpriteRows(e.getSpriteRows());
        dto.setSpriteScale(e.getSpriteScale());
        dto.setImageTargetFormat(e.getImageTargetFormat());
        dto.setImageQuality(e.getImageQuality());
        dto.setImageResize(e.getImageResize());
        dto.setCondition(e.getCondition());
        dto.setStrategyIdWhenTrue(e.getStrategyIdWhenTrue());
        dto.setStrategyIdWhenFalse(e.getStrategyIdWhenFalse());
        return dto;
    }
}
