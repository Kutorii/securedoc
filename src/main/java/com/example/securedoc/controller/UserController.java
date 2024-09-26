package com.example.securedoc.controller;

import com.example.securedoc.domain.ApiAuthentication;
import com.example.securedoc.domain.Response;
import com.example.securedoc.domain.TokenData;
import com.example.securedoc.dto.User;
import com.example.securedoc.request.*;
import com.example.securedoc.service.JwtService;
import com.example.securedoc.service.UserService;
import com.example.securedoc.utils.MfaUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

import static com.example.securedoc.constant.Constants.MFA_AUTH;
import static com.example.securedoc.enumeration.TokenType.ACCESS;
import static com.example.securedoc.enumeration.TokenType.REFRESH;
import static com.example.securedoc.utils.RequestUtils.*;
import static java.util.Collections.emptyMap;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Response> createUser(@RequestBody @Valid RegisterRequest user, HttpServletRequest request) {
        userService.createUser(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword());

        return ResponseEntity.created(getUri()).body(getResponse(request, emptyMap(), "Your account has been created. Check your email to enable your account", CREATED));
    }

    @GetMapping("/verify/account")
    public ResponseEntity<Response> verifyAccount(@RequestParam("key") String key, HttpServletRequest request) {
        userService.verifyAccountKey(key);

        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Account verified", OK));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Response> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest, HttpServletRequest request) {
        userService.forgotPasswordRequest(forgotPasswordRequest.getEmail());

        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "An email has been sent to reset your password", OK));
    }

    @GetMapping("/verify/password")
    public ResponseEntity<Response> verifyForgotPasswordReset(@RequestParam("token") String token, HttpServletRequest request) {
        userService.verifyResetPasswordToken(token);

        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Send form to reset password", OK));
    }

    @PostMapping("/verify/password")
    public ResponseEntity<Response> resetPassword(@RequestParam("token") String token, ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        userService.resetPassword(token, resetPasswordRequest.getPassword());

        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Password reset successful", OK));
    }

    @PostMapping("/login")
    public void loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        authenticationManager.authenticate(ApiAuthentication.unauthenticated(loginRequest.getEmail(), loginRequest.getPassword()));
    }

    @PostMapping("/login/mfa-verify")
    public ResponseEntity<Response> verifyMfa(@RequestBody @Valid MfaRequest mfaRequest, HttpServletRequest request, HttpServletResponse response) {
        var session = request.getSession();
        var auth = (ApiAuthentication) session.getAttribute(MFA_AUTH);
        if (auth == null) {
            return ResponseEntity.status(FORBIDDEN).body(getResponse(request, emptyMap(), "No login process previously triggered", FORBIDDEN));
        }

        var user = (User) auth.getPrincipal();
        if (MfaUtils.isCodeValid(mfaRequest.getCode(), userService.getUserMfaSecretByEmail(user.getEmail()))) {
            session.removeAttribute(MFA_AUTH);
            SecurityContextHolder.getContext().setAuthentication(auth);
            jwtService.addCookie(response, user, ACCESS);
            jwtService.addCookie(response, user, REFRESH);

            return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Login successful", OK));
        } else {
            return ResponseEntity.status(UNAUTHORIZED).body(getResponse(request, emptyMap(), "Invalid MFA code", UNAUTHORIZED));
        }
    }

    @GetMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletRequest request, HttpServletResponse response) {
        jwtService.removeCookie(request, response, ACCESS.getValue());
        jwtService.removeCookie(request, response, REFRESH.getValue());
        SecurityContextHolder.clearContext();
//        request.getSession().invalidate();

        return ResponseEntity.ok().body(getResponse(request, emptyMap(), "Logout successful", OK));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<Response> getNewAccessToken(HttpServletRequest request, HttpServletResponse response) {
        var optionalRefreshToken = jwtService.extractToken(request, REFRESH.getValue());

        if (optionalRefreshToken.isPresent()) {
            var refreshToken = optionalRefreshToken.get();
            if (jwtService.getTokenData(refreshToken, TokenData::isValid)) {
                var user = jwtService.getTokenData(refreshToken, TokenData::getUser);
                // Avoid having multiple access cookies at the same time
                jwtService.removeCookie(request, response, ACCESS.getValue());
                jwtService.addCookie(response, user, ACCESS);

                return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "New access token delivered", OK));
            }
        }

        return ResponseEntity.status(UNAUTHORIZED).body(getResponse(request, emptyMap(), "Not logged in", UNAUTHORIZED));
    }

    @PutMapping("/profile/{userid}")
    @PreAuthorize("#userid == @methodSecurityService.principalId and hasAuthority('user:update')")
    public ResponseEntity<Response> editProfile(@PathVariable("userid") String userid, @RequestBody @Valid ProfileRequest profileRequest, HttpServletRequest request) {
        var user = new ObjectMapper().convertValue(profileRequest, User.class);
        user.setUserId(userid);

        user = userService.updateUser(user);

        return ResponseEntity.ok().body(getResponse(request, Map.of("user", user), "Profile edited successfully", OK));
    }

    private URI getUri() {
        return URI.create("");
    }

//    @GetMapping(value = "/barcode/{secret}", produces = {MediaType.IMAGE_PNG_VALUE})
//    public BufferedImage barcode(@PathVariable String secret) {
//        return MfaUtils.getQRCodeImage(
//                MfaUtils.getQrCodeUri(secret, ""),
//                200, 200
//        );
//    }
}
