package com.example.securedoc.security;

import com.example.securedoc.domain.ApiAuthentication;
import com.example.securedoc.dto.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("methodSecurityService")
public class MethodSecurityService {

    public String getPrincipalId() {
        var auth = (ApiAuthentication) SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            var user = (User) auth.getPrincipal();

            return user.getUserId();
        }

        return null;
    }
}
