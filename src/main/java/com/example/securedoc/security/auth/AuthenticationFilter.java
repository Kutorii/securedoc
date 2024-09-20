package com.example.securedoc.security.auth;

import com.example.securedoc.domain.ApiAuthentication;
import com.example.securedoc.domain.Response;
import com.example.securedoc.dto.User;
import com.example.securedoc.request.LoginRequest;
import com.example.securedoc.service.JwtService;
import com.example.securedoc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

import static com.example.securedoc.constant.Constants.LOGIN_PATH;
import static com.example.securedoc.enumeration.LoginType.LOGIN_ATTEMPT;
import static com.example.securedoc.enumeration.LoginType.LOGIN_SUCCESSFUL;
import static com.example.securedoc.enumeration.TokenType.*;
import static com.example.securedoc.utils.RequestUtils.getResponse;
import static com.example.securedoc.utils.RequestUtils.handleErrorResponse;
import static com.example.securedoc.utils.RequestUtils.writeResponse;
import static com.fasterxml.jackson.core.JsonParser.Feature.AUTO_CLOSE_SOURCE;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class AuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private final UserService userService;
    private final JwtService jwtService;

    public AuthenticationFilter(AuthenticationManager authenticationManager, UserService userService, JwtService jwtService) {
        super(new AntPathRequestMatcher(LOGIN_PATH, POST.name()), authenticationManager);
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        try {
            var user = new ObjectMapper().configure(AUTO_CLOSE_SOURCE, true).readValue(request.getInputStream(), LoginRequest.class);
            userService.updateLoginAttempt(user.getEmail(), LOGIN_ATTEMPT);

            var authentication = ApiAuthentication.unauthenticated(user.getEmail(), user.getPassword());
            return getAuthenticationManager().authenticate(authentication);
        } catch (Exception e) {
            log.error(e.getMessage());
            handleErrorResponse(request, response, e);
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        var user = (User) authResult.getPrincipal();
        userService.updateLoginAttempt(user.getEmail(), LOGIN_SUCCESSFUL);

        var httpResponse = user.isMfa() ? sendQrcode(request, user) : sendResponse(request, response, user);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setStatus(OK.value());

        if (user.isMfa()) {
            request.getSession().setAttribute("MFA_AUTH", authResult);
            writeResponse.accept(response, httpResponse);
//            response.sendRedirect("/user/login/mfa-verify");
        } else {
            SecurityContextHolder.getContext().setAuthentication(authResult);
            writeResponse.accept(response, httpResponse);
        }
    }

    private Response sendQrcode(HttpServletRequest request, User user) {
        return getResponse(request, Map.of("user", user), "Please enter QR Code", OK);
    }

    private Response sendResponse(HttpServletRequest request, HttpServletResponse response, User user) {
        jwtService.addCookie(response, user, ACCESS);
        jwtService.addCookie(response, user, REFRESH);

        return getResponse(request, Map.of("user", user), "Login successful", OK);
    }
}
