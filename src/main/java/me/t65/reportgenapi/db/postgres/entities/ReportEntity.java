package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "report")
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "generate_date")
    private Instant generateDate;

    @Enumerated
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "report_type")
    private ReportType reportType;

    @Column(name = "last_modified")
    private Instant lastModified;

    @Column(name = "email_status")
    private Boolean emailStatus;
}
