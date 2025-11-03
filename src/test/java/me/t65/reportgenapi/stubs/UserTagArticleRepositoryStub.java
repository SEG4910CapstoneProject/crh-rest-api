package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.UserTagArticleEntity;
import me.t65.reportgenapi.db.postgres.repository.UserTagArticleRepository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.UUID;
import java.util.function.Function;

@Component
public class UserTagArticleRepositoryStub implements UserTagArticleRepository {

    @Override
    public List<UserTagArticleEntity> findByTagId(Long tagId) {
        return Collections.emptyList();
    }

    @Override
    public List<UserTagArticleEntity> findByArticleId(UUID articleId) {
        return Collections.emptyList();
    }

    @Override
    public void deleteByTagId(Long tagId) {}

    // --- Required JpaRepository methods below ---

    @Override
    public void flush() {}

    @Override
    public <S extends UserTagArticleEntity> S saveAndFlush(S entity) {
        return entity;
    }

    @Override
    public <S extends UserTagArticleEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return Collections.emptyList();
    }

    @Override
    public void deleteAllInBatch(Iterable<UserTagArticleEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<UserTagArticleEntity.PK> pks) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public UserTagArticleEntity getOne(UserTagArticleEntity.PK pk) {
        return null;
    }

    @Override
    public UserTagArticleEntity getById(UserTagArticleEntity.PK pk) {
        return null;
    }

    @Override
    public UserTagArticleEntity getReferenceById(UserTagArticleEntity.PK pk) {
        return null;
    }

    @Override
    public <S extends UserTagArticleEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends UserTagArticleEntity> List<S> findAll(Example<S> example) {
        return Collections.emptyList();
    }

    @Override
    public <S extends UserTagArticleEntity> List<S> findAll(Example<S> example, Sort sort) {
        return Collections.emptyList();
    }

    @Override
    public <S extends UserTagArticleEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public <S extends UserTagArticleEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends UserTagArticleEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends UserTagArticleEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends UserTagArticleEntity> S save(S entity) {
        return entity;
    }

    @Override
    public <S extends UserTagArticleEntity> List<S> saveAll(Iterable<S> entities) {
        return Collections.emptyList();
    }

    @Override
    public Optional<UserTagArticleEntity> findById(UserTagArticleEntity.PK pk) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(UserTagArticleEntity.PK pk) {
        return false;
    }

    @Override
    public List<UserTagArticleEntity> findAll() {
        return Collections.emptyList();
    }

    @Override
    public List<UserTagArticleEntity> findAllById(Iterable<UserTagArticleEntity.PK> pks) {
        return Collections.emptyList();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(UserTagArticleEntity.PK pk) {}

    @Override
    public void delete(UserTagArticleEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends UserTagArticleEntity.PK> pks) {}

    @Override
    public void deleteAll(Iterable<? extends UserTagArticleEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<UserTagArticleEntity> findAll(Sort sort) {
        return Collections.emptyList();
    }

    @Override
    public Page<UserTagArticleEntity> findAll(Pageable pageable) {
        return Page.empty();
    }
}
