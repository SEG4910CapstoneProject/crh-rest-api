package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.UserTagEntity;
import me.t65.reportgenapi.db.postgres.repository.UserTagRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
public class UserTagRepositoryStub implements UserTagRepository {

    @Override
    public List<UserTagEntity> findByUserId(Long userId) {
        return Collections.emptyList();
    }

    @Override
    public boolean existsByUserIdAndTagName(Long userId, String tagName) {
        return false;
    }

    @Override
    public Optional<UserTagEntity> findByUserIdAndTagId(Long userId, Long tagId) {
        return Optional.empty();
    }

    // --- Required JpaRepository methods below ---

    @Override
    public void flush() {}

    @Override
    public <S extends UserTagEntity> S saveAndFlush(S entity) {
        return entity;
    }

    @Override
    public <S extends UserTagEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return Collections.emptyList();
    }

    @Override
    public void deleteAllInBatch(Iterable<UserTagEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public UserTagEntity getOne(Long id) {
        return null;
    }

    @Override
    public UserTagEntity getById(Long id) {
        return null;
    }

    @Override
    public UserTagEntity getReferenceById(Long id) {
        return null;
    }

    @Override
    public <S extends UserTagEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends UserTagEntity> List<S> findAll(Example<S> example) {
        return Collections.emptyList();
    }

    @Override
    public <S extends UserTagEntity> List<S> findAll(Example<S> example, Sort sort) {
        return Collections.emptyList();
    }

    @Override
    public <S extends UserTagEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public <S extends UserTagEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends UserTagEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends UserTagEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends UserTagEntity> S save(S entity) {
        return entity;
    }

    @Override
    public <S extends UserTagEntity> List<S> saveAll(Iterable<S> entities) {
        return Collections.emptyList();
    }

    @Override
    public Optional<UserTagEntity> findById(Long id) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        return false;
    }

    @Override
    public List<UserTagEntity> findAll() {
        return Collections.emptyList();
    }

    @Override
    public List<UserTagEntity> findAllById(Iterable<Long> ids) {
        return Collections.emptyList();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long id) {}

    @Override
    public void delete(UserTagEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {}

    @Override
    public void deleteAll(Iterable<? extends UserTagEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<UserTagEntity> findAll(Sort sort) {
        return Collections.emptyList();
    }

    @Override
    public Page<UserTagEntity> findAll(Pageable pageable) {
        return Page.empty();
    }
}
