package me.t65.reportgenapi.db.postgres.entities;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import me.t65.reportgenapi.db.postgres.entities.id.IOCArticlesId;

@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Entity
@Table(name = "ioc_articles")
public class IOCArticlesEntity {
    @EmbeddedId private IOCArticlesId iocArticlesId;
}
