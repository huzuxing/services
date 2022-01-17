package com.future.exception.exception.assertion;

import com.future.exception.exception.BaseException;
import com.future.exception.exception.BusinessException;
import com.future.exception.constant.ResponseEnum;

import java.text.MessageFormat;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 15:34
 */
public interface BusinessExceptionAssert extends ResponseEnum, Assert {
    @Override
    default BaseException newException(Object... args) {
        return new BusinessException(this, args, msg(args));
    }

    @Override
    default BaseException newException(Throwable t, Object... args) {
        return new BusinessException(this, args, msg(args), t);
    }

    private String msg(Object... args) {
        String msg = this.getMessage();
        if (null != args && args.length > 0) {
            msg = MessageFormat.format(msg, args);
        }
        return msg;
    }
}
