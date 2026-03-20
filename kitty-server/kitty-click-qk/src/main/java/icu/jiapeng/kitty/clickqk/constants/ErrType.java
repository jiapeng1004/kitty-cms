package icu.jiapeng.kitty.clickqk.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
@AllArgsConstructor
@Getter
public enum ErrType {

    UN_AUTHENTICATED(401, "unauthenticated"),

    UN_AUTHORIZED(403, "unauthorized"),

    NORMAL_EXCEPTION(500, "normal exception"),

    ;
    private final int errCode;

    private final String errMsg;
}
