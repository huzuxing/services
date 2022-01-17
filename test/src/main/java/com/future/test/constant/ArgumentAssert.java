package com.future.test.constant;

import com.future.exception.exception.assertion.ArgumentExceptionAssert;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 21:47
 */
@Getter
@AllArgsConstructor
public enum ArgumentAssert implements ArgumentExceptionAssert {

    ;
    private int code;
    private String message;
}
