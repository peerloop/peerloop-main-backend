package com.peerloop.peerloopapp.global.auth.jwt;


import static com.peerloop.peerloopapp.global.exception.ErrorCode.INVALID_TOKEN_ETC;
import static com.peerloop.peerloopapp.global.exception.ErrorCode.MALFORMED_TOKEN;
import static com.peerloop.peerloopapp.global.exception.ErrorCode.TOKEN_EXPIRED;
import static com.peerloop.peerloopapp.global.exception.ErrorCode.UNSUPPORTED_TOKEN;

import com.peerloop.peerloopapp.global.auth.jwt.dto.MemberDetails;
import com.peerloop.peerloopapp.global.auth.jwt.dto.TokenDto;
import com.peerloop.peerloopapp.global.common.enums.MemberRole;
import com.peerloop.peerloopapp.global.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtProvider {

    private static final Long MILLI_SECOND = 1000L;
    private static final String ID_KEY = "memberId";
    private static final String ROLE_KEY = "role";
    private static final String DEFAULT_ROLE = MemberRole.USER.getRole();

    private final Key key;
    private final Long accessTokenExpirySeconds;
    private final Long refreshTokenExpirySeconds;

    public JwtProvider(
            @Value("${jwt.secret-key}") String secretKey,
            @Value("${jwt.expiry-seconds.access-token}") Long accessTokenExpirySeconds,
            @Value("${jwt.expiry-seconds.refresh-token}") Long refreshTokenExpirySeconds
    ) {
        this.accessTokenExpirySeconds = accessTokenExpirySeconds;
        this.refreshTokenExpirySeconds = refreshTokenExpirySeconds;

        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String createAccessToken(Long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpirySeconds * MILLI_SECOND);

        return Jwts.builder()
                .setHeaderParam("type", "jwt")
                .claim(ID_KEY, memberId)
                .claim(ROLE_KEY, DEFAULT_ROLE)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(Long memberId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + refreshTokenExpirySeconds * MILLI_SECOND);

        return Jwts.builder()
                .setHeaderParam("type", "jwt")
                .claim(ID_KEY, memberId)
                .claim(ROLE_KEY, DEFAULT_ROLE)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) throws CustomException {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new CustomException(MALFORMED_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            throw new CustomException(UNSUPPORTED_TOKEN);
        } catch (IllegalArgumentException e) {
            throw new CustomException(INVALID_TOKEN_ETC);
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Authentication getAuthentication(String token) {
        // token 복호화
        Claims claims = getClaims(token);

        // claim 정보 가져오기
        Long memberId = claims.get(ID_KEY, Long.class);
        String role = claims.get(ROLE_KEY, String.class);

        // Authentication 객체를 만들기 위한 principal과 authroities 생성
        MemberDetails principal = MemberDetails.of(memberId);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

        // JwtAuthenticationToken 생성 (UsernamePasswordAuthenticationToken 대신 사용)
        return new JwtAuthenticationToken(principal, null, authorities);
    }

    public TokenDto generateToken(Long memberId) {
        String accessToken = createAccessToken(memberId);
        String refreshToken = createRefreshToken(memberId);
        return TokenDto.of(accessToken, refreshToken);
    }

}
