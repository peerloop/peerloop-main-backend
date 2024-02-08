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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.HANDLE_AUTHENTICATION_EXCEPTION;

        logWarn(authException);

        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.getWriter()
                .write(objectMapper.writeValueAsString((
                        ErrorResponse.from(errorCode)
                )));
    }
}
