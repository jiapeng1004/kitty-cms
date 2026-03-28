package icu.jiapeng.kitty.material.metadata.service;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.metadata.dto.MaterialMetadataFieldUpsertDTO;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataField;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFieldVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 编目字段定义（元数据字典项）的增删改查；与模板、实例通过 ID 关联。
 */
@Service
@RequiredArgsConstructor
public class MaterialMetadataFieldService {

    private final MetadataFieldService metadataFieldService;

    public MaterialMetadataFieldVO create(MaterialMetadataFieldUpsertDTO req) {
        validateUpsert(req, false);
        KtMetadataField f = new KtMetadataField();
        f.setId(UUID.randomUUID().toString());
        applyUpsert(f, req);
        metadataFieldService.saveField(f);
        return toVo(f);
    }

    public MaterialMetadataFieldVO update(MaterialMetadataFieldUpsertDTO req) {
        validateUpsert(req, true);
        KtMetadataField existing = metadataFieldService.findById(req.getId())
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        applyUpsert(existing, req);
        metadataFieldService.saveField(existing);
        return toVo(existing);
    }

    public void delete(String id) {
        if (isBlank(id)) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        KtMetadataField field = metadataFieldService.findById(id)
                .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
        metadataFieldService.delete(field);
    }

    public List<MaterialMetadataFieldVO> listAll() {
        return metadataFieldService.findAll()
                .stream()
                .map(this::toVo)
                .toList();
    }

    private void validateUpsert(MaterialMetadataFieldUpsertDTO req, boolean update) {
        if (req == null || isBlank(req.getFieldCode()) || isBlank(req.getFieldName()) || isBlank(req.getInputType())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (update && isBlank(req.getId())) {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
    }

    private void applyUpsert(KtMetadataField f, MaterialMetadataFieldUpsertDTO req) {
        f.setFieldCode(req.getFieldCode());
        f.setFieldName(req.getFieldName());
        f.setInputType(req.getInputType());
        f.setRequired(req.getRequired() == null ? 0 : req.getRequired());
        f.setOptionsJson(req.getOptionsJson());
    }

    private MaterialMetadataFieldVO toVo(KtMetadataField f) {
        MaterialMetadataFieldVO vo = new MaterialMetadataFieldVO();
        vo.setId(f.getId());
        vo.setFieldCode(f.getFieldCode());
        vo.setFieldName(f.getFieldName());
        vo.setInputType(f.getInputType());
        vo.setRequired(f.getRequired());
        vo.setOptionsJson(f.getOptionsJson());
        return vo;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
