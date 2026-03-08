package icu.jiapeng.kitty.transcoder.func.strategy;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import icu.jiapeng.kitty.transcoder.api.CreateStrategyRequest;
import icu.jiapeng.kitty.transcoder.api.StrategyStepDTO;
import icu.jiapeng.kitty.transcoder.api.StrategyStepVO;
import icu.jiapeng.kitty.transcoder.api.StrategyVO;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeStrategyStep;
import icu.jiapeng.kitty.transcoder.func.entity.TranscodeTask;
import icu.jiapeng.kitty.transcoder.func.mapper.TranscodeStrategyStepMapper;
import icu.jiapeng.kitty.transcoder.func.mapper.TranscodeTaskMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StrategyServiceImpl implements StrategyService {

    @Resource
    private TranscodeStrategyStepMapper stepMapper;

    @Resource
    private TranscodeTaskMapper taskMapper;

    @Override
    public Long createStrategy(CreateStrategyRequest request) {
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
        return rootId;
    }

    @Override
    public StrategyVO getStrategy(Long strategyId) {
        if (strategyId == null) return null;
        List<TranscodeStrategyStep> rows = stepMapper.selectList(
                new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, strategyId).orderByAsc(TranscodeStrategyStep::getStepId));
        if (rows.isEmpty()) return null;
        StrategyVO vo = new StrategyVO();
        vo.setId(strategyId);
        vo.setName(rows.getFirst().getStrategyName());
        vo.setWorkDir(rows.getFirst().getWorkDir());
        vo.setStepCount(rows.size());
        vo.setSteps(rows.stream().map(this::stepToVO).collect(Collectors.toList()));
        if (rows.getFirst().getCreatedAt() != null)
            vo.setCreatedAt(rows.getFirst().getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
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
            vo.setId(rootId);
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
    public boolean updateStrategy(Long strategyId, CreateStrategyRequest request) {
        if (strategyId == null) return false;
        stepMapper.delete(new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, strategyId));
        List<StrategyStepDTO> stepDtos = request.getSteps();
        if (stepDtos == null || stepDtos.isEmpty()) stepDtos = List.of(defaultStep());
        for (int i = 0; i < stepDtos.size(); i++) {
            int stepId = i + 1;
            TranscodeStrategyStep row = new TranscodeStrategyStep();
            row.setRootId(strategyId);
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
    public boolean deleteStrategy(Long strategyId) {
        if (strategyId == null) return false;
        return stepMapper.delete(new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, strategyId)) > 0;
    }

    @Override
    public void updateStrategyId(Long oldId, Long newId) {
        if (oldId == null) throw new IllegalArgumentException("策略ID无效");
        if (newId == null) throw new IllegalArgumentException("新策略ID无效");
        if (oldId.equals(newId)) return;
        long conflict = stepMapper.selectCount(
                new LambdaQueryWrapper<TranscodeStrategyStep>().eq(TranscodeStrategyStep::getRootId, newId));
        if (conflict > 0) throw new IllegalArgumentException("策略ID重复");
        stepMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TranscodeStrategyStep>()
                .eq(TranscodeStrategyStep::getRootId, oldId)
                .set(TranscodeStrategyStep::getRootId, newId));
        taskMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<TranscodeTask>()
                .eq(TranscodeTask::getStrategyId, oldId)
                .set(TranscodeTask::getStrategyId, newId));
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
                vo.setImageTargetFormat(parsed.getImageTargetFormat());
                vo.setImageQuality(parsed.getImageQuality());
                vo.setImageResize(parsed.getImageResize());
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
