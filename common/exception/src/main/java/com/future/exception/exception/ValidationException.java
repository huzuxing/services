package com.future.exception.exception;

import com.future.exception.constant.ResponseEnum;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 15:33
 */
public class ValidationException extends BaseException{
    public ValidationException(ResponseEnum baseResponse, Object[] args, String msg) {
        super(baseResponse, args, msg);
    }

    public ValidationException(ResponseEnum baseResponse, Object[] args, String msg, Throwable cause) {
        super(baseResponse, args, msg, cause);
    }
}
