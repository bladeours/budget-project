package com.budget.project.auth.service;

import com.budget.project.model.db.Role;
import com.budget.project.model.db.User;
import com.budget.project.model.dto.request.AuthenticationRequest;
import com.budget.project.model.dto.response.AuthenticationResponse;
import com.budget.project.security.JwtService;
import com.budget.project.service.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(AuthenticationRequest request) {
        User user =
                User.builder()
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
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email()).orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return new AuthenticationResponse(jwtToken);
    }
}
