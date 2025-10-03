package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.postgres.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DbUserServiceImpl implements DbUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DbUserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity register(String email, String rawPassword, String role) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public Optional<UserEntity> login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> {
                    boolean matches = passwordEncoder.matches(rawPassword, user.getPasswordHash());
                    System.out.println("DEBUG: Checking password for " + email + " → " + matches);
                    return matches;
                });
    }

    @Override
    public Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
