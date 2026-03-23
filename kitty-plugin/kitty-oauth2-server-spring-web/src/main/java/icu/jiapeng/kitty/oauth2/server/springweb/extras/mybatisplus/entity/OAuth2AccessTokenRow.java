package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@TableName("kt_oauth2_access_token")
public class OAuth2AccessTokenRow {

    /** SHA-256 十六进制（64 字符），与原始 access_token 一一对应。 */
    @TableId("token_hash")
    private String tokenHash;
    private String payloadJson;
    private LocalDateTime expiresAt;
}
