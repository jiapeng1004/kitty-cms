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
package icu.jiapeng.kitty.user.cfg.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.common.core.page.PageRespVo;
import icu.jiapeng.kitty.user.api.cfg.dto.ConfigCreateDTO;
import icu.jiapeng.kitty.user.api.cfg.dto.ConfigQueryPageDTO;
import icu.jiapeng.kitty.user.api.cfg.dto.ConfigUpdateDTO;
import icu.jiapeng.kitty.user.api.cfg.vo.ConfigListVo;
import icu.jiapeng.kitty.user.cfg.TenantConfigEnum;
import icu.jiapeng.kitty.user.api.cfg.dto.GetValDTO;
import icu.jiapeng.kitty.user.api.cfg.dto.SetValDTO;
import icu.jiapeng.kitty.user.cfg.entity.KtConfig;
import jakarta.validation.Valid;

/**
 * 配置服务
 *
 * @author jiapeng
 * @since 2025/12/20
 */
public interface KtConfigService extends IService<KtConfig> {
    /**
     * 查询
     *
     * @param query 查询参数
     * @return 结果
     */
    PageRespVo<ConfigListVo> query(ConfigQueryPageDTO query);

    /**
     * 新增配置项
     *
     * @param dto 创建参数
     * @return 配置项 id
     */
    String create(ConfigCreateDTO dto);

    /**
     * 更新配置项
     *
     * @param id  配置项 id
     * @param dto 更新参数
     * @return 是否成功
     */
    boolean update(String id, ConfigUpdateDTO dto);

    /**
     * 获取值
     *
     * @param getValDTO 获取值参数
     * @return 值
     */
    String getVal(GetValDTO getValDTO);

    /**
     * 按配置枚举获取值，推荐使用此方法以统一维护配置 key。
     * 查不到配置时返回枚举维护的默认值（不依赖 configService/库表）。
     *
     * @param configKey 配置项枚举
     * @return 值，未配置时为枚举的 defaultVal
     */
    default String getVal(TenantConfigEnum configKey) {
        String v = getVal(new GetValDTO().setConfigKey(configKey.getKey()));
        return (v == null || v.isBlank()) ? configKey.getDefaultVal() : v;
    }

    /**
     * 设置值
     *
     * @param setValDTO 设置值参数
     * @return 新值
     */
    String setVal(@Valid SetValDTO setValDTO);
}
