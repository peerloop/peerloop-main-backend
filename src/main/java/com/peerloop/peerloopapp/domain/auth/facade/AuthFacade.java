package com.peerloop.peerloopapp.domain.auth.facade;


import static com.peerloop.peerloopapp.global.exception.ErrorCode.FAILED_LOGIN_BY_ANYTHING;

import com.peerloop.peerloopapp.domain.auth.api.request.LogInRequest;
import com.peerloop.peerloopapp.domain.auth.api.request.TokenReissueRequest;
import com.peerloop.peerloopapp.domain.auth.api.response.AuthResponse;
import com.peerloop.peerloopapp.domain.auth.api.response.TokenReissueResponse;
import com.peerloop.peerloopapp.domain.auth.dto.OAuthMemberInfo;
import com.peerloop.peerloopapp.domain.auth.entity.Auth;
import com.peerloop.peerloopapp.domain.auth.service.AuthService;
import com.peerloop.peerloopapp.domain.member.entity.Member;
import com.peerloop.peerloopapp.domain.member.service.MemberService;
import com.peerloop.peerloopapp.global.auth.jwt.JwtProvider;
import com.peerloop.peerloopapp.global.auth.jwt.dto.TokenDto;
import com.peerloop.peerloopapp.global.common.enums.MemberRole;
import com.peerloop.peerloopapp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;


    @Transactional
    public AuthResponse oauthLogin(String code, String oAuthProvider) {
        // [1] Retrieve member information from oauth provider
        OAuthMemberInfo oAuthMemberInfo = authService.getOAuthMemberInfo(code, oAuthProvider);
        String memberId = oAuthMemberInfo.getId();

        // [2] Register with peerloop.
        // If id exists, issue an access token. If not, save and issue an access token.
        MemberRole role = MemberRole.USER;
        if (!memberService.existsById(memberId)) {
            memberService.createOAuthMember(oAuthMemberInfo, oAuthProvider, role);
        }

        // Issue internal JWT tokens
        TokenDto tokenDto = authService.createTokenAndSaveAuth(memberId);
        return AuthResponse.of(tokenDto.accessToken(), tokenDto.refreshToken(), tokenDto.tokenType());
    }

    @Transactional
    public void logout(String memberId) {
        // DB에서 refresh token(Auth) 제거
        authService.deleteRefreshToken(memberId);
    }

    public TokenReissueResponse generateAccessTokenByRefreshToken(TokenReissueRequest request) {
        // 1. refresh token 검증
        jwtProvider.validateToken(request.refreshToken());

        // 2. DB에 저장된 Auth 찾기
        Auth auth = authService.getAuthByRefreshToken(request.refreshToken());

        // TODO: 굳이 필요한 과정인가?
        // 3. DB에 Auth에 해당하는 member가 존재하는지 다시 확인
        String memberId = memberService.getMemberById(auth.getMemberId()).getId();

        // 4. access token 재발급
        String accessToken = jwtProvider.createAccessToken(memberId);

        // TODO: token 재발급 시, access token + refresh token을 모두 재발급 해주는 것이 좋을까? (RTR, Refresh Token Rotation)

        return TokenReissueResponse.of(accessToken);
    }
}
