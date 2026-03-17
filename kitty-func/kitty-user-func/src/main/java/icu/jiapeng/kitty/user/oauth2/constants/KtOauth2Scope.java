package icu.jiapeng.kitty.user.oauth2.constants;

/**
 * OAuth2 / OpenID Connect scope 常量定义。
 * <p>
 * 这里先维护系统内置的基础 scope，供：
 * 1. `kt_oauth2_scope` 基线数据初始化；
 * 2. OAuth2 客户端管理页下拉选项；
 * 3. 后续服务端做 scope 合法性校验时复用。
 * <p>
 * 约定：
 * 1. 常量值使用协议侧标准字符串；
 * 2. 常量名使用全大写下划线风格，便于在 Java 代码中引用；
 * 3. 如果后续扩展自定义业务 scope，也建议在这里补充说明用途和数据范围。
 */
public interface KtOauth2Scope {
    /**
     * OpenID Connect 身份标识 scope。
     * <p>
     * 该 scope 用于声明“这是一次 OIDC 身份认证请求”，通常会要求授权服务器返回可识别用户身份的
     * `id_token` 或等价身份信息。很多 OIDC 客户端在接入登录态时都会默认申请该 scope。
     */
    String OPENID = "openid";

    /**
     * 用户基础资料 scope。
     * <p>
     * 一般表示客户端希望读取用户的公开基础信息，例如昵称、头像、显示名、性别、语言、时区等。
     * 具体能返回哪些字段，最终取决于授权服务端和上游身份源的实现。
     */
    String PROFILE = "profile";

    /**
     * 用户邮箱信息 scope。
     * <p>
     * 一般用于读取邮箱地址及邮箱是否已验证等信息，适合“邮箱登录”、“邮箱通知”或“账号绑定邮箱”这类场景。
     */
    String EMAIL = "email";

    /**
     * 用户手机号信息 scope。
     * <p>
     * 一般用于读取手机号及其验证状态，适合短信通知、手机号绑定、手机号快捷登录等场景。
     * 该字段通常属于相对敏感信息，后续如果增加校验或审计，应重点关注该 scope。
     */
    String PHONE = "phone";

    /**
     * 用户地址信息 scope。
     * <p>
     * 一般用于读取国家、省市、街道、邮编等地址资料，常用于物流、账单、收货信息或区域化业务场景。
     * 如果上游身份源不支持地址字段，也可以保留该 scope 作为兼容占位。
     */
    String ADDRESS = "address";
}

