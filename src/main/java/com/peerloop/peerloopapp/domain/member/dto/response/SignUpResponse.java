package com.peerloop.peerloopapp.domain.member.dto.response;

public record SignUpResponse(
        Long memberId
) {
    public static SignUpResponse of(Long memberId) {
        return new SignUpResponse(memberId);
    }
}
