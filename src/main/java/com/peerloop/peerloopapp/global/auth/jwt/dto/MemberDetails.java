package com.peerloop.peerloopapp.global.auth.jwt.dto;

public record MemberDetails(
        Long memberId
) {
    public static MemberDetails of(Long memberId) {
        return new MemberDetails(memberId);
    }
}
