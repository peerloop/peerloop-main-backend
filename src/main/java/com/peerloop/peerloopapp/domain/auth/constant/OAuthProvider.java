package com.peerloop.peerloopapp.domain.auth.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum OAuthProvider {
    GOOGLE("google"),
    GITHUB("github");

    private final String oAuthProvider;

    // Obtain enum object based on the actual value
    public static OAuthProvider fromValue(String value) {
        for (OAuthProvider oAuthProvider : OAuthProvider.values()) {
            if (oAuthProvider.getOAuthProvider().equals(value)) {
                return oAuthProvider;
            }
        }

        throw new IllegalArgumentException("Invalid OAuth provider value: " + value);
    }
}
