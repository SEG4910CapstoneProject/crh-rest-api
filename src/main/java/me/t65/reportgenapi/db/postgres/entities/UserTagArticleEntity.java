package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "user_tag_articles")
@IdClass(UserTagArticleEntity.PK.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTagArticleEntity implements Serializable {

    @Id
    @Column(name = "tag_id", nullable = false)
    private Long tagId;

    @Id
    @Column(name = "article_id", columnDefinition = "uuid", nullable = false)
    private UUID articleId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        private Long tagId;
        private UUID articleId;
    }
}
