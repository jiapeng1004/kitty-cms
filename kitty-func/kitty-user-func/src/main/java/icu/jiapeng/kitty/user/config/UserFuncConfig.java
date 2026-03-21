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
package icu.jiapeng.kitty.user.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import icu.jiapeng.kitty.common.core.baomidou.KittyMetaDataHandler;
import icu.jiapeng.kitty.common.core.config.GrpcConfig;
import icu.jiapeng.kitty.user.db.DefaultTenantDataSource;
import icu.jiapeng.kitty.user.db.KtTenSchemaInterceptor;
import icu.jiapeng.kitty.user.db.TenantAwareDataSource;
import icu.jiapeng.kitty.user.filter.TenFilter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(UserConfigProperties.class)
@Import({GrpcConfig.class
        , SaConfig.class
        , KittyMetaDataHandler.class
        , DefaultTenantDataSource.class
})
@MapperScan("icu.jiapeng.kitty.user.**.mapper")
public class UserFuncConfig {
    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(TenantAwareDataSource tenantAwareDataSource) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new KtTenSchemaInterceptor(tenantAwareDataSource));
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        // 如果配置多个插件, 切记分页最后添加
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    @Bean
    public FilterRegistrationBean<TenFilter> loggingFilter() {
        FilterRegistrationBean<TenFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TenFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}