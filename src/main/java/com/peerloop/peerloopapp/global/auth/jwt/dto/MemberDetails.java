package com.peerloop.peerloopapp.global.auth.jwt.dto;

public record MemberDetails(
        String memberId
) {
    public static MemberDetails of(String memberId) {
        return new MemberDetails(memberId);
    }
}
