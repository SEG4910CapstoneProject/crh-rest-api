package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;

@lombok.Getter
@lombok.Setter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role", nullable = false)
    private String role; // "admin", "analyst", "restricted_analyst"

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
