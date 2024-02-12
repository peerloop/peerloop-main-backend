package com.peerloop.peerloopapp.global.auth.jwt.dto;

public record TokenDto(
        String accessToken,
        String refreshToken,

        String tokenType
) {
    public static TokenDto of(String accessToken, String refreshToken, String tokenType) {
        return new TokenDto(accessToken, refreshToken, tokenType);
    }
}

