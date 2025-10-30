package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.postgres.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class DbCustomUserDetailsServiceImpl implements UserDetailsService {

    // --- Dependency changed from DbUserService to the UserRepository to break the cycle ---
    private final UserRepository userRepository;

    @Autowired
    public DbCustomUserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Use the repository directly instead of calling a method on DbUserService
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);

        UserEntity user =
                userOptional.orElseThrow(
                        () -> new UsernameNotFoundException("User not found with email: " + email));

        // NOTE on authorities:
        // You are currently passing Collections.emptyList(). If your users have roles (e.g., ADMIN,
        // USER),
        // you should retrieve those and wrap them in SimpleGrantedAuthority here.
        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList() // Placeholder for roles/authorities
                );
    }
}
