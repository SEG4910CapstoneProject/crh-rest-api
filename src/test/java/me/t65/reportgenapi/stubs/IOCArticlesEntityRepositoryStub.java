package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.IOCArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.id.IOCArticlesId;
import me.t65.reportgenapi.db.postgres.repository.IOCArticlesEntityRepository;

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
public class IOCArticlesEntityRepositoryStub implements IOCArticlesEntityRepository {
    @Override
    public List<IOCArticlesEntity> findByIocArticlesId_ArticleIdIn(Collection<UUID> articleIds) {
        return null;
    }

    @Override
    public void flush() {}

    @Override
    public <S extends IOCArticlesEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends IOCArticlesEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<IOCArticlesEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<IOCArticlesId> iocArticlesIds) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public IOCArticlesEntity getOne(IOCArticlesId iocArticlesId) {
        return null;
    }

    @Override
    public IOCArticlesEntity getById(IOCArticlesId iocArticlesId) {
        return null;
    }

    @Override
    public IOCArticlesEntity getReferenceById(IOCArticlesId iocArticlesId) {
        return null;
    }

    @Override
    public <S extends IOCArticlesEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends IOCArticlesEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends IOCArticlesEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends IOCArticlesEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends IOCArticlesEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends IOCArticlesEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends IOCArticlesEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends IOCArticlesEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends IOCArticlesEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<IOCArticlesEntity> findById(IOCArticlesId iocArticlesId) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(IOCArticlesId iocArticlesId) {
        return false;
    }

    @Override
    public List<IOCArticlesEntity> findAll() {
        return null;
    }

    @Override
    public List<IOCArticlesEntity> findAllById(Iterable<IOCArticlesId> iocArticlesIds) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(IOCArticlesId iocArticlesId) {}

    @Override
    public void delete(IOCArticlesEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends IOCArticlesId> iocArticlesIds) {}

    @Override
    public void deleteAll(Iterable<? extends IOCArticlesEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<IOCArticlesEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<IOCArticlesEntity> findAll(Pageable pageable) {
        return null;
    }
}
