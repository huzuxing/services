package com.future.exception.constant.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.servlet.http.HttpServletResponse;

/**
 * @author huzuxing
 * @version 1.0
 * @description: TODO
 * @date 2021/7/11 16:08
 */

@Getter
@AllArgsConstructor
public enum ServletResponseEnum {
    METHOD_ARGUMENT_NOT_VALID(4400, "",HttpServletResponse.SC_BAD_REQUEST),
    METHOD_ARGUMENT_TYPE_MISMATCH(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    MISS_SERVLET_REQUEST_PART(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    MISS_PATH_VARIABLE(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    BIND_ERROR(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    MISS_SERVLET_REQUEST_PARAMETER(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    TYPE_MISMATCH(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    SERVLET_REQUEST_BIND_ERROR(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    HTTP_MESSAGE_NOT_READABLE(4400, "", HttpServletResponse.SC_BAD_REQUEST),
    NO_HANDLER_FOUND(4404, "", HttpServletResponse.SC_NOT_FOUND),
    NO_SUCH_REQUEST_HANDLING_METHOD(4404, "", HttpServletResponse.SC_NOT_FOUND),
    HTTP_REQUEST_METHOD_NOT_SUPPORT(4405, "", HttpServletResponse.SC_METHOD_NOT_ALLOWED),
    HTTP_MEDIA_TYPE_NOT_ACCEPTABLE(4406, "", HttpServletResponse.SC_NOT_ACCEPTABLE),
    HTTP_MEDIA_TYPE_NOT_SUPPORT(4415, "", HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE),
    CONVERSION_NOT_SUPPORT(4500, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    HTTP_MESSAGE_NOT_WRITABLE(4500, "", HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
    ASYNC_REQUEST_TIMEOUT(4503, "", HttpServletResponse.SC_SERVICE_UNAVAILABLE);

    private int code;
    private String message;
    private int statusCode;
}
