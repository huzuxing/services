package com.future.exception.exception;

import com.future.exception.constant.ResponseEnum;
import lombok.Getter;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 11:46
 */

@Getter
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = -4393689853927264821L;

    /**
     * 返回码
     */
    protected ResponseEnum responseEnum;

    protected Object[] args;

    public BaseException(ResponseEnum responseEnum) {
        super(responseEnum.getMessage());
    }

    public BaseException(int code, String msg) {
        super(msg);
        this.responseEnum = new ResponseEnum() {
            @Override
            public int getCode() {
                return code;
            }

            @Override
            public String getMessage() {
                return msg;
            }
        };
    }

    public BaseException(ResponseEnum responseEnum, Object[] args, String msg) {
        super(msg);
        this.responseEnum = responseEnum;
        this.args = args;
    }

    public BaseException(ResponseEnum responseEnum, Object[] args, String msg, Throwable cause) {
        super(msg, cause);
        this.responseEnum = responseEnum;
        this.args = args;
    }
}
