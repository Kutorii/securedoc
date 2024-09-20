package com.example.securedoc.security;

import com.example.securedoc.security.auth.AuthenticationFilter;
import com.example.securedoc.security.jwt.JwtAuthenticationFilter;
import com.example.securedoc.service.JwtService;
import com.example.securedoc.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.http.HttpMethod.*;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@RequiredArgsConstructor
@Configuration
public class FilterChainConfiguration {
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @Bean
    public RequestMatcher publicEndPointMatcher() {
        return new OrRequestMatcher(
                new AntPathRequestMatcher("/user/login", POST.name()),
                new AntPathRequestMatcher("/user/login/mfa-verify", POST.name()),
                new AntPathRequestMatcher("/user/logout", GET.name()),
                new AntPathRequestMatcher("/user/register", POST.name()),
                new AntPathRequestMatcher("/user/refresh-token", GET.name()),
                new AntPathRequestMatcher("/user/reset-password", GET.name()),
                new AntPathRequestMatcher("/user/verify/account", GET.name()),
                new AntPathRequestMatcher("/user/verify/password", GET.name()),
                new AntPathRequestMatcher("/user/verify/password", POST.name())
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        .requestMatchers(publicEndPointMatcher()).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/user/profile/**", POST.name())).hasAuthority("user:update")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .addFilterAt(new AuthenticationFilter(authenticationManager, userService, jwtService), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), AuthenticationFilter.class)
                .build();
    }
}
