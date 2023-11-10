package com.budget.project.auth.service;

import com.budget.project.auth.model.dto.AuthenticationResponse;
import com.budget.project.exception.AppException;
import com.budget.project.model.db.Role;
import com.budget.project.model.db.User;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.security.JwtService;
import com.budget.project.service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @SneakyThrows
    public AuthenticationResponse register(AuthenticationRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new AppException(
                    "user with email: " + request.email() + " already exists", HttpStatus.CONFLICT);
        }
        User user = new User();
        user = user.toBuilder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .hash(UUID.randomUUID().toString())
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);

        return new AuthenticationResponse(jwtToken);
    }
}
