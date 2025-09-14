package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "monthly_articles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MonthlyArticlesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "article_id", nullable = false)
    private UUID articleId;

    @Column(name = "date_published", nullable = false)
    private LocalDate datePublished;

    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    @Column(name = "is_article_of_note", nullable = false)
    private boolean isArticleOfNote = false;

    @Column(name = "title", nullable = false)
    private String title;

    public void incrementViewCount() {
        this.viewCount++;
    }
}
