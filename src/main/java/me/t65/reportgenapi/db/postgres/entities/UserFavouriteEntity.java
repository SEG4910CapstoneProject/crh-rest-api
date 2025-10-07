package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * Represents a favourite article entry for a specific user.
 * Each row links one user to one article.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "user_favourites")
@IdClass(UserFavouriteEntity.PK.class)
public class UserFavouriteEntity {

    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Id
    @Column(name = "article_id", columnDefinition = "uuid", nullable = false)
    private UUID articleId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    /** Composite key (user_id + article_id) */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long userId;
        private UUID articleId;
    }
}
