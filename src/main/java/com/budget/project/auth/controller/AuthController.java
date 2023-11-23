package com.budget.project.auth.controller;

import com.budget.project.auth.model.dto.AuthInput;
import com.budget.project.auth.model.dto.JwtResponse;
import com.budget.project.auth.service.AuthService;
import com.budget.project.auth.service.RefreshTokenService;
import com.budget.project.exception.AppException;
import com.budget.project.security.JwtService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final HttpServletResponse httpServletResponse;
    private final HttpServletRequest httpServletRequest;

    @Value("${jwt.refresh.cookie}")
    String refreshTokenName;

    @MutationMapping
    public JwtResponse register(@Argument AuthInput authInput) {
        return authService.register(authInput);
    }

    @MutationMapping
    public JwtResponse authenticate(@Argument AuthInput authInput) {
        var jwtResponse = authService.authenticate(authInput);
        httpServletResponse.addCookie(refreshTokenService.createRefreshTokenCookieAndRemoveOld());
        return jwtResponse;
    }

    @MutationMapping
    public JwtResponse refreshToken() {
        if (Objects.isNull(httpServletRequest.getCookies())) {
            throw new AppException("can't find refreesh cookie", HttpStatus.BAD_REQUEST);
        }
        Optional<Cookie> refreshCookie = Arrays.stream(httpServletRequest.getCookies())
                .filter(c -> c.getName().equals(refreshTokenName))
                .findFirst();
        String refreshToken = refreshCookie.map(Cookie::getValue).orElse(null);
        String jwt = jwtService.generateToken(
                refreshTokenService.findByToken(refreshToken).getUser());
        httpServletResponse.addCookie(refreshTokenService.getCookie(refreshToken));
        return new JwtResponse(jwt);
    }

    @MutationMapping
    public boolean logout() {
        refreshTokenService.deleteTokenForLoggedUser();
        httpServletResponse.addCookie(refreshTokenService.getLogoutCookie());
        return true;
    }
}
