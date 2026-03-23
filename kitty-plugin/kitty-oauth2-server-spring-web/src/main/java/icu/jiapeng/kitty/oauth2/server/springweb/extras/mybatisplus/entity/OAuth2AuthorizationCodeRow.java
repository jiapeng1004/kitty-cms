package icu.jiapeng.kitty.oauth2.server.springweb.extras.mybatisplus.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 授权码行（一次性使用，{@code consume} 时删除）。
 */
@Setter
@Getter
@TableName("kt_oauth2_authorization_code")
public class OAuth2AuthorizationCodeRow {

    @TableId
    private String code;
    private String payloadJson;
    private LocalDateTime expiresAt;
}
