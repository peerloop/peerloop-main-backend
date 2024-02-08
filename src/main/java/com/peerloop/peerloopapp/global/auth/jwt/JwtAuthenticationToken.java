package com.peerloop.peerloopapp.global.auth.jwt;

import java.util.Collection;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;
    private String credentials;

    public JwtAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities) {
        this(principal, null, authorities);
    }

    public JwtAuthenticationToken(String principal, String credentials) {
        super(null);
        super.setAuthenticated(false);

        this.principal = principal;
        this.credentials = credentials;
    }

    public JwtAuthenticationToken(
            Object principal, String credentials, Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        super.setAuthenticated(true);

        this.principal = principal;
        this.credentials = credentials;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    /* TODO: 참고 자료에서 가져오긴 했으나 실제로 필요한지는 모르겠음
    @Override
    public void setAuthenticated(boolean isAuthenticated) {
        if (isAuthenticated) {
            throw new IllegalArgumentException("유효하지 않은 접근입니다.");
        }
        super.setAuthenticated(false);
    }
     */

    @Override
    public void eraseCredentials() {
        super.eraseCredentials();
        credentials = null;
    }

    @Override
    public String toString() {
        return "JwtAuthenticationToken{"
                + "principal=" + principal
                + ", credentials='" + credentials + '\''
                + '}';
    }
}
