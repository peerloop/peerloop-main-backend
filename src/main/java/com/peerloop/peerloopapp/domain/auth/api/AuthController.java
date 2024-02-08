package com.peerloop.peerloopapp.domain.auth.api;

import com.peerloop.peerloopapp.domain.auth.dto.request.LogInRequest;
import com.peerloop.peerloopapp.domain.auth.dto.request.TokenReissueRequest;
import com.peerloop.peerloopapp.domain.auth.dto.response.AuthResponse;
import com.peerloop.peerloopapp.domain.auth.dto.response.TokenReissueResponse;
import com.peerloop.peerloopapp.domain.auth.facade.AuthFacade;
import com.peerloop.peerloopapp.global.auth.jwt.dto.MemberDetails;
import com.peerloop.peerloopapp.global.auth.jwt.dto.TokenDto;
import com.peerloop.peerloopapp.global.common.dto.IdResponse;
import com.peerloop.peerloopapp.global.common.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "02. Auth Controller", description = "인증 인가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthFacade authFacade;

    // TODO: TOKEN_TYPE은 JwtProvider 단에서 아예 TokenDto에 넣어주는 것이 낫나?
    private static final String TOKEN_TYPE = "Bearer";

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    // TODO: EXPIRATION_SECONDS 따로 관리
    private static final Integer COOKIE_EXPIRATION_SECONDS = 7 * 24 * 60 * 60;


    @Operation(summary = "로그인", description = "email과 password를 이용하여 로그인을 진행합니다.")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> login(@Valid @RequestBody LogInRequest request) {
        TokenDto token = authFacade.login(request);

        // TODO: 로그인 api에서 token에 "Bearer " prefix를 붙여서 보낼 필요가 없나? 클라이언트 단에서 authorization header에 넣을 때 "Bearer "를 붙이면 되나?
        // TODO: access token, refresh token을 모두 json response body에 포함하는 것이 safe 한가?
        // TODO: SET_COOKIE 과연 안전한가?

        // access / refresh token을 넘겨주는 방법: (1) response body (2) set-cookie header (w/ http only, secure option)
        // set-cookie header: 클라이언트의 쿠키에 refresh token 저장

        // 우선은, 두 방법 모두 적용

        // create token cookies
        ResponseCookie accessTokenCookie = createTokenCookie(ACCESS_TOKEN, token.accessToken());
        ResponseCookie refreshTokenCookie = createTokenCookie(REFRESH_TOKEN, token.refreshToken());

        // create response
        AuthResponse response = AuthResponse.of(token.accessToken(), token.refreshToken(), TOKEN_TYPE);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(new SuccessResponse<>(response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<IdResponse> logout(@AuthenticationPrincipal MemberDetails memberDetails) {
        // TODO: MemberDetails를 아예 넘기는 것이 낫나?
        authFacade.logout(memberDetails.memberId());
        return ResponseEntity.ok().body(new IdResponse(memberDetails.memberId()));
    }

    @Operation(summary = "액세스 토큰 재발급", description = "Refresh Token을 통해 Access Token을 재발급합니다.")
    @PostMapping("/reissue-token")
    public ResponseEntity<SuccessResponse<TokenReissueResponse>> reissueAccessToken(@Valid @RequestBody TokenReissueRequest request) {
        // TODO: token을 cookie로 받고 반환하는 것이 필요할까? 혹은 지금처럼 명시적으로 DTO로만 하는 것이 좋을까?
        TokenReissueResponse response = authFacade.generateAccessTokenByRefreshToken(request);
        return ResponseEntity.ok().body(new SuccessResponse<>(response));
    }


    @Operation(summary = "로그인 정보 확인", description = "현재 로그인 되어 있는 회원의 식별자 값을 반환합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<IdResponse> getInfo(@AuthenticationPrincipal MemberDetails memberDetails) {
        Long memberId = memberDetails.memberId();
        return ResponseEntity.ok().body(new IdResponse(memberId));
    }


    private ResponseCookie createTokenCookie(String tokenType, String token) {
        return ResponseCookie.from(tokenType, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(COOKIE_EXPIRATION_SECONDS)
                .sameSite("None")
                .build();
    }
}
