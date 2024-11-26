package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.CategoryEntity;
import me.t65.reportgenapi.db.postgres.repository.CategoryRepository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public class CategoryRepositoryStub implements CategoryRepository {
    @Override
    public void flush() {}

    @Override
    public <S extends CategoryEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends CategoryEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<CategoryEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public CategoryEntity getOne(Integer integer) {
        return null;
    }

    @Override
    public CategoryEntity getById(Integer integer) {
        return null;
    }

    @Override
    public CategoryEntity getReferenceById(Integer integer) {
        return null;
    }

    @Override
    public <S extends CategoryEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends CategoryEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends CategoryEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends CategoryEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends CategoryEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends CategoryEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends CategoryEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends CategoryEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends CategoryEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<CategoryEntity> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public List<CategoryEntity> findAll() {
        return null;
    }

    @Override
    public List<CategoryEntity> findAllById(Iterable<Integer> integers) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {}

    @Override
    public void delete(CategoryEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {}

    @Override
    public void deleteAll(Iterable<? extends CategoryEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<CategoryEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<CategoryEntity> findAll(Pageable pageable) {
        return null;
    }
}
