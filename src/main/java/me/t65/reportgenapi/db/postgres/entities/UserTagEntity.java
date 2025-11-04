package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "user_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTagEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "tag_name", nullable = false)
    private String tagName;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
