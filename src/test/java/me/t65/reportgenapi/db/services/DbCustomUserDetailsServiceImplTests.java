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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class DbCustomUserDetailsServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private DbCustomUserDetailsServiceImpl dbCustomUserDetailsServiceImpl;

    private final String TEST_EMAIL = "test@user.com";
    private final String HASHED_PASSWORD = "$2a$10$hashedpassword";

    @Test
    void testLoadUserByUsername_success_returnsUserDetails() {
        UserEntity mockUserEntity = new UserEntity();
        mockUserEntity.setEmail(TEST_EMAIL);
        mockUserEntity.setPasswordHash(HASHED_PASSWORD);

        // Mock the underlying repository call
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockUserEntity));

        // Execute the method
        UserDetails userDetails = dbCustomUserDetailsServiceImpl.loadUserByUsername(TEST_EMAIL);

        // Assertions
        assertNotNull(userDetails);
        assertEquals(TEST_EMAIL, userDetails.getUsername());
        assertEquals(HASHED_PASSWORD, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().isEmpty());
        // Verify the repository method was called
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void testLoadUserByUsername_userNotFound_throwsException() {
        // Mock the underlying repository call to return empty
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Assert that the specific Spring Security exception is thrown
        UsernameNotFoundException thrown =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> dbCustomUserDetailsServiceImpl.loadUserByUsername(TEST_EMAIL),
                        "Expected UsernameNotFoundException to be thrown, but it wasn't");

        // Verify exception message contains the email
        assertTrue(thrown.getMessage().contains(TEST_EMAIL));
        // Verify the repository method was called
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }
}
