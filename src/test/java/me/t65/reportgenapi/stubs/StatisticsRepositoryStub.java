package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.StatisticEntity;
import me.t65.reportgenapi.db.postgres.repository.StatisticsRepository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Component
public class StatisticsRepositoryStub implements StatisticsRepository {
    @Override
    public void flush() {}

    @Override
    public <S extends StatisticEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends StatisticEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<StatisticEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<UUID> uuids) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public StatisticEntity getOne(UUID uuid) {
        return null;
    }

    @Override
    public StatisticEntity getById(UUID uuid) {
        return null;
    }

    @Override
    public StatisticEntity getReferenceById(UUID uuid) {
        return null;
    }

    @Override
    public <S extends StatisticEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends StatisticEntity> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends StatisticEntity> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends StatisticEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends StatisticEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends StatisticEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends StatisticEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends StatisticEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends StatisticEntity> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<StatisticEntity> findById(UUID uuid) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(UUID uuid) {
        return false;
    }

    @Override
    public List<StatisticEntity> findAll() {
        return List.of();
    }

    @Override
    public List<StatisticEntity> findAllById(Iterable<UUID> uuids) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(UUID uuid) {}

    @Override
    public void delete(StatisticEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends UUID> uuids) {}

    @Override
    public void deleteAll(Iterable<? extends StatisticEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<StatisticEntity> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<StatisticEntity> findAll(Pageable pageable) {
        return null;
    }
}
