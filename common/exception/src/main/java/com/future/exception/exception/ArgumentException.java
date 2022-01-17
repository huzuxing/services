package com.future.exception.exception;

import com.future.exception.constant.ResponseEnum;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 15:27
 */
public class ArgumentException extends BaseException{
    private static final long serialVersionUID = 7135493026737708708L;

    public ArgumentException(ResponseEnum baseResponse, Object[] args, String msg) {
        super(baseResponse, args, msg);
    }

    public ArgumentException(ResponseEnum baseResponse, Object[] args, String msg, Throwable cause) {
        super(baseResponse, args, msg, cause);
    }
}
