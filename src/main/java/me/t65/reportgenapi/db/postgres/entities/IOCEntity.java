package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "iocs")
public class IOCEntity {
    @Id
    @Column(name = "ioc_ID")
    private int iocID;

    @Column(name = "ioc_type")
    private int iocTypeId;

    @Column(name = "ioc_value")
    private String value;
}
