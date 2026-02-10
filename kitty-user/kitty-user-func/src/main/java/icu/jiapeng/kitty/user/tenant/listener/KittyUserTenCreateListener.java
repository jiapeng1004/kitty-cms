package icu.jiapeng.kitty.user.tenant.listener;


import icu.jiapeng.kitty.user.cfg.entity.KtConfig;
import icu.jiapeng.kitty.user.cfg.service.KtConfigService;
import icu.jiapeng.kitty.user.config.UserConfigProperties;
import icu.jiapeng.kitty.user.db.TenantAwareDataSource;
import icu.jiapeng.kitty.user.scope.TenScoped;
import icu.jiapeng.kitty.user.tenant.vo.TenantVO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 *
 * @author jiapeng
 * @since 2026/2/11
 */
@Service
public class KittyUserTenCreateListener implements TenCreateListener {
    @Resource
    private TenantAwareDataSource tenantAwareDataSource;
    @Resource
    private UserConfigProperties userConfigProperties;
    @Resource
    private KtConfigService ktConfigService;

    @SneakyThrows
    @Override
    public void onTenCreate(TenantVO tenantVO) {
        // 检查租户schema 是否存在不存在则创建
        String tenSchema = tenantAwareDataSource.getTenSchema(tenantVO.getId());
        tenantAwareDataSource.createSchema(tenSchema);
        // 从主schema 迁移表结构到 租户schema
        tenantAwareDataSource.migrateTables(tenSchema, userConfigProperties.getTenMigrateIgnore().toArray(String[]::new));
        // 加入所有配置数据排除 value
        List<KtConfig> allNewConfig = ktConfigService.lambdaQuery().list().stream().peek(ktConfig -> {
            ktConfig.setConfigValue(null);
        }).toList();
        // 保存配置到新租户
        TenScoped.run(tenantVO.getId(), () -> ktConfigService.saveOrUpdateBatch(allNewConfig));
    }
}
