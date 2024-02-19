package com.peerloop.peerloopapp.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OAuthMemberInfo {

    String id;
    String email;

    public static OAuthMemberInfo of(String id, String email) {
        return new OAuthMemberInfo(id, email);
    }
}
