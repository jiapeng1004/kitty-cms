package icu.jiapeng.kitty.transcoder.func.strategy;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.kitty.transcoder.api.CreateStrategyRequest;
import icu.jiapeng.kitty.transcoder.api.StrategyStepDTO;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeStrategyStep;
import icu.jiapeng.kitty.transcoder.func.mapper.TranscodeStrategyStepMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StrategyServiceImpl implements StrategyService {

    @Autowired
    private TranscodeStrategyStepMapper stepMapper;

    @Override
    public String createStrategy(CreateStrategyRequest request) {
        List<StrategyStepDTO> stepDtos = request.getSteps();
        if (stepDtos == null || stepDtos.isEmpty()) {
            stepDtos = List.of(defaultStep());
        }
        Long rootId = null;
        for (int i = 0; i < stepDtos.size(); i++) {
            int stepId = i + 1;
            TranscodeStrategyStep row = new TranscodeStrategyStep();
            row.setRootId(rootId != null ? rootId : 0L);
            row.setStrategyName(request.getName());
            if (stepId == 1 && request.getWorkDir() != null) row.setWorkDir(request.getWorkDir());
            row.setStepId(stepId);
            row.setDepends(stepDtos.get(i).getDepends() != null && !stepDtos.get(i).getDepends().isBlank() ? stepDtos.get(i).getDepends().trim() : (stepId == 1 ? "" : String.valueOf(stepId - 1)));
            row.setType(stepDtos.get(i).getType() != null ? stepDtos.get(i).getType() : "transcode");
            row.setTiAnchor("ti_anchor" + stepId);
            row.setParam(toStepParamJson(stepDtos.get(i)));
            row.setCreatedAt(java.time.LocalDateTime.now());
            stepMapper.insert(row);
            if (rootId == null) {
                rootId = row.getId();
                row.setRootId(rootId);
                stepMapper.updateById(row);
            }
        }
        return String.valueOf(rootId);
    }

    @Override
    public StrategyVO getStrategy(String strategyId) {
        Long id = parseId(strategyId);
        if (id == null) return null;
        List<TranscodeStrategyStep> rows = stepMapper.selectList(
                new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, id).orderByAsc(TranscodeStrategyStep::getStepId));
        if (rows.isEmpty()) return null;
        StrategyVO vo = new StrategyVO();
        vo.setId(String.valueOf(id));
        vo.setName(rows.get(0).getStrategyName());
        vo.setWorkDir(rows.get(0).getWorkDir());
        vo.setStepCount(rows.size());
        vo.setSteps(rows.stream().map(this::stepToVO).collect(Collectors.toList()));
        if (rows.get(0).getCreatedAt() != null)
            vo.setCreatedAt(rows.get(0).getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        return vo;
    }

    @Override
    public List<StrategyVO> getStrategies() {
        List<TranscodeStrategyStep> roots = stepMapper.selectList(
                new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getStepId, 1));
        List<StrategyVO> result = new ArrayList<>();
        for (TranscodeStrategyStep root : roots) {
            Long rootId = root.getRootId() != null ? root.getRootId() : root.getId();
            int count = stepMapper.selectCount(new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, rootId)).intValue();
            StrategyVO vo = new StrategyVO();
            vo.setId(String.valueOf(rootId));
            vo.setName(root.getStrategyName());
            vo.setWorkDir(root.getWorkDir());
            vo.setStepCount(count);
            if (root.getCreatedAt() != null)
                vo.setCreatedAt(root.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
            result.add(vo);
        }
        return result;
    }

    @Override
    public boolean updateStrategy(String strategyId, CreateStrategyRequest request) {
        Long id = parseId(strategyId);
        if (id == null) return false;
        stepMapper.delete(new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, id));
        List<StrategyStepDTO> stepDtos = request.getSteps();
        if (stepDtos == null || stepDtos.isEmpty()) stepDtos = List.of(defaultStep());
        for (int i = 0; i < stepDtos.size(); i++) {
            int stepId = i + 1;
            TranscodeStrategyStep row = new TranscodeStrategyStep();
            row.setRootId(id);
            row.setStrategyName(request.getName());
            if (stepId == 1) row.setWorkDir(request.getWorkDir());
            row.setStepId(stepId);
            row.setDepends(stepDtos.get(i).getDepends() != null && !stepDtos.get(i).getDepends().isBlank() ? stepDtos.get(i).getDepends().trim() : (stepId == 1 ? "" : String.valueOf(stepId - 1)));
            row.setType(stepDtos.get(i).getType() != null ? stepDtos.get(i).getType() : "transcode");
            row.setTiAnchor("ti_anchor" + stepId);
            row.setParam(toStepParamJson(stepDtos.get(i)));
            row.setCreatedAt(java.time.LocalDateTime.now());
            stepMapper.insert(row);
        }
        return true;
    }

    @Override
    public boolean deleteStrategy(String strategyId) {
        Long rootId = parseId(strategyId);
        if (rootId == null) return false;
        return stepMapper.delete(new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, rootId)) > 0;
    }

    private static Long parseId(String strategyId) {
        if (strategyId == null || strategyId.isBlank()) return null;
        try {
            return Long.parseLong(strategyId.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static StrategyStepDTO defaultStep() {
        StrategyStepDTO dto = new StrategyStepDTO();
        dto.setType("transcode");
        dto.setTargetFormat("mp4");
        dto.setResolution("1920x1080");
        dto.setBitrate(5000);
        dto.setFrameRate(30);
        dto.setEncoder("h264");
        return dto;
    }

    private String toStepParamJson(StrategyStepDTO dto) {
        return JSONObject.toJSONString(dto);
    }

    private StrategyStepVO stepToVO(TranscodeStrategyStep step) {
        StrategyStepVO vo = new StrategyStepVO();
        vo.setStepId(step.getStepId());
        vo.setType(step.getType() != null ? step.getType() : "transcode");
        vo.setDepends(step.getDepends());
        if (step.getParam() != null && !step.getParam().isEmpty()) {
            StrategyStepVO parsed = JSONObject.parseObject(step.getParam(), StrategyStepVO.class);
            if (parsed != null) {
                vo.setTargetFormat(parsed.getTargetFormat());
                vo.setResolution(parsed.getResolution());
                vo.setBitrate(parsed.getBitrate());
                vo.setFrameRate(parsed.getFrameRate());
                vo.setEncoder(parsed.getEncoder());
                vo.setFrameInterval(parsed.getFrameInterval());
                vo.setExtractFrameCount(parsed.getExtractFrameCount());
                vo.setExtractOutputFormat(parsed.getExtractOutputFormat());
                vo.setSpriteColumns(parsed.getSpriteColumns());
                vo.setSpriteRows(parsed.getSpriteRows());
                vo.setCondition(parsed.getCondition());
                vo.setStrategyIdWhenTrue(parsed.getStrategyIdWhenTrue());
                vo.setStrategyIdWhenFalse(parsed.getStrategyIdWhenFalse());
                vo.setInputTemplate(parsed.getInputTemplate());
                vo.setOutputTemplate(parsed.getOutputTemplate());
            }
        }
        return vo;
    }
}
