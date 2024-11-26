package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.*;

import java.util.UUID;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "statistics")
public class StatisticEntity {

    @Id
    @Column(name = "statistic_ID", columnDefinition = "uuid")
    private UUID statisticId;

    @Column(name = "statistic_number")
    private Integer statisticNumber;

    @Column(name = "title")
    private String title;

    @Column(name = "subtitle")
    private String subtitle;
}
