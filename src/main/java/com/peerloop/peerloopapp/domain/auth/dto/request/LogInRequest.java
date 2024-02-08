package com.peerloop.peerloopapp.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LogInRequest(
        @NotBlank(message = "email은 blank일 수 없습니다.")
        @Email
        String email,

        @NotBlank(message = "password는 blank일 수 없습니다.")
        String password
) {
}
