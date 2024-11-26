package me.t65.reportgenapi.db.mongo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@lombok.Getter
@lombok.Setter
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
@lombok.ToString
@lombok.EqualsAndHashCode
@Document(collection = "articleContent")
public class ArticleContentEntity {
    @Id private UUID id;
    private String link;
    private String name;
    private Instant date;
    private String description;
}
