package com.budget.project.auth.service;

import com.budget.project.auth.model.db.RefreshToken;
import com.budget.project.auth.service.repository.RefreshTokenRepository;
import com.budget.project.exception.AppException;
import com.budget.project.service.UserService;
import jakarta.servlet.http.Cookie;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenDurationMs;

    @Value("${jwt.refresh.cookie}")
    private String JWT_REFRESH_COOKIE;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @SneakyThrows
    public RefreshToken findByToken(String token) {
        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(token)
                        .orElseThrow(
                                () -> {
                                    log.debug("can not find refresh token");
                                    return new AppException(
                                            "Can't find refresh token", HttpStatus.FORBIDDEN);
                                });
        verifyExpiration(refreshToken);
        return refreshToken;
    }

    @Transactional
    public Cookie createRefreshTokenCookieAndRemoveOld() {
        refreshTokenRepository.deleteByUser(userService.getLoggedUser());
        RefreshToken refreshToken =
                refreshTokenRepository.save(
                        RefreshToken.builder()
                                .user(userService.getLoggedUser())
                                .expiryDate(Instant.now().plusMillis(refreshTokenDurationMs))
                                .token(UUID.randomUUID().toString())
                                .build());
        return getCookie(refreshToken.getToken());
    }

    public Cookie getCookie(String refreshToken) {
        Cookie cookie = new Cookie(JWT_REFRESH_COOKIE, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }

    @SneakyThrows
    public void verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new AppException(
                    "Refresh token was expired. Please make a new signin request",
                    HttpStatus.FORBIDDEN);
        }
    }
}
