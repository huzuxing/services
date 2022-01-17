package com.future.exception.constant.enums;

import com.future.exception.exception.assertion.ArgumentExceptionAssert;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 12:09
 */

@Getter
@AllArgsConstructor
public enum ArgumentResponseEnum implements ArgumentExceptionAssert {
    ERROR_VALID(1001, "参数校验不正确"),
    ;

    private int code;
    private String message;

    public String getMessage(String msg) {
        StringBuilder builder = new StringBuilder(message);
        builder.append("(").append(msg).append(")");
        return builder.toString();
    }
}
