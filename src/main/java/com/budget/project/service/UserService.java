package com.budget.project.service;

import com.budget.project.exception.InternalServerError;
import com.budget.project.model.db.User;
import com.budget.project.service.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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
            throw new InternalServerError();
        }
        return userOptional.get();
    }
}
