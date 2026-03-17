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
package icu.jiapeng.kitty.user.auth.open.service;

import com.baomidou.mybatisplus.extension.service.IService;
import icu.jiapeng.kitty.user.auth.open.entity.KtOpenUser;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * 开放认证用户：上游 SSO 与本地用户映射的增查。
 */
public interface KtOpenUserService extends IService<@NonNull KtOpenUser> {

    /**
     * 根据平台与上游用户唯一标识查询本地 user_id。
     *
     * @param source  平台标识，如 feishu、github
     * @param openUid 上游 SSO 用户唯一 id
     * @return 本地用户 id，不存在则 empty
     */
    Optional<String> findUserIdBySourceAndOpenUid(String source, String openUid);

    /**
     * 绑定上游用户与本地用户（若已存在则不再插入，由唯一约束保证）。
     *
     * @param source  平台标识
     * @param openUid 上游用户唯一 id
     * @param userId  本地用户 id
     * @param extra   可选扩展 JSON，可为 null
     * @return 是否保存成功
     */
    boolean bind(String source, String openUid, String userId, String extra);
}
