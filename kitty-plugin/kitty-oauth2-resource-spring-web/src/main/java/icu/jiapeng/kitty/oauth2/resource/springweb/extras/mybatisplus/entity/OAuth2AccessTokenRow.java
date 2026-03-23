package icu.jiapeng.kitty.oauth2.resource.springweb.extras.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 与授权服务器 {@code kt_oauth2_access_token} 表结构约定一致（资源侧仅 SELECT）。
 */
@Setter
@Getter
@TableName("kt_oauth2_access_token")
public class OAuth2AccessTokenRow {

    @TableId("token_hash")
    private String tokenHash;
    private String payloadJson;
    private LocalDateTime expiresAt;
}
