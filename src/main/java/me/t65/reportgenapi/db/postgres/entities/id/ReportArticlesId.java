package me.t65.reportgenapi.db.postgres.entities.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@lombok.Setter
@lombok.Builder
@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Embeddable
public class ReportArticlesId implements Serializable {
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "article_id")
    private UUID articleId;
}
