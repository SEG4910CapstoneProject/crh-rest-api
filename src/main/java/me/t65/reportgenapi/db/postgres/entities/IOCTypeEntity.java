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
@Table(name = "ioc_types")
public class IOCTypeEntity {
    @Id
    @Column(name = "ioc_type_ID")
    private int iocTypeId;

    @Column(name = "name")
    private String name;
}
