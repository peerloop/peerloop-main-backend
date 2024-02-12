package com.peerloop.peerloopapp.global.common.dto;

import com.peerloop.peerloopapp.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.validation.BindingResult;

public record ErrorResponse(
        String code,
        String message,
        List<FieldError> errors
) {
    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(code, message, new ArrayList<>());
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), new ArrayList<>());
    }

    public static ErrorResponse of(ErrorCode errorCode, List<FieldError> errors) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors);
    }

    public static ErrorResponse of(ErrorCode errorCode, Map<String, String> errorMap) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), FieldError.of(errorMap));
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), FieldError.of(bindingResult));
    }


    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        private FieldError(final String field, final String value, final String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        private static List<FieldError> of(final Map<String, String> errorMap) {
            List<FieldError> fieldErrors = new ArrayList<>();
            for (Entry<String, String> entry : errorMap.entrySet()) {
                fieldErrors.add(new FieldError(
                        entry.getKey(),
                        null,
                        entry.getValue()
                ));
            }

            return fieldErrors;
        }

        public static List<FieldError> of(final String field, final String value, final String reason) {
            List<FieldError> fieldErrors = new ArrayList<>();
            fieldErrors.add(new FieldError(field, value, reason));
            return fieldErrors;
        }

        private static List<FieldError> of(final BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .collect(Collectors.toList());
        }

    }
}
