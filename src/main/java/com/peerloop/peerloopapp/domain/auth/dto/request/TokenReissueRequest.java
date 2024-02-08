package com.peerloop.peerloopapp.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TokenReissueRequest(
        @NotBlank(message = "refresh token은 blank일 수 없습니다.")
        String refreshToken
) {

}
