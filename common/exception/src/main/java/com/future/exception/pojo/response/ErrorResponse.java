package com.future.exception.pojo.response;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 17:09
 */
public class ErrorResponse extends BaseResponse{
    public ErrorResponse() {
    }

    public ErrorResponse(int code, String message) {
        super(code, message);
    }
}
