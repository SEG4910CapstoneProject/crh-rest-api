package me.t65.reportgenapi.utils;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.postgres.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No authenticated user found");
        }

        String email = auth.getName();
        return userRepository
                .findByEmail(email)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Authenticated user not found in database"));
    }
}
