package me.t65.reportgenapi.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.services.DbUserService;
import me.t65.reportgenapi.utils.JwtUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AuthApiControllerTests {

    private static final String MOCK_JWT_SECRET =
            "test-secret-that-is-at-least-256-bits-long-for-HS512";
    private static final long MOCK_JWT_EXPIRATION_MS = 3600000;

    @Mock private DbUserService userService;
    @Mock private JwtUtils jwtUtils;

    private AuthApiController authApiController;

    @BeforeEach
    public void setup() {
        authApiController = new AuthApiController(userService, jwtUtils);

        ReflectionTestUtils.setField(authApiController, "jwtSecret", MOCK_JWT_SECRET);
        ReflectionTestUtils.setField(authApiController, "jwtExpirationMs", MOCK_JWT_EXPIRATION_MS);

        Key mockKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        // Use lenient() to prevent UnnecessaryStubbingException in tests that don't need this mock
        lenient().when(jwtUtils.key()).thenReturn(mockKey);
    }

    private UserEntity createMockUser() {
        UserEntity user = new UserEntity();
        user.setEmail("test@example.com");
        user.setUserId(1234L);
        user.setRole("USER");
        return user;
    }

    @Test
    public void testLogin_success() {
        UserEntity mockUser = createMockUser();
        when(userService.login(eq("user@test.com"), eq("password123")))
                .thenReturn(Optional.of(mockUser));

        Map<String, String> loginRequest =
                Map.of("email", "user@test.com", "password", "password123");

        ResponseEntity<?> actual = authApiController.login(loginRequest);

        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertTrue(actual.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) actual.getBody();
        assertTrue(responseBody.containsKey("token"));
        verify(userService, times(1)).login(anyString(), anyString());
    }

    @Test
    public void testLogin_failure_invalidCredentials() {
        when(userService.login(anyString(), anyString())).thenReturn(Optional.empty());

        Map<String, String> loginRequest = Map.of("email", "bad@test.com", "password", "wrongpass");

        ResponseEntity<?> actual = authApiController.login(loginRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, actual.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) actual.getBody();
        assertEquals("Invalid credentials", responseBody.get("error"));
        verify(userService, times(1)).login(anyString(), anyString());
    }

    @Test
    public void testLogin_failure_missingFields() {
        // GIVEN a request missing the password, which results in null being passed to the service
        Map<String, String> loginRequest = Map.of("email", "user@test.com");

        // WHEN the service is called with 'null' password, it returns empty (failure)
        when(userService.login(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<?> actual = authApiController.login(loginRequest);

        // THEN the response is 401, and we verify the service was called once (with null password)
        assertEquals(HttpStatus.UNAUTHORIZED, actual.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) actual.getBody();
        assertEquals("Invalid credentials", responseBody.get("error"));

        verify(userService, times(1)).login(eq("user@test.com"), eq(null));
    }

    @Test
    public void testLogin_internalServerError() {
        String errorMessage = "Database connection failed";
        when(userService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException(errorMessage));

        Map<String, String> loginRequest = Map.of("email", "error@test.com", "password", "anypass");

        ResponseEntity<?> actual = authApiController.login(loginRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actual.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) actual.getBody();
        assertEquals(errorMessage, responseBody.get("error"));
        verify(userService, times(1)).login(anyString(), anyString());
    }

    @Test
    public void testMe_alwaysReturnsForbidden() {
        String email = "test@example.com";

        ResponseEntity<?> actual = authApiController.me(email);

        assertEquals(HttpStatus.FORBIDDEN, actual.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) actual.getBody();
        assertEquals("Access denied", responseBody.get("error"));
        verifyNoInteractions(userService);
    }
}
