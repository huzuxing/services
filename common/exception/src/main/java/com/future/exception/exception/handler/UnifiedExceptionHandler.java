package com.future.exception.exception.handler;

import com.future.exception.exception.BaseException;
import com.future.exception.exception.BusinessException;
import com.future.exception.i18n.UnifiedMessageSource;
import com.future.exception.pojo.response.ErrorResponse;
import com.future.exception.constant.enums.ArgumentResponseEnum;
import com.future.exception.constant.enums.CommonResponseEnum;
import com.future.exception.constant.enums.ServletResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.net.BindException;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 16:55
 */

@Slf4j
@ControllerAdvice
public class UnifiedExceptionHandler{

    private final static String ENV_PROD = "prod";

    @Autowired
    private UnifiedMessageSource unifiedMessageSource;

    @Value("${spring.profiles.active}")
    private String profile;

    public String getMessage(BaseException e) {
        String code = "response." + e.getResponseEnum().toString();
        String message = unifiedMessageSource.getMessage(code, e.getArgs());
        if (null == message || message.isEmpty()) {
            return e.getMessage();
        }
        return message;
    }

    /**
     * 业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = BusinessException.class)
    @ResponseBody
    public ErrorResponse handleBusinessException(BaseException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getResponseEnum().getCode(), getMessage(e));
    }

    @ExceptionHandler(value = BaseException.class)
    @ResponseBody
    public ErrorResponse handleBaseException(BaseException e) {
        log.error(e.getMessage(), e);
        return new ErrorResponse(e.getResponseEnum().getCode(), getMessage(e));
    }

    /**
     * 处理servlet 异常，controller
     * @param e
     * @return
     */
    @ExceptionHandler({
            //NoHandlerFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMediaTypeNotAcceptableException.class,
            MissingPathVariableException.class,
            MissingServletRequestParameterException.class,
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            HttpMessageNotWritableException.class,
            // BindException.class,
            // MethodArgumentNotValidException.class
            ServletRequestBindingException.class,
            ConversionNotSupportedException.class,
            MissingServletRequestPartException.class,
            AsyncRequestTimeoutException.class
    })
    @ResponseBody
    public ErrorResponse handleServletException(Exception e) {
        log.error(e.getMessage(), e);
        int code = CommonResponseEnum.SERVER_ERROR.getCode();
        try {
            ServletResponseEnum servletResponseEnum = ServletResponseEnum.valueOf(e.getClass().getSimpleName());
            code = servletResponseEnum.getCode();
        } catch (IllegalArgumentException el) {
            log.error("class [{}] not defined in enum: {}", e.getClass().getName(),
                    ServletResponseEnum.class.getName());
        }
        if (ENV_PROD.equals(profile)) {
            code = CommonResponseEnum.SERVER_ERROR.getCode();
            BaseException be = new BaseException(CommonResponseEnum.SERVER_ERROR);
            return new ErrorResponse(code, getMessage(be));
        }
        return new ErrorResponse(code, e.getMessage());
    }

    /**
     * 参数绑定异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ErrorResponse handleBindException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        return this.wrapBindResult(e.getBindingResult());
    }

    private ErrorResponse wrapBindResult(BindingResult bindingResult) {
        StringBuilder sb = new StringBuilder();
        bindingResult.getAllErrors().forEach(error -> {
            sb.append(", ");
            if (error instanceof FieldError) {
                sb.append(((FieldError) error).getField()).append(": ");
            }
            sb.append(error.getDefaultMessage() == null ? "" : error.getDefaultMessage());
        });
        return new ErrorResponse(ArgumentResponseEnum.ERROR_VALID.getCode(), sb.toString());
    }

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ErrorResponse handleException(Exception e) {
        log.error(e.getMessage(), e);
        if (ENV_PROD.equals(profile)) {
            int code = CommonResponseEnum.SERVER_ERROR.getCode();
            BaseException be = new BaseException(CommonResponseEnum.SERVER_ERROR);
            return new ErrorResponse(code, getMessage(be));
        }
        return new ErrorResponse(CommonResponseEnum.SERVER_ERROR.getCode(), e.getMessage());
    }

}
