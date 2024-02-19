package com.peerloop.peerloopapp.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {

    ADMIN("ADMIN"),
    USER("USER");

    private final String role;
    
}
