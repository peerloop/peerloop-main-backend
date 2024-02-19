package com.peerloop.peerloopapp.domain.auth.service;


import static com.peerloop.peerloopapp.global.exception.ErrorCode.FAILED_LOGIN_BY_ANYTHING;
import static com.peerloop.peerloopapp.global.exception.ErrorCode.NOT_FOUND_REFRESH_TOKEN;

import com.fasterxml.jackson.databind.JsonNode;
import com.peerloop.peerloopapp.domain.auth.dto.OAuthMemberInfo;
import com.peerloop.peerloopapp.domain.auth.entity.Auth;
import com.peerloop.peerloopapp.domain.auth.repository.AuthRepository;
import com.peerloop.peerloopapp.global.auth.jwt.JwtProvider;
import com.peerloop.peerloopapp.global.auth.jwt.dto.TokenDto;
import com.peerloop.peerloopapp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final JwtProvider jwtProvider;
    private final AuthRepository authRepository;
    private final Environment env;

    public OAuthMemberInfo getOAuthMemberInfo(String code, String oAuthProvider) {
        // [1] Retrieve OAuth access token
        String oauthAccessToken = getOAuthAccessToken(code, oAuthProvider);

        // [2] Get member info (id)
        String userinfoUri = env.getProperty("oauth2." + oAuthProvider + ".userinfo-uri");
        // TODO: null exception handling
        assert userinfoUri != null;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + oauthAccessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<JsonNode> responseNode = restTemplate.exchange(
                userinfoUri,
                HttpMethod.GET,
                requestEntity,
                JsonNode.class
        );
        JsonNode responseBody = responseNode.getBody();
        assert responseBody != null;
        return OAuthMemberInfo.of(
                responseBody.get("id").asText(),
                responseBody.get("email").asText()
        );
    }

    /**
     * Exchange OAuth token with code
     * @param code OAuth code passed as a query parameter for the callback endpoint
     * @param oAuthProvider OAuth2 provider name. ex) google, github, etc
     * @return OAuth2 access token
     */
    private String getOAuthAccessToken(String code, String oAuthProvider) {
        String clientId = env.getProperty("oauth2." + oAuthProvider + ".client-id");
        String clientSecret = env.getProperty("oauth2." + oAuthProvider + ".client-secret");
        String redirectUri = env.getProperty("oauth2." + oAuthProvider + ".redirect-uri");

        // Header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Request Body
        LinkedMultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("grant_type", "authorization_code");
        requestBody.add("client_id", clientId);
        requestBody.add("client_secret", clientSecret);
        requestBody.add("redirect_uri", redirectUri);
        requestBody.add("code", code);

        // Create HTTP entity
        HttpEntity<LinkedMultiValueMap<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        // Send POST request
        String tokenUri = env.getProperty("oauth2." + oAuthProvider + ".token-uri");
        assert tokenUri != null;
        ResponseEntity<JsonNode> responseNode = restTemplate.exchange(
                tokenUri,
                HttpMethod.POST,
                requestEntity,
                JsonNode.class
        );

        JsonNode responseBody = responseNode.getBody();
        assert responseBody != null;
        return responseBody.get("access_token").asText();
    }


    public TokenDto createTokenAndSaveAuth(String memberId) {
        // 1. access token, refresh token 생성
        TokenDto token = jwtProvider.generateToken(memberId);

        // 2. refresh token DB에 저장
        // memberId에 대한 refreshToken(Auth)이 DB에 존재하면 update, 존재하지 않으면 save
        authRepository.findByMemberId(memberId).ifPresentOrElse(
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

    public void deleteRefreshToken(String memberId) {
        authRepository.findByMemberId(memberId).ifPresentOrElse(
                existingAuth -> authRepository.delete(existingAuth),
                () -> {
                    throw new CustomException(NOT_FOUND_REFRESH_TOKEN);
                }
        );

    }

    public Auth getAuthByRefreshToken(String refreshToken) {
        return authRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new CustomException(NOT_FOUND_REFRESH_TOKEN));
    }

}
