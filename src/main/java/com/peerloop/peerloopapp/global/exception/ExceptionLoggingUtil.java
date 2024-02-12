package com.peerloop.peerloopapp.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionLoggingUtil {

    private static final String EXCEPTION_MESSAGE_FORMAT = "[EXCEPTION] EXCEPTION_MESSAGE -----> [{}]";
    private static final String EXCEPTION_TYPE_FORMAT = "[EXCEPTION] EXCEPTION_TYPE    -----> [{}]";
    private static final String EXCEPTION_REQUEST_URI = "[EXCEPTION] REQUEST_URI       -----> [{}]";
    private static final String EXCEPTION_HTTP_METHOD_TYPE = "[EXCEPTION] HTTP_METHOD_TYPE  -----> [{}]";

    // Security Filter 단 Exception 용
    private static final String ERROR_MESSAGE_FORMAT = "[ERROR] {} : {}";

    public static void logCustom(HttpServletRequest request, CustomException e, ErrorCode errorCode) {
        log.debug(EXCEPTION_REQUEST_URI, request.getRequestURI());
        log.debug(EXCEPTION_HTTP_METHOD_TYPE, request.getMethod());
        // log.debug(MessageUtil.getMessage(e.getMessageKey()));
        log.info(errorCode.getCode());
        log.warn(EXCEPTION_TYPE_FORMAT, e.getClass().getSimpleName());
        log.warn(EXCEPTION_MESSAGE_FORMAT, e.getMessage());
    }

    public static void logDebug(HttpServletRequest request, Exception e) {
        log.debug(EXCEPTION_REQUEST_URI, request.getRequestURI());
        log.debug(EXCEPTION_HTTP_METHOD_TYPE, request.getMethod());
        log.debug(EXCEPTION_TYPE_FORMAT, e.getClass().getSimpleName());
        log.debug(EXCEPTION_MESSAGE_FORMAT, e.getMessage());
    }

    public static void logWarn(Exception e) {
        log.warn(EXCEPTION_TYPE_FORMAT, e.getClass().getSimpleName());
        log.warn(EXCEPTION_MESSAGE_FORMAT, e.getMessage());
    }

    public static void logError(Exception e) {
        log.error(ERROR_MESSAGE_FORMAT, e.getClass().getSimpleName(), e.getMessage());
    }
}
