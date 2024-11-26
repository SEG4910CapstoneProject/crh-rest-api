package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.IOCTypeEntity;
import me.t65.reportgenapi.db.postgres.repository.IOCTypeEntityRepository;

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
public class IOCTypeEntityRepositoryStub implements IOCTypeEntityRepository {
    @Override
    public void flush() {}

    @Override
    public <S extends IOCTypeEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends IOCTypeEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<IOCTypeEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public IOCTypeEntity getOne(Integer integer) {
        return null;
    }

    @Override
    public IOCTypeEntity getById(Integer integer) {
        return null;
    }

    @Override
    public IOCTypeEntity getReferenceById(Integer integer) {
        return null;
    }

    @Override
    public <S extends IOCTypeEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends IOCTypeEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends IOCTypeEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends IOCTypeEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends IOCTypeEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends IOCTypeEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends IOCTypeEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends IOCTypeEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends IOCTypeEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<IOCTypeEntity> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public List<IOCTypeEntity> findAll() {
        return null;
    }

    @Override
    public List<IOCTypeEntity> findAllById(Iterable<Integer> integers) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {}

    @Override
    public void delete(IOCTypeEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {}

    @Override
    public void deleteAll(Iterable<? extends IOCTypeEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<IOCTypeEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<IOCTypeEntity> findAll(Pageable pageable) {
        return null;
    }
}
