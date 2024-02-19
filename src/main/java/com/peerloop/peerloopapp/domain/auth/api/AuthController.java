package com.peerloop.peerloopapp.domain.auth.api;

import com.peerloop.peerloopapp.domain.auth.api.request.LogInRequest;
import com.peerloop.peerloopapp.domain.auth.api.request.TokenReissueRequest;
import com.peerloop.peerloopapp.domain.auth.api.response.AuthResponse;
import com.peerloop.peerloopapp.domain.auth.api.response.TokenReissueResponse;
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
import org.springframework.web.bind.annotation.*;

@Tag(name = "02. Auth Controller", description = "인증 인가 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthFacade authFacade;
    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    // TODO: EXPIRATION_SECONDS 따로 관리
    private static final Integer COOKIE_EXPIRATION_SECONDS = 7 * 24 * 60 * 60;

    @Operation(summary = "Social Login", description = "Currently support Google.")
    @GetMapping("login/oauth2/callback/{oauthServiceName}")
    public ResponseEntity<AuthResponse> oauthCallback(@RequestParam(name = "code") String code, @PathVariable(name = "oauthServiceName") String oauthServiceName) {
        AuthResponse responseBody = authFacade.oauthLogin(code, oauthServiceName);

        // TODO: 로그인 api에서 token에 "Bearer " prefix를 붙여서 보낼 필요가 없나? 클라이언트 단에서 authorization header에 넣을 때 "Bearer "를 붙이면 되나?
        // TODO: access token, refresh token을 모두 json response body에 포함하는 것이 safe 한가?
        // TODO: SET_COOKIE 과연 안전한가?

        // TODO: 지금처럼 response body에 token을 넘겨주게 되면, front 측에서 받을 방법이 없다. 보통 어떻게 처리하는지? picktoss에서는 token을 query string으로 포함하는 URL로 redirect시켜서 프런트가 캐치할 수 있도록 해줬다.
        return ResponseEntity.ok()
                .body(responseBody);
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<IdResponse> logout(@AuthenticationPrincipal MemberDetails memberDetails) {
        // TODO: MemberDetails를 아예 넘기는 것이 낫나?
        authFacade.logout(memberDetails.memberId());
        return ResponseEntity.ok().body(new IdResponse(memberDetails.memberId()));
    }
//
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
        String memberId = memberDetails.memberId();
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
