package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import java.sql.Types;
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

    @Column(name = "generate_date", nullable = false, updatable = false)
    private Instant generateDate = Instant.now();

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "report_type", nullable = false)
    private ReportType reportType;

    @Column(name = "last_modified", nullable = false)
    private Instant lastModified = Instant.now();

    @Column(name = "email_status", nullable = false)
    private Boolean emailStatus = false;

    @JdbcTypeCode(Types.VARBINARY)
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "report_pdf", columnDefinition = "BYTEA")
    private byte[] pdfData;

    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)
    @Column(name = "email_type", nullable = false)
    private EmailTemplateType emailType;
}
