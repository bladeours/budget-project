package com.budget.project.service;

import com.budget.project.exception.AppException;
import com.budget.project.model.db.User;
import com.budget.project.service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    @SneakyThrows
    public User getLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userRepository.findByEmail(authentication.getName());
        if (userOptional.isEmpty()) {
            log.warn("there is no user with mail: {}", authentication.getName());
            throw new AppException("You don't have access", HttpStatus.UNAUTHORIZED);
        }
        return userOptional.get();
    }
}
