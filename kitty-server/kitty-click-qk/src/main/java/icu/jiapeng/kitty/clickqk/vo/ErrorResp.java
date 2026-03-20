package icu.jiapeng.kitty.clickqk.vo;


import icu.jiapeng.kitty.clickqk.constants.ErrType;
import lombok.Getter;
import lombok.Setter;

/**
 *
 *
 * @author jiapeng
 * @since 2026/3/21
 */
@Getter
@Setter
public class ErrorResp {
    private int errCode;
    private String errMsg;

    public static ErrorResp of(int errCode, String errMsg) {
        ErrorResp errorResp = new ErrorResp();
        errorResp.setErrCode(errCode);
        errorResp.setErrMsg(errMsg);
        return errorResp;
    }

    public static ErrorResp failed(String errMsg) {
        return of(ErrType.NORMAL_EXCEPTION.getErrCode(), errMsg);
    }
}
