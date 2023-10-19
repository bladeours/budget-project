package com.budget.project.auth.controller;

import com.budget.project.auth.model.dto.AuthenticationResponse;
import com.budget.project.auth.service.AuthService;
import com.budget.project.auth.service.RefreshTokenService;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request, HttpServletResponse httpServletResponse) {
        var authResponse = authService.authenticate(request);
        httpServletResponse.addCookie(refreshTokenService.createRefreshTokenCookieAndRemoveOld());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            @CookieValue("${jwt.refresh.cookie}") String refreshToken,
            HttpServletResponse response) {

        String jwt =
                jwtService.generateToken(refreshTokenService.findByToken(refreshToken).getUser());
        response.addCookie(refreshTokenService.getCookie(refreshToken));
        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }
}
