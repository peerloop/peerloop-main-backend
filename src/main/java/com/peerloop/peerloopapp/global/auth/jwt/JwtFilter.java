package com.peerloop.peerloopapp.global.auth.jwt;

import static com.peerloop.peerloopapp.global.exception.ExceptionLoggingUtil.logCustom;
import static com.peerloop.peerloopapp.global.exception.ExceptionLoggingUtil.logError;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peerloop.peerloopapp.global.common.dto.ErrorResponse;
import com.peerloop.peerloopapp.global.exception.CustomException;
import com.peerloop.peerloopapp.global.exception.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    // TODO: ObjectMapper 이렇게 주입 받아도 되나? (thread-safe?)
    private final ObjectMapper objectMapper;

    private static final String BEARER_PREFIX = "Bearer ";


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 예외 처리
        // - AccessDeniedException, AuthenticationException: AccessDeniedHandler, AuthenticationEntryPoint 에서 catch
        // - CustomException, RuntimeException: JwtFilter에서 catch

        try {

            // 1. request의 Authorization header에서 token 추출
            String token = parseBearerToken(request);

            // 2. validateToken()을 통해 token 검증
            if (token != null && jwtProvider.validateToken(token)) {
                // token이 유효하다면, 검증 객체를 가져와서 SecurityContext에 저장
                Authentication authentication = jwtProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 3. 다음 filter 이어서 수행
            filterChain.doFilter(request, response);

        } catch (CustomException e) {

            ErrorCode errorCode = e.getErrorCode();
            logCustom(request, e, errorCode);
            setErrorResponse(response, errorCode);

        } catch (RuntimeException e) {
            // TODO: RuntimeException을 이렇게 잡아도 되나?

            ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            logError(e);
            e.printStackTrace();    // TODO: 다음의 메서드를 통해 발생한 RuntimeException의 trace를 남기는 것이 좋은 방법인가?
            setErrorResponse(response, errorCode);

        }
    }

    private String parseBearerToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(token -> token.substring(0, 7).equalsIgnoreCase(BEARER_PREFIX))
                .map(token -> token.substring(7))
                .orElse(null);
    }

    private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("utf-8");
        response.getWriter()
                .write(objectMapper.writeValueAsString((
                        ErrorResponse.from(errorCode)
                )));
    }
}
