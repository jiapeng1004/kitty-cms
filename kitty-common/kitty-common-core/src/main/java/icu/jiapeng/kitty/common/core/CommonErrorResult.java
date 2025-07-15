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
package icu.jiapeng.kitty.common.core;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author 24291
 */
@Getter
@Setter
@Schema(description = "通用异常结果")
public class CommonErrorResult {

    @Schema(description = "状态码")
    private Integer state;


    @Schema(description = "返回信息")
    private String message;


    public static <T> CommonErrorResult error(int state) {
        CommonErrorResult result = new CommonErrorResult();
        result.setState(state);
        return result;
    }

    public static <T> CommonErrorResult error(int state, String message) {
        CommonErrorResult result = new CommonErrorResult();
        result.setState(state);
        result.setMessage(message);
        return result;
    }

    public static <T> CommonErrorResult warning(int state) {
        return error(state);
    }

    public static <T> CommonErrorResult warning(int state, String message) {
        return error(state, message);
    }
}
