package me.t65.reportgenapi.db.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.postgres.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DbUserServiceImplTests {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private DbUserServiceImpl dbUserServiceImpl;

    @Test
    void testRegister_success_encodesAndSavesUser() {
        String email = "test@example.com";
        String rawPassword = "securepassword";
        String role = "ADMIN";
        String hashedPassword = "$2a$10$encodedhashvalue";

        UserEntity userToSave = new UserEntity();
        userToSave.setEmail(email);
        userToSave.setPasswordHash(hashedPassword);
        userToSave.setRole(role);

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userToSave);

        UserEntity result = dbUserServiceImpl.register(email, rawPassword, role);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(hashedPassword, result.getPasswordHash());
        assertEquals(role, result.getRole());

        verify(passwordEncoder, times(1)).encode(rawPassword);
        verify(userRepository, times(1))
                .save(
                        argThat(
                                user ->
                                        user.getEmail().equals(email)
                                                && user.getPasswordHash().equals(hashedPassword)));
    }

    @Test
    void testLogin_success_passwordMatches() {
        String email = "login@example.com";
        String rawPassword = "correctPassword";
        String hashedPassword = "$2a$10$encodedhashvalue";

        UserEntity foundUser = new UserEntity();
        foundUser.setEmail(email);
        foundUser.setPasswordHash(hashedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);

        Optional<UserEntity> result = dbUserServiceImpl.login(email, rawPassword);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
    }

    @Test
    void testLogin_failure_passwordDoesNotMatch() {
        String email = "login@example.com";
        String rawPassword = "wrongPassword";
        String hashedPassword = "$2a$10$encodedhashvalue";

        UserEntity foundUser = new UserEntity();
        foundUser.setEmail(email);
        foundUser.setPasswordHash(hashedPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(foundUser));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        Optional<UserEntity> result = dbUserServiceImpl.login(email, rawPassword);

        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
    }

    @Test
    void testLogin_failure_userNotFound() {
        String email = "unknown@example.com";
        String rawPassword = "anyPassword";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<UserEntity> result = dbUserServiceImpl.login(email, rawPassword);

        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void testGetUserByEmail_userFound_returnsOptionalUser() {
        String email = "get@example.com";
        UserEntity expectedUser = new UserEntity();
        expectedUser.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        Optional<UserEntity> result = dbUserServiceImpl.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void testGetUserByEmail_userNotFound_returnsEmptyOptional() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<UserEntity> result = dbUserServiceImpl.getUserByEmail(email);

        assertTrue(result.isEmpty());

        verify(userRepository, times(1)).findByEmail(email);
    }
}
