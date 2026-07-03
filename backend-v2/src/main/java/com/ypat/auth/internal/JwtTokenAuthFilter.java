package com.ypat.auth.internal;

import com.ypat.auth.api.Principal;
import com.ypat.auth.api.TokenBridge;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * PR-14 follow-up: Servlet filter that pulls the Authorization
 * header, hands it to TokenBridge.resolve, and pushes a
 * Spring Security Authentication into the SecurityContext.
 *
 * Instantiated as a @Bean inside {@link AuthSecurityConfig}
 * (not @Component) so we can pin the position with
 * addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class).
 * Spring Boot's default @Component registration lands the filter
 * at the very end of the chain — too late for SecurityContextHolder
 * to flow into the controllers.
 */
public class JwtTokenAuthFilter extends OncePerRequestFilter {

    private final TokenBridge bridge;

    public JwtTokenAuthFilter(TokenBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        System.out.println("[DEBUG] JwtTokenAuthFilter path=" + req.getRequestURI()
                + " header=" + (header == null ? "null" : header.substring(0, Math.min(40, header.length()))));
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Optional<Principal> p = bridge.resolve(token);
            System.out.println("[DEBUG] TokenBridge.resolve -> " + (p.isPresent() ? "OK uid=" + p.get().userId() : "EMPTY"));
            if (p.isPresent()) {
                Principal principal = p.get();
                Authentication auth = new TokenAuthentication(
                        principal,
                        principal.roles().stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList()));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }
        chain.doFilter(req, res);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        // Actuator health check + WebHook-style endpoints are
        // public; the SecurityFilterChain config in
        // AuthSecurityConfig handles the actual permitAll
        // decision.
        String path = req.getRequestURI();
        return path.startsWith("/actuator/")
                || path.equals("/wxpay/notify");
    }
}