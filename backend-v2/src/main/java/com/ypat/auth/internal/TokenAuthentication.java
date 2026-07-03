package com.ypat.auth.internal;

import com.ypat.auth.api.Principal;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * PR-14 follow-up: Spring Security Authentication backed by our
 * {@link Principal}. Used by {@link JwtTokenAuthFilter}.
 */
public class TokenAuthentication extends AbstractAuthenticationToken {

    private final Principal principal;

    public TokenAuthentication(Principal principal,
                                Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";       // never expose the raw token through Spring Security
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public Principal ypatPrincipal() {
        return principal;
    }
}