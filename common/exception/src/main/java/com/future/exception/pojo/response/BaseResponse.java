package com.future.exception.pojo.response;

import com.future.exception.constant.ResponseEnum;
import com.future.exception.constant.enums.CommonResponseEnum;
import lombok.Data;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 15:59
 */

@Data
public class BaseResponse {

    protected int code;
    protected String message;

    public BaseResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public BaseResponse() {
        this(CommonResponseEnum.SUCCESS);
    }

    public BaseResponse(ResponseEnum responseEnum) {
        this(responseEnum.getCode(), responseEnum.getMessage());
    }

}
