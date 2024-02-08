package com.peerloop.peerloopapp.global.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorCode {

    // ** Common
    INVALID_INPUT_VALUE(BAD_REQUEST, "C_001", "잘못된 입력 값입니다."),
    INVALID_TYPE_VALUE(BAD_REQUEST, "C_002", "잘못된 타입입니다."),
    MISSING_INPUT_VALUE(BAD_REQUEST, "C_003", "인자가 부족합니다."),
    NOT_EXIST_API(BAD_REQUEST, "C_004", "요청 주소가 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C_005", "사용할 수 없는 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C_006", "서버 에러입니다."),
    FAILED_FIELD_VALIDATION(BAD_REQUEST, "C_007", "필드 검증에 실패하였습니다."),

    // ** Auth
    FAILED_LOGIN_BY_ANYTHING(BAD_REQUEST, "A_001", "등록되지 않은 이메일이거나, 이메일 또는 비밀번호를 잘못 입력했습니다."),
    NOT_PERMITTED(BAD_REQUEST, "A_002", "해당 요청에 대한 권한이 없습니다."),
    INVALID_TOKEN_ETC(BAD_REQUEST, "A_003", "기타 보안 문제로 토큰이 유효하지 못합니다."),
    NOT_FOUND_REFRESH_TOKEN(BAD_REQUEST, "A_004", "해당 리프레쉬 토큰의 인증 데이터가 존재하지 않습니다."),
    NOT_INCLUDE_ACCESS_TOKEN(BAD_REQUEST, "A_005", "요청에 액세스 토큰이 존재하지 않습니다."),
    NOT_FOUND_REQUEST(BAD_REQUEST, "A_006", "해당 요청이 존재하지 않습니다."),
    NOT_FOUND_MEMBER_ID(BAD_REQUEST, "A_007", "해당 사용자의 인증 데이터가 존재하지 않습니다."),
    TOKEN_EXPIRED(BAD_REQUEST, "A_008", "토큰이 만료 시간을 초과했습니다."),
    UNSUPPORTED_TOKEN(BAD_REQUEST, "A_010", "토큰 유형이 지원되지 않습니다."),
    MALFORMED_TOKEN(BAD_REQUEST, "A_011", "토큰의 구조가 올바르지 않습니다."),
    BLACKLISTED_TOKEN(BAD_REQUEST, "A_012", "해당 토큰은 블랙리스트에 등록되어있으므로 유효하지 않습니다."),
    HANDLE_AUTHENTICATION_EXCEPTION(UNAUTHORIZED, "A_013", "인증되지 않은 사용자입니다."),
    HANDLE_ACCESS_DENIED_EXCEPTION(FORBIDDEN, "A_014", "접근 권한이 없습니다."),

    // ** Member
    NOT_FOUND_BY_ID(BAD_REQUEST, "M_001", "id에 해당하는 사용자가 존재하지 않습니다."),
    NOT_FOUND_BY_EMAIL(BAD_REQUEST, "M_002", "email에 해당하는 사용자가 존재하지 않습니다."),
    DUPLICATED_EMAIL(BAD_REQUEST, "M_003", "이미 존재하는 email입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
