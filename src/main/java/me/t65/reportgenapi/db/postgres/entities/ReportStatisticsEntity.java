package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import me.t65.reportgenapi.db.postgres.entities.id.ReportStatisticsId;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "report_statistics")
public class ReportStatisticsEntity {

    @EmbeddedId private ReportStatisticsId reportStatisticsId;

    @Column(name = "suggestion")
    private boolean suggestion;
}
