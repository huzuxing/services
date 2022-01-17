package com.future.exception.exception.assertion;

import com.future.exception.exception.BaseException;
import com.future.exception.exception.ExceptionMessageWrap;

import java.text.MessageFormat;
import java.util.function.Supplier;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 14:12
 */
public interface Assert {
    /**
     * create exception
     * @param args
     * @return
     */
    BaseException newException(Object... args);

    /**
     * create exception
     * @param t
     * @param args
     * @return
     */
    BaseException newException(Throwable t, Object... args);

    default BaseException newException(String msg, Object... args) {
        if (null != args && args.length > 0) {
            msg = MessageFormat.format(msg, args);
        }
        ExceptionMessageWrap messageWrap = new ExceptionMessageWrap(msg);
        throw newException(messageWrap, args);
    }

    default BaseException newException(String msg, Throwable cause, Object... args) {
        if (null != args && args.length > 0) {
            msg = MessageFormat.format(msg, args);
        }
        ExceptionMessageWrap messageWrap = new ExceptionMessageWrap(msg, cause);
        throw newException(messageWrap, args);
    }

    default void assertNotNull(Object obj) {
        if (null == obj) {
            throw newException();
        }
    }
    default void assertNotNull(Object obj, String msg) {
        if (null == obj) {
            throw newException(msg, obj);
        }
    }
    default void assertNotNull(Object obj, String msg, Object... args) {
        if (null == obj) {
            throw newException(msg, args);
        }
    }
    default void assertNotNull(Object obj, Supplier<String> msg) {
        if (null == obj) {
            throw newException(msg.get());
        }
    }
    default void assertNotNull(Object obj, Supplier<String> msg, Object... args) {
        if (null == obj) {
            throw newException(msg.get(), args);
        }
    }
    default void assertNotEmpty(String v) {
        if (null == v || "".equals(v.trim())) {
            throw newException();
        }
    }
    default void assertNotEmpty(String v, String msg) {
        if (null == v || "".equals(v.trim())) {
            throw newException(msg);
        }
    }
    default void assertNotEmpty(String v, String msg, Object... args) {
        if (null == v || "".equals(v.trim())) {
            throw newException(msg, args);
        }
    }
}
