package com.peerloop.peerloopapp.global.auth.jwt;


import static com.peerloop.peerloopapp.global.exception.ExceptionLoggingUtil.logWarn;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerloop.peerloopapp.global.common.dto.ErrorResponse;
import com.peerloop.peerloopapp.global.exception.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    // TODO: 테스트 필요

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.HANDLE_ACCESS_DENIED_EXCEPTION;

        logWarn(accessDeniedException);

        // TODO: JwtAccessDeniedHandler, JwtAuthenticationEntryPoint, JwtExceptionFilter(JwtFilter) 에서 중복되는 코드 -> 어떻게 분리?
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.getWriter()
                .write(objectMapper.writeValueAsString((
                        ErrorResponse.from(errorCode)
                )));
    }
}
