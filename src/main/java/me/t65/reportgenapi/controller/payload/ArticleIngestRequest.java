package me.t65.reportgenapi.controller.payload;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for ingesting a new article by URL")
public class ArticleIngestRequest {

    @Schema(description = "Title of the article", example = "Massive Data Breach Affects Millions")
    private String title;

    @Schema(description = "Direct link (URL) to the article", example = "https://www.cshub.com/threat-defense/articles/cyber-security-experts-fight-ai-generated-threats")
    private String link;

    @Schema(description = "Short summary or description of the article", example = "This article discusses the increasing use of AI in cyberattacks and defensive measures.")
    private String description;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
