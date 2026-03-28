package icu.jiapeng.kitty.material.support.web;

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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

/**
 * material 模块全局异常处理。
 */
@ControllerAdvice
@RestController
@Slf4j
public class GlobalExceptionHandler {

    @Resource
    private MessageSource messageSource;

    @ExceptionHandler(BizException.class)
    public ResponseEntity<@NonNull CommonErrorResult> handleBizException(BizException e) {
        log.error("material biz exception:", e);
        ResultStatus resultStatus = e.getResultStatus();
        Locale locale = LocaleContextHolder.getLocale();
        if (resultStatus != null) {
            String message = resultStatus.getMessageKey();
            try {
                message = messageSource.getMessage(resultStatus.getMessageKey(), null, locale);
            } catch (NoSuchMessageException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("message key not found: {}", resultStatus.getMessageKey(), ex);
                }
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonErrorResult.error(resultStatus.getState(), message));
        }
        resultStatus = ResultStatus.NORMAL_ERROR;
        String message = messageSource.getMessage(resultStatus.getMessageKey(), null, locale);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonErrorResult.error(resultStatus.getState(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull CommonErrorResult> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String detail = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse(ResultStatus.PARAM_ERROR.getMessageKey());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonErrorResult.error(ResultStatus.PARAM_ERROR.getState(), detail));
    }

    @ExceptionHandler(NoSuchMessageException.class)
    public ResponseEntity<@NonNull CommonErrorResult> handleNoSuchMessageException(NoSuchMessageException e) {
        ResultStatus resultStatus = ResultStatus.NORMAL_ERROR;
        String message = "错误的消息类型:" + e.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonErrorResult.error(resultStatus.getState(), message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<@NonNull CommonErrorResult> handleException(Exception e) {
        log.error("material exception:", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonErrorResult.error(ResultStatus.NORMAL_ERROR.getState(), e.getMessage()));
    }
}
