package icu.jiapeng.kitty.material.metadata.service;

import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataField;
import icu.jiapeng.kitty.material.metadata.entity.KtMetadataTemplateFieldBinding;
import icu.jiapeng.kitty.material.metadata.service.MetadataFieldService;
import icu.jiapeng.kitty.material.metadata.service.MetadataTemplateFieldBindingService;
import icu.jiapeng.kitty.material.metadata.vo.MaterialMetadataFormFieldVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MetadataFormFieldAssembler {

    private final MetadataTemplateFieldBindingService metadataTemplateFieldBindingService;
    private final MetadataFieldService metadataFieldService;

    public List<MaterialMetadataFormFieldVO> orderedFormFields(String templateId) {
        List<KtMetadataTemplateFieldBinding> bindings = metadataTemplateFieldBindingService.listByTemplateIdOrderBySort(templateId);
        List<MaterialMetadataFormFieldVO> out = new ArrayList<>();
        for (KtMetadataTemplateFieldBinding bind : bindings) {
            KtMetadataField field = metadataFieldService.findById(bind.getFieldId())
                    .orElseThrow(() -> BizException.of(ResultStatus.PARAM_ERROR));
            MaterialMetadataFormFieldVO vo = new MaterialMetadataFormFieldVO();
            vo.setFieldId(field.getId());
            vo.setFieldCode(field.getFieldCode());
            vo.setFieldName(field.getFieldName());
            vo.setInputType(field.getInputType());
            vo.setRequired(field.getRequired());
            vo.setOptionsJson(field.getOptionsJson());
            vo.setSortNum(bind.getSortNum());
            out.add(vo);
        }
        return out;
    }
}
