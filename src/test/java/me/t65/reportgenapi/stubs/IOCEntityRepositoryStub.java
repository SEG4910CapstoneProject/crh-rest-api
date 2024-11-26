package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.IOCEntity;
import me.t65.reportgenapi.db.postgres.repository.IOCEntityRepository;

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
public class IOCEntityRepositoryStub implements IOCEntityRepository {
    @Override
    public void flush() {}

    @Override
    public <S extends IOCEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends IOCEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<IOCEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public IOCEntity getOne(Integer integer) {
        return null;
    }

    @Override
    public IOCEntity getById(Integer integer) {
        return null;
    }

    @Override
    public IOCEntity getReferenceById(Integer integer) {
        return null;
    }

    @Override
    public <S extends IOCEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends IOCEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends IOCEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends IOCEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends IOCEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends IOCEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends IOCEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends IOCEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends IOCEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<IOCEntity> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public List<IOCEntity> findAll() {
        return null;
    }

    @Override
    public List<IOCEntity> findAllById(Iterable<Integer> integers) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {}

    @Override
    public void delete(IOCEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {}

    @Override
    public void deleteAll(Iterable<? extends IOCEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<IOCEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<IOCEntity> findAll(Pageable pageable) {
        return null;
    }
}
