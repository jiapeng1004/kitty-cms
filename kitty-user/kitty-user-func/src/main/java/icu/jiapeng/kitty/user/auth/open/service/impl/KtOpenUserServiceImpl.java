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
package icu.jiapeng.kitty.user.auth.open.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import icu.jiapeng.kitty.user.auth.open.entity.KtOpenUser;
import icu.jiapeng.kitty.user.auth.open.mapper.KtOpenUserMapper;
import icu.jiapeng.kitty.user.auth.open.service.KtOpenUserService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * 开放认证用户服务实现：按 source + open_uid 查本地 user_id，并支持绑定与扩展字段。
 */
@Service
public class KtOpenUserServiceImpl extends ServiceImpl<KtOpenUserMapper, KtOpenUser> implements KtOpenUserService {

    @Override
    public Optional<String> findUserIdBySourceAndOpenUid(String source, String openUid) {
        if (StrUtil.isBlank(source) || StrUtil.isBlank(openUid)) {
            return Optional.empty();
        }
        KtOpenUser one = lambdaQuery()
                .eq(KtOpenUser::getSource, source.trim().toLowerCase())
                .eq(KtOpenUser::getOpenUid, openUid)
                .last("LIMIT 1")
                .one();
        return Optional.ofNullable(one).map(KtOpenUser::getUserId);
    }

    @Override
    public boolean bind(String source, String openUid, String userId, String extra) {
        if (StrUtil.isBlank(source) || StrUtil.isBlank(openUid) || StrUtil.isBlank(userId)) {
            return false;
        }
        KtOpenUser entity = new KtOpenUser();
        entity.setId(UUID.randomUUID().toString());
        entity.setSource(source.trim().toLowerCase());
        entity.setOpenUid(openUid);
        entity.setUserId(userId);
        entity.setExtra(extra);
        return save(entity);
    }
}
