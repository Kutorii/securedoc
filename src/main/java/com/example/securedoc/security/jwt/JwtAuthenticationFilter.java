package com.example.securedoc.security.jwt;

import com.example.securedoc.domain.ApiAuthentication;
import com.example.securedoc.domain.TokenData;
import com.example.securedoc.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.example.securedoc.enumeration.TokenType.*;
import static com.example.securedoc.utils.RequestUtils.handleErrorResponse;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            var optionalJwtToken = jwtService.extractToken(request, ACCESS.getValue());
            if (optionalJwtToken.isPresent()) {
                var context = SecurityContextHolder.getContext();
                var jwtToken = optionalJwtToken.get();
                if (context.getAuthentication() == null) {
                    if (jwtService.getTokenData(jwtToken, TokenData::isValid)) {
                        var apiAuth = ApiAuthentication.authenticated(
                                jwtService.getTokenData(jwtToken, TokenData::getUser),
                                jwtService.getTokenData(jwtToken, TokenData::getAuthorities)
                        );
                        apiAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        context.setAuthentication(apiAuth);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            handleErrorResponse(request, response, e);
        }
    }
}
