package com.ypat.auth.internal;

import com.ypat.auth.api.TokenBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * PR-14 follow-up: SecurityFilterChain + WebMvcConfigurer.
 *
 * Permits unauthenticated access to:
 *   - /actuator/health
 *   - /wxpay/notify (WeChat server-to-server callback;
 *     protected by Nginx IP allowlist at the edge)
 *   - /api/auth/login, /api/auth/sms (login flow)
 *
 * Everything else requires a valid Bearer token. The
 * JwtTokenAuthFilter translates the header into a
 * TokenAuthentication; the controller's Principal parameter
 * comes from the same via PrincipalArgumentResolver.
 */
@Configuration
public class AuthSecurityConfig {

    @Bean
    public JwtTokenAuthFilter jwtTokenAuthFilter(TokenBridge bridge) {
        return new JwtTokenAuthFilter(bridge);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                            JwtTokenAuthFilter jwtFilter) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/actuator/**",
                                "/wxpay/notify",
                                "/api/auth/login",
                                "/api/auth/sms",
                                "/api/auth/refresh"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write(
                                "{\"error\":\"UNAUTHENTICATED\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write(
                                "{\"error\":\"FORBIDDEN\"}");
                        }));
        return http.build();
    }

    @Bean
    public WebMvcConfigurer ypatWebMvc(PrincipalArgumentResolver resolver) {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(resolver);
            }
        };
    }
}