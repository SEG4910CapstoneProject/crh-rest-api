package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;

@lombok.Getter
@lombok.Setter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "report_articles")
public class ReportArticlesEntity {

    @EmbeddedId private ReportArticlesId reportArticlesId;

    @Column(name = "article_rank")
    private short articleRank;

    @Column(name = "suggestion")
    private boolean suggestion;
}
