package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Table(name = "report")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "generate_date", nullable = false)
    private Instant generateDate;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "last_modified", nullable = false)
    private Instant lastModified;

    @Column(name = "email_status", nullable = false)
    private Boolean emailStatus = false; // Default to false
}
