package com.peerloop.peerloopapp.global.common.dto;

public record IdResponse(
        String id
) {
    public static IdResponse of(String id) {
        return new IdResponse(id);
    }
}
