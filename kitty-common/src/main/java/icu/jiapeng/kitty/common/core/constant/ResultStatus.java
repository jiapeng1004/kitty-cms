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
package icu.jiapeng.kitty.common.core.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 24291
 */
@Getter
@AllArgsConstructor
public enum ResultStatus {

    /**
     * 未知的异常
     */
    NORMAL_ERROR(10001, "operate.failed"),

    /**
     * 警告级错误
     */
    WARNING_ERROR(10002, "operate.warning"),

    /**
     * 用户昵称已经存在了
     */
    USER_NICK_NAME_EXIST(10003, "user.nick.name.exist"),

    /**
     * 手机号存在了
     */
    USER_PHONE_EXIST(10004, "user.phone.exist"),

    /**
     * 密码格式错误
     */
    USER_PWD_FORMAT_ERROR(10005, "user.pwd.format.error"),


    /**
     * 手机号格式错误
     */
    USER_PHONE_FORMAT_ERROR(10006, "user.phone.format.error"),

    /**
     * 邮箱格式错误
     */
    USER_EMAIL_FORMAT_ERROR(10007, "user.email.format.error"),

    /**
     * 邮箱存在了
     */
    USER_EMAIL_EXIST(10008, "user.email.exist"),
    /**
     * 用户名或密码错误
     */
    USER_PWD_NOT_EXIST(10009, "user.pwd.not.exist"),
    /**
     * 验证码错误
     */
    CAPTCHA_ERROR(10010, "captcha.error"),

    /**
     * 角色不存在
     */
    ROLE_NOT_EXIST(10011, "role.not.exist"),


    /**
     * 参数错误
     */
    PARAM_ERROR(10012, "param.error"),
    CONFIG_NOT_EXIST(10013, "config.not.exist"),
    CATALOG_PARENT_FOUND(10014, "catalog.parent.not.exist"),
    CATALOG_NAME_EXISTS(10015, "catalog.name.exists"),

    /**
     * 未配置主存储（分片上传等未显式指定 storageId 时依赖主存储）
     */
    MATERIAL_PRIMARY_STORAGE_NOT_SET(10016, "material.primary.storage.not.set"),

    /**
     * 当前用户对目标栏目无所需权限（含个人栏目非本人、角色在栏目下无该权限位等），非参数非法
     */
    MATERIAL_CATALOG_PERMISSION_DENIED(10017, "material.catalog.permission.denied"),

    /**
     * 目标栏目不存在
     */
    CATALOG_TARGET_NOT_EXIST(10018, "catalog.target.not.exist"),

    /**
     * 栏目移动位置参数非法
     */
    CATALOG_MOVE_POSITION_INVALID(10019, "catalog.move.position.invalid"),


    /**
     * KtMetaFile 文件找不到
     */
    MATERIAL_FILE_NOT_FOUND(10020, "material.file.not.found"),

    ;
    /**
     * 状态码
     */
    private final int state;


    /**
     * 状态信息key
     */
    private final String messageKey;
}
