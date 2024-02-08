package com.peerloop.peerloopapp.global.exception;

import com.peerloop.peerloopapp.global.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handlerCustomException(HttpServletRequest request, CustomException e) {

        ErrorCode errorCode = e.getErrorCode();
        ExceptionLoggingUtil.logCustom(request, e, errorCode);

        return createErrorResponseEntity(errorCode);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDeniedException(AccessDeniedException e) {

        // TODO: 실제로 필요한지는 테스트 필요
        // AccessDeniedException은 JwtAccessDeniedHandler, 즉 스프링 시큐리티 측에서 처리하도록 넘긴다.
        e.printStackTrace();
        throw e;
    }

    // request body 필드 검증 (@Valid, Bean Validation)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException e) {
        ExceptionLoggingUtil.logDebug(request, e);

//        e.printStackTrace();  // TODO: exception log 고려 필요 (다 표시하면 안됨)

        BindingResult bindingResult = e.getBindingResult();

        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        fieldErrors.forEach(
                error -> log.debug(
                        "[EXCEPTION] FIELD_ERROR       -----> [{} = {}]: {}",
                        error.getField(),
                        error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                        error.getDefaultMessage()
                )
        );

        ErrorCode errorCode = ErrorCode.FAILED_FIELD_VALIDATION;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, bindingResult);

        return createErrorResponseEntity(errorCode, errorResponse);

    }

    // 엔티티 필드 검증 (@NotNull, @NotBlank 등)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(HttpServletRequest request, ConstraintViolationException e) {
        ExceptionLoggingUtil.logDebug(request, e);

//        e.printStackTrace();  // TODO: exception log 고려 필요 (다 표시하면 안됨)

        Map<String, String> errors = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                                .reduce((first, second) -> second)
                                .orElseThrow(IllegalArgumentException::new)
                                .toString(),
                        ConstraintViolation::getMessage
                ));

        ErrorCode errorCode = ErrorCode.FAILED_FIELD_VALIDATION;
        ErrorResponse errorResponse = ErrorResponse.of(errorCode, errors);

        return createErrorResponseEntity(errorCode, errorResponse);

    }

    // 요청 데이터로 들어와야할 인자 부족
    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestValueException(HttpServletRequest request, MissingRequestValueException e) {

        ExceptionLoggingUtil.logDebug(request, e);
        return createErrorResponseEntity(ErrorCode.MISSING_INPUT_VALUE);
    }

    // 해당 URI에 잘못된 HttpMethod
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(HttpServletRequest request, HttpRequestMethodNotSupportedException e) {

        ExceptionLoggingUtil.logDebug(request, e);
        return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED);
    }

    // 없는 API
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(HttpServletRequest request, NoHandlerFoundException e) {

        ExceptionLoggingUtil.logDebug(request, e);
        return createErrorResponseEntity(ErrorCode.NOT_EXIST_API);
    }

    // 메서드 validation 예외 상황
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(HttpServletRequest request, IllegalArgumentException e) {

        ExceptionLoggingUtil.logWarn(e);
        return createErrorResponseEntity(ErrorCode.INVALID_INPUT_VALUE);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> runtimeExceptionHandler(HttpServletRequest request, RuntimeException e) {

        ExceptionLoggingUtil.logError(e);
        String messageCode = "RUNTIME_ERROR";
        String errorMessage = e.getMessage(); // TODO: exception log 고려 필요 (다 표시하면 안됨)
        e.printStackTrace();
        ErrorResponse errorResponse = getErrorResponse(messageCode, errorMessage);

        return createErrorResponseEntity(ErrorCode.INTERNAL_SERVER_ERROR, errorResponse);
    }


    private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode) {

        return ResponseEntity
                .status(errorCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorResponse.from(errorCode));
    }

    private ResponseEntity<ErrorResponse> createErrorResponseEntity(ErrorCode errorCode, ErrorResponse errorResponse) {

        return ResponseEntity
                .status(errorCode.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
    }

    private ErrorResponse getErrorResponse(String originCode, String message) {
        return switch (originCode) {
            case "NotBlank" -> ErrorResponse.of("EMPTY_FIELD_ERROR", message);
            case "Email", "Pattern" -> ErrorResponse.of("REGEX_ERROR", message);
            case "Max", "Min", "Size" -> ErrorResponse.of("SIZE_LIMIT_ERROR", message);
            default -> ErrorResponse.of("UNEXPECTED_ERROR", message);
        };
    }

}
