package com.future.exception.constant.enums;

import com.future.exception.exception.BaseException;
import com.future.exception.exception.assertion.CommonExceptionAssert;
import com.future.exception.pojo.response.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 14:05
 */

@Getter
@AllArgsConstructor
public enum CommonResponseEnum implements CommonExceptionAssert {
    SUCCESS(0, "success"),
    SERVER_BUSY(1000, "服务器繁忙"),
    SERVER_ERROR(1001, "服务器异常"),
    ;

    private int code;
    private String message;

    public String getMessage(String msg) {
        StringBuilder builder = new StringBuilder(message);
        builder.append("(").append(msg).append(")");
        return builder.toString();
    }

    public static void assertSuccess(BaseResponse b) {
        SERVER_ERROR.assertNotNull(b);
        var code = b.getCode();
        if (code != CommonResponseEnum.SUCCESS.getCode()) {
            String msg = b.getMessage();
            throw new BaseException(code, msg);
        }
    }
}
