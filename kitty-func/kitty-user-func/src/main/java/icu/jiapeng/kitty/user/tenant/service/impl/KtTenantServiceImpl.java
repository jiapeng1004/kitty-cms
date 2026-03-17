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
package icu.jiapeng.kitty.user.tenant.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.scope.TenScoped;
import icu.jiapeng.kitty.user.tenant.dto.TenantQueryPageDTO;
import icu.jiapeng.kitty.user.tenant.listener.TenCreateListener;
import icu.jiapeng.kitty.user.tenant.convert.TenantConvert;
import icu.jiapeng.kitty.user.tenant.dto.TenantCreateDTO;
import icu.jiapeng.kitty.user.tenant.dto.TenantUpdateDTO;
import icu.jiapeng.kitty.user.tenant.entity.KtTenant;
import icu.jiapeng.kitty.user.tenant.mapper.KtTenantMapper;
import icu.jiapeng.kitty.user.tenant.service.KtTenantService;
import icu.jiapeng.kitty.user.tenant.vo.TenantVO;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.OrderComparator;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

/**
 * 租户服务实现类
 */
@Slf4j
@Service
public class KtTenantServiceImpl extends ServiceImpl<KtTenantMapper, KtTenant> implements KtTenantService {

    @Override
    @SneakyThrows
    public List<TenantVO> listAll() {
        // 切到主租户
        Page<KtTenant> page = TenScoped.call(null, (ScopedValue.CallableOp<Page<KtTenant>, Exception>) () -> page(new Page<>(1, 500), new LambdaQueryWrapper<>()));
        return page.getRecords().stream().map(TenantConvert.INSTANCE::entityToVo).toList();
    }

    @Override
    public PageRespVo<TenantVO> query(TenantQueryPageDTO query) {
        LambdaQueryWrapper<KtTenant> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getSearchKey())) {
            wrapper.like(KtTenant::getName, query.getSearchKey());
        }
        Page<KtTenant> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageRespVo.<TenantVO>builder()
                .page(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .orders(query.getOrders())
                .records(page.getRecords().stream().map(TenantConvert.INSTANCE::entityToVo).toList())
                .build();
    }

    @Override
    @Transactional
    public String create(TenantCreateDTO dto) {
        KtTenant tenant = TenantConvert.INSTANCE.createDtoToEntity(dto);
        DuplicateKeyException e = null;
        for (int i = 0; i < 2; i++) {
            try {
                tenant.setId(genTenantId());
                save(tenant);
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        Map<String, TenCreateListener> beansOfType = SpringUtil.getBeansOfType(TenCreateListener.class);
                        if (CollUtil.isNotEmpty(beansOfType)) {
                            List<TenCreateListener> listeners = beansOfType.values().stream().sorted(OrderComparator.INSTANCE).toList();
                            for (TenCreateListener listener : listeners) {
                                listener.onTenCreate(TenantConvert.INSTANCE.entityToVo(tenant));
                            }
                        }
                    }
                });
                return tenant.getId();
            } catch (DuplicateKeyException duplicateKeyException) {
                log.warn("租户创建失败，已重试：{}", tenant.getId());
                e = duplicateKeyException;
            }
        }
        throw new IllegalStateException("租户创建失败", e);
    }

    protected String genTenantId() {
        return RandomUtil.randomStringLower(8);
    }

    @Override
    public TenantVO getById(String id) {
        KtTenant tenant = super.getById(id);
        return TenantConvert.INSTANCE.entityToVo(tenant);
    }

    @Override
    public boolean update(String id, TenantUpdateDTO dto) {
        KtTenant tenant = TenantConvert.INSTANCE.updateDtoToEntity(dto);
        tenant.setId(id);
        return updateById(tenant);
    }

    @Override
    public boolean delete(String id) {
        return removeById(id);
    }
}