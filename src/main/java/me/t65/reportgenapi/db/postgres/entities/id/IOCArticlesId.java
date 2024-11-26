package me.t65.reportgenapi.db.postgres.entities.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@lombok.Setter
@lombok.Builder
@lombok.Getter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Embeddable
public class IOCArticlesId {
    @Column(name = "ioc_ID")
    private int iocID;

    @Column(name = "article_ID")
    private UUID articleId;
}
