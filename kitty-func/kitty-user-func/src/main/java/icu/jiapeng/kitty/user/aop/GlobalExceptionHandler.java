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
package icu.jiapeng.kitty.user.aop;

import icu.jiapeng.kitty.common.core.CommonErrorResult;
import icu.jiapeng.kitty.common.core.constant.ResultStatus;
import icu.jiapeng.kitty.common.core.exceptions.BizException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * 全局异常处理
 *
 * @author 24291
 */
@ControllerAdvice
@RestController
@Slf4j
public class GlobalExceptionHandler {

    @Resource
    private MessageSource messageSource;

    /**
     * 处理Exception
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<@NonNull CommonErrorResult> handleException(BizException e) {
        log.error("请求异常:", e);
        // 如果存在状态码
        ResultStatus resultStatus = e.getResultStatus();
        Locale locale = LocaleContextHolder.getLocale();
        if (resultStatus != null) {
            String message = resultStatus.getMessageKey();
            try {
                message = messageSource.getMessage(resultStatus.getMessageKey(), null, locale);
            } catch (NoSuchMessageException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("NoSuchMessageException:", ex);
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonErrorResult.error(resultStatus.getState(), message));
        }
        resultStatus = ResultStatus.NORMAL_ERROR;
        String message = messageSource.getMessage(resultStatus.getMessageKey(), null, locale);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonErrorResult.error(resultStatus.getState(), message));
    }

    /**
     * 处理Exception
     */
    @ExceptionHandler(NoSuchMessageException.class)
    public ResponseEntity<@NonNull CommonErrorResult> handleException(NoSuchMessageException e) {
        // 如果存在状态码
        ResultStatus resultStatus = ResultStatus.NORMAL_ERROR;
        String message = "错误的消息类型:" + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonErrorResult.error(resultStatus.getState(), message));
    }

    /**
     * 处理Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull CommonErrorResult> handleException(Exception e) {
        log.error("Exception:", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CommonErrorResult.error(ResultStatus.NORMAL_ERROR.getState(), e.getMessage()));
    }
}
