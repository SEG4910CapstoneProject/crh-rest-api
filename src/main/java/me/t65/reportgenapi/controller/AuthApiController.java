package me.t65.reportgenapi.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.Operation;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;
import me.t65.reportgenapi.db.services.DbUserService;
import me.t65.reportgenapi.utils.JwtUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin(origins = "http://localhost:4200")
@ConditionalOnProperty(name = "feature.auth.enabled", havingValue = "true", matchIfMissing = true)
public class AuthApiController {

    private final DbUserService userService;
    private final JwtUtils jwtUtils;

    @Value("${spring.jwt.secret}")
    private String jwtSecret;

    @Value("${spring.jwt.expiration.ms:86400000}")
    private long jwtExpirationMs;

    @Autowired
    public AuthApiController(DbUserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Login user")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");
        System.out.println("DEBUG: Login attempt → email=" + email);

        try {
            Optional<UserEntity> userOptional = userService.login(email, password);
            if (userOptional.isPresent()) {
                UserEntity user = userOptional.get();
                System.out.println("DEBUG: Password check passed for " + email);

                String token =
                        Jwts.builder()
                                .setSubject(user.getEmail())
                                .claim("userId", user.getUserId())
                                .claim("role", user.getRole())
                                .setIssuedAt(new Date())
                                .setExpiration(
                                        new Date(System.currentTimeMillis() + jwtExpirationMs))
                                .signWith(jwtUtils.key(), SignatureAlgorithm.HS512)
                                .compact();

                System.out.println("DEBUG: JWT generated for " + email);
                return ResponseEntity.ok(Map.of("token", token));
            } else {
                System.out.println("DEBUG: Invalid credentials for " + email);
                return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Get user by email")
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestParam String email) {
        return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
    }
}
