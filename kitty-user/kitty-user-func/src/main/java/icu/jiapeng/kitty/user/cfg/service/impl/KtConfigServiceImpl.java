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
import icu.jiapeng.kitty.user.cfg.dto.ConfigQueryPageDTO;
import icu.jiapeng.kitty.user.cfg.dto.GetValDTO;
import icu.jiapeng.kitty.user.cfg.dto.SetValDTO;
import icu.jiapeng.kitty.user.cfg.entity.KtConfig;
import icu.jiapeng.kitty.user.cfg.mapper.KtConfigMapper;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.cfg.vo.ConfigListVo;
import org.springframework.stereotype.Service;

import java.util.Objects;


/**
 * 配置服务
 *
 * @author jiapeng
 * @since 2025/12/20
 */
@Service
public class KtConfigServiceImpl extends ServiceImpl<KtConfigMapper, KtConfig> implements KtConfigService {

    @Override
    public PageRespVo<ConfigListVo> query(ConfigQueryPageDTO query) {
        // 分页查询
        LambdaQueryWrapper<KtConfig> lmWrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getClassId())) {
            lmWrapper.eq(KtConfig::getClassId, query.getClassId());
        }
        if (StrUtil.isNotBlank(query.getConfigKey())) {
            lmWrapper.like(KtConfig::getConfigKey, query.getConfigKey());
        }
        if (StrUtil.isNotBlank(query.getSearchKey())) {
            lmWrapper.like(KtConfig::getConfigName, query.getSearchKey());
        }
        Page<KtConfig> ktConfigPage = new Page<>(query.getPage(), query.getSize());
        if (CollUtil.isNotEmpty(query.getOrders())) {
            for (PageReqDTO.OrderItem order : query.getOrders()) {
                ktConfigPage.addOrder(OrderItem.withExpression(StrUtil.toUnderlineCase(order.getOrderField()), CommonOrder.ASC.equals(order.getOrder())));
            }
        }
        Page<KtConfig> page = page(ktConfigPage, lmWrapper);
        return PageRespVo.<ConfigListVo>builder()
                .page(query.getPage())
                .size(query.getSize())
                .total(page.getTotal())
                .orders(query.getOrders())
                .records(page.getRecords().stream().map(BeansConvert.INSTANCE::config2ListVo).toList())
                .build();
    }

    @Override
    public String getVal(GetValDTO getValDTO) {
        String val = "";
        KtConfig one = lambdaQuery().eq(KtConfig::getConfigKey, getValDTO.getConfigKey()).one();
        if (Objects.nonNull(one)) {
            val = one.getConfigValue();
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
        if (StrUtil.isNotBlank(setValDTO.getConfigName())) {
            one.setConfigName(setValDTO.getConfigName());
        }
        if (StrUtil.isNotBlank(setValDTO.getConfigDesc())) {
            one.setConfigDesc(setValDTO.getConfigDesc());
        }
        if (StrUtil.isNotBlank(setValDTO.getConfigWay())) {
            one.setConfigWay(setValDTO.getConfigWay());
        }
        if (Objects.nonNull(setValDTO.getConfigEnum())) {
            one.setConfigEnum(JSONObject.toJSONString(setValDTO.getConfigEnum()));
        }
        cfgUpdate.setConfigValue(setValDTO.getConfigValue());
        updateById(cfgUpdate);
        return setValDTO.getConfigValue();
    }
}
