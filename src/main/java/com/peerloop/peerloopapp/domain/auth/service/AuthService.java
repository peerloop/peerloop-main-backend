package com.peerloop.peerloopapp.domain.auth.service;


import static com.peerloop.peerloopapp.global.exception.ErrorCode.FAILED_LOGIN_BY_ANYTHING;
import static com.peerloop.peerloopapp.global.exception.ErrorCode.NOT_FOUND_REFRESH_TOKEN;

import com.peerloop.peerloopapp.domain.auth.entity.Auth;
import com.peerloop.peerloopapp.domain.auth.repository.AuthRepository;
import com.peerloop.peerloopapp.global.auth.jwt.JwtProvider;
import com.peerloop.peerloopapp.global.auth.jwt.dto.TokenDto;
import com.peerloop.peerloopapp.global.exception.CustomException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;

    public void checkPassword(String rawPassword, String storedPassword) {
        if (!passwordEncoder.matches(rawPassword, storedPassword)) {
            throw new CustomException(FAILED_LOGIN_BY_ANYTHING);
        }
    }

    public TokenDto createTokenAndSaveAuth(Long memberId) {
        // 1. access token, refresh token 생성
        TokenDto token = jwtProvider.generateToken(memberId);

        // 2. refresh token DB에 저장
        // memberId에 대한 refreshToken(Auth)이 DB에 존재하면 update, 존재하지 않으면 save
        Optional<Auth> auth = authRepository.findByMemberId(memberId);
        auth.ifPresentOrElse(
                existingAuth -> existingAuth.updateRefreshToken(token.refreshToken()),
                () -> {
                    Auth newAuth = Auth.builder()
                            .memberId(memberId)
                            .refreshToken(token.refreshToken())
                            .build();
                    authRepository.save(newAuth);
                }
        );

        return token;
    }

    public void deleteRefreshToken(Long memberId) {
        Optional<Auth> auth = authRepository.findByMemberId(memberId);
        auth.ifPresentOrElse(
                existingAuth -> authRepository.delete(existingAuth),
                () -> {
                    throw new CustomException(NOT_FOUND_REFRESH_TOKEN);
                }
        );

    }

    public Auth getAuthByRefreshToken(String refreshToken) {
        Optional<Auth> auth = authRepository.findByRefreshToken(refreshToken);
        return auth
                .orElseThrow(() -> new CustomException(NOT_FOUND_REFRESH_TOKEN));
    }

}
