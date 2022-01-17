package com.future.exception.exception;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 15:02
 */
public class ExceptionMessageWrap extends RuntimeException{
    public ExceptionMessageWrap(String msg) {
        super(msg);
    }

    public ExceptionMessageWrap(String msg, Throwable cause) {
        super(msg, cause);
    }
}
