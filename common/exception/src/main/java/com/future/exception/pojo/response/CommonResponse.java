package com.future.exception.pojo.response;

import com.future.exception.constant.ResponseEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 20:23
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class CommonResponse<T> extends BaseResponse {
    protected T data;

    public CommonResponse() {
        super();
    }

    public CommonResponse(ResponseEnum responseEnum, T data) {
        super();
        this.data = data;
    }
}
