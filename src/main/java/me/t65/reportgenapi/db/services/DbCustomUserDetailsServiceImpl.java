package me.t65.reportgenapi.db.services;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collections;

@Service
public class DbCustomUserDetailsServiceImpl implements UserDetailsService {

    private final DbUserService dbUserService;

    // Constructor injection
    public DbCustomUserDetailsServiceImpl(DbUserService dbUserService) {
        this.dbUserService = dbUserService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = dbUserService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                user.getEmail(),
                user.getPasswordHash(), // Use the HASHED password from your entity
                Collections.emptyList() // Placeholder for roles/authorities
        );
    }
}