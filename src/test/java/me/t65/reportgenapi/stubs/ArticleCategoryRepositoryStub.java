package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.ArticleCategoryEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ArticleCategoryId;
import me.t65.reportgenapi.db.postgres.repository.ArticleCategoryRepository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class ArticleCategoryRepositoryStub implements ArticleCategoryRepository {
    @Override
    public List<ArticleCategoryEntity> findByArticleCategoryId_ArticleIdIn(
            Collection<UUID> articleIds) {
        return null;
    }

    @Override
    public void flush() {}

    @Override
    public <S extends ArticleCategoryEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends ArticleCategoryEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<ArticleCategoryEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<ArticleCategoryId> articleCategoryIds) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public ArticleCategoryEntity getOne(ArticleCategoryId articleCategoryId) {
        return null;
    }

    @Override
    public ArticleCategoryEntity getById(ArticleCategoryId articleCategoryId) {
        return null;
    }

    @Override
    public ArticleCategoryEntity getReferenceById(ArticleCategoryId articleCategoryId) {
        return null;
    }

    @Override
    public <S extends ArticleCategoryEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends ArticleCategoryEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends ArticleCategoryEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends ArticleCategoryEntity> Page<S> findAll(
            Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ArticleCategoryEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ArticleCategoryEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends ArticleCategoryEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends ArticleCategoryEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends ArticleCategoryEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<ArticleCategoryEntity> findById(ArticleCategoryId articleCategoryId) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ArticleCategoryId articleCategoryId) {
        return false;
    }

    @Override
    public List<ArticleCategoryEntity> findAll() {
        return null;
    }

    @Override
    public List<ArticleCategoryEntity> findAllById(Iterable<ArticleCategoryId> articleCategoryIds) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(ArticleCategoryId articleCategoryId) {}

    @Override
    public void delete(ArticleCategoryEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends ArticleCategoryId> articleCategoryIds) {}

    @Override
    public void deleteAll(Iterable<? extends ArticleCategoryEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<ArticleCategoryEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<ArticleCategoryEntity> findAll(Pageable pageable) {
        return null;
    }
}
