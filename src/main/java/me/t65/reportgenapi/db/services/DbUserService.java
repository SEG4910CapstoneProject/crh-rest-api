package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;

import java.util.Optional;

public interface DbUserService {
    UserEntity register(String email, String rawPassword, String role);
    Optional<UserEntity> login(String email, String rawPassword);
    Optional<UserEntity> getUserByEmail(String email);
}
