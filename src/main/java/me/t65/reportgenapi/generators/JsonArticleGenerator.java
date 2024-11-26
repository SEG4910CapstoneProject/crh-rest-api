package me.t65.reportgenapi.generators;

import me.t65.reportgenapi.controller.payload.JsonArticleReportResponse;
import me.t65.reportgenapi.controller.payload.JsonIocResponse;
import me.t65.reportgenapi.db.mongo.entities.ArticleContentEntity;
import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.CategoryEntity;
import me.t65.reportgenapi.db.postgres.entities.IOCEntity;
import me.t65.reportgenapi.utils.HtmlRemover;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JsonArticleGenerator {

    private final HtmlRemover htmlRemover;

    @Autowired
    public JsonArticleGenerator(HtmlRemover htmlRemover) {
        this.htmlRemover = htmlRemover;
    }

    public JsonArticleReportResponse createJsonArticleFromArticleEntity(
            UUID articleId,
            ArticleContentEntity articleContentEntity,
            ArticlesEntity articlesEntity,
            List<IOCEntity> iocList,
            Map<Integer, String> iocTypeIdToTypeString,
            CategoryEntity categoryEntity) {
        return new JsonArticleReportResponse(
                articleId.toString(),
                articleContentEntity.getName(),
                htmlRemover.unescapeAndRemoveHtml(articleContentEntity.getDescription()),
                categoryEntity != null ? categoryEntity.getCategoryName() : null,
                articleContentEntity.getLink(),
                this.createJsonIocFromIocEntity(iocList, iocTypeIdToTypeString),
                LocalDate.ofInstant(articlesEntity.getDatePublished(), ZoneOffset.UTC));
    }

    public List<JsonIocResponse> createJsonIocFromIocEntity(
            List<IOCEntity> iocList, Map<Integer, String> iocTypeIdToTypeString) {
        if (iocList == null) {
            return Collections.emptyList();
        }
        return iocList.stream()
                .map(
                        ioc ->
                                new JsonIocResponse(
                                        ioc.getIocID(),
                                        ioc.getIocTypeId(),
                                        iocTypeIdToTypeString.get(ioc.getIocTypeId()),
                                        ioc.getValue()))
                .toList();
    }
}
