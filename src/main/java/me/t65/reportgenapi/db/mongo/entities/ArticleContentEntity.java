package me.t65.reportgenapi.db.mongo.entities;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@lombok.Getter
@lombok.Setter
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

    @Field("clean_full_text")
    private String cleanFullText;

    // This is the vector itself (768 doubles)
    @Field("embedding")
    private List<Double> embedding;

    public ArticleContentEntity(
            UUID id, String link, String name, Instant date, String description) {
        this.id = id;
        this.link = link;
        this.name = name;
        this.date = date;
        this.description = description;
    }
}
