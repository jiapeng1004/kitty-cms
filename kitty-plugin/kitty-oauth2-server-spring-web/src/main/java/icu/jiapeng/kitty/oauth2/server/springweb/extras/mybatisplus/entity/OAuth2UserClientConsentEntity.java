package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户在某 OAuth2 客户端下已同意的 scope 合并串（空格分隔，与 RFC 6749 常见形式一致）。
 *
 * @see icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.MybatisPlusOAuth2ConsentStorageAdapter
 */
@Setter
@Getter
@TableName("kt_oauth2_user_client_consent")
public class OAuth2UserClientConsentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String userId;
    private String clientId;
    /**
     * 已授权 scope 合并串（空格分隔）。
     */
    private String scopes;

}
