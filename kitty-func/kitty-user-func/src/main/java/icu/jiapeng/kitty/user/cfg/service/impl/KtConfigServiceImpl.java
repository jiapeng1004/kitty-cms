/*
 * Copyright [2025] [贾鹏]
 *
 * kitty-cms采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 贾鹏: jiapeng_aoa@163.com。
 * 5.不可二次分发开源参与同类竞品，如有想法可联系 贾鹏: jiapeng_aoa@163.com商议合作。
 */
package icu.jiapeng.kitty.user.cfg.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import icu.jiapeng.kitty.common.core.page.CommonOrder;
import icu.jiapeng.kitty.common.core.page.PageReqDTO;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.cfg.convert.BeansConvert;
import icu.jiapeng.kitty.user.api.cfg.dto.ConfigCreateDTO;
import icu.jiapeng.kitty.user.api.cfg.dto.ConfigQueryPageDTO;
import icu.jiapeng.kitty.user.api.cfg.dto.ConfigUpdateDTO;
import icu.jiapeng.kitty.user.api.cfg.dto.GetValDTO;
import icu.jiapeng.kitty.user.api.cfg.dto.SetValDTO;
import icu.jiapeng.kitty.user.cfg.entity.KtConfig;
import icu.jiapeng.kitty.user.cfg.entity.KtConfigClass;
import icu.jiapeng.kitty.user.cfg.mapper.KtConfigMapper;
import icu.jiapeng.kitty.user.cfg.service.KtConfigClassService;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.api.cfg.vo.ConfigListVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 配置服务
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Service
public class KtConfigServiceImpl extends ServiceImpl<KtConfigMapper, KtConfig> implements KtConfigService {

    @Resource
    private KtConfigClassService ktConfigClassService;

    @Override
    public String create(ConfigCreateDTO dto) {
        KtConfig entity = BeanUtil.copyProperties(dto, KtConfig.class);
        if (Objects.nonNull(dto.getConfigEnum())) {
            entity.setConfigEnum(JSONObject.toJSONString(dto.getConfigEnum()));
        }
        save(entity);
        return entity.getId();
    }

    @Override
    public boolean update(String id, ConfigUpdateDTO dto) {
        KtConfig one = getById(id);
        if (Objects.isNull(one)) {
            return false;
        }
        if (StrUtil.isNotBlank(dto.getConfigName())) {
            one.setConfigName(dto.getConfigName());
        }
        if (StrUtil.isNotBlank(dto.getConfigDesc())) {
            one.setConfigDesc(dto.getConfigDesc());
        }
        if (StrUtil.isNotBlank(dto.getConfigWay())) {
            one.setConfigWay(dto.getConfigWay());
        }
        if (dto.getClassId() != null) {
            one.setClassId(dto.getClassId());
        }
        one.setConfigDefault(dto.getConfigDefault());
        one.setConfigValue(dto.getConfigValue());
        if (Objects.nonNull(dto.getConfigEnum())) {
            one.setConfigEnum(JSONObject.toJSONString(dto.getConfigEnum()));
        }
        return updateById(one);
    }

    @Override
    public PageRespVo<ConfigListVo> query(ConfigQueryPageDTO query) {
        // 分页查询
        LambdaQueryWrapper<KtConfig> lmWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getClassId())) {
            lmWrapper.eq(KtConfig::getClassId, query.getClassId());
        }
        if (StrUtil.isNotBlank(query.getConfigKey())) {
            lmWrapper.likeRight(KtConfig::getConfigKey, query.getConfigKey());
        }
        if (StrUtil.isNotBlank(query.getSearchKey())) {
            lmWrapper.nested(lmWrapper1 -> lmWrapper1
                    .like(KtConfig::getConfigName, query.getSearchKey())
                    .or()
                    .likeRight(KtConfig::getConfigKey, query.getSearchKey())
            );
        }
        Page<KtConfig> ktConfigPage = new Page<>(query.getPage(), query.getSize());
        if (CollUtil.isNotEmpty(query.getOrders())) {
            for (PageReqDTO.OrderItem order : query.getOrders()) {
                ktConfigPage.addOrder(OrderItem.withExpression(StrUtil.toUnderlineCase(order.getOrderField()), CommonOrder.ASC.equals(order.getOrder())));
            }
        }
        Page<KtConfig> configPage = page(ktConfigPage, lmWrapper);
        List<ConfigListVo> records = configPage.getRecords().stream().map(BeansConvert.INSTANCE::config2ListVo).toList();
        fillClassName(records);
        return PageRespVo.<ConfigListVo>builder()
                .page(query.getPage())
                .size(query.getSize())
                .total(configPage.getTotal())
                .orders(query.getOrders())
                .records(records)
                .build();
    }


    /**
     * 批量解析 classId 为分类名，写入每条记录的 className
     */
    private void fillClassName(List<ConfigListVo> records) {
        if (CollUtil.isEmpty(records)) {
            return;
        }
        List<String> classIds = records.stream()
                .map(ConfigListVo::getClassId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .toList();
        if (classIds.isEmpty()) {
            return;
        }
        Map<String, String> idToName = ktConfigClassService.listByIds(classIds).stream()
                .collect(Collectors.toMap(KtConfigClass::getId, KtConfigClass::getClassName, (a, _) -> a));
        for (ConfigListVo vo : records) {
            if (StrUtil.isNotBlank(vo.getClassId())) {
                vo.setClassName(idToName.get(vo.getClassId()));
            }
        }
    }

    @Override
    public String getVal(GetValDTO getValDTO) {
        String val = "";
        KtConfig one = lambdaQuery().eq(KtConfig::getConfigKey, getValDTO.getConfigKey()).one();
        if (Objects.nonNull(one)) {
            val = StrUtil.firstNonBlank(one.getConfigValue(), one.getConfigDefault());
        }
        return val;
    }

    @Override
    public String setVal(SetValDTO setValDTO) {
        KtConfig one;
        if (StrUtil.isNotBlank(setValDTO.getId())) {
            // 传了id
            one = lambdaQuery().eq(KtConfig::getId, setValDTO.getId()).one();
        } else if (StrUtil.isNotBlank(setValDTO.getConfigKey())) {
            // 传了配置key
            one = lambdaQuery().eq(KtConfig::getConfigKey, setValDTO.getConfigKey()).one();
        } else {
            throw BizException.of(ResultStatus.PARAM_ERROR);
        }
        if (Objects.isNull(one)) {
            throw BizException.of(ResultStatus.CONFIG_NOT_EXIST);
        }
        KtConfig cfgUpdate = new KtConfig();
        cfgUpdate.setId(one.getId());
        one.setConfigValue(setValDTO.getConfigValue());
        cfgUpdate.setConfigValue(setValDTO.getConfigValue());
        updateById(cfgUpdate);
        return setValDTO.getConfigValue();
    }
}
