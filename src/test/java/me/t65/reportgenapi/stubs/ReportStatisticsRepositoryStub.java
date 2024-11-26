package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.ReportStatisticsEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ReportStatisticsId;
import me.t65.reportgenapi.db.postgres.repository.ReportStatisticsRepository;

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
public class ReportStatisticsRepositoryStub implements ReportStatisticsRepository {
    @Override
    public void flush() {}

    @Override
    public <S extends ReportStatisticsEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends ReportStatisticsEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public void deleteAllInBatch(Iterable<ReportStatisticsEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<ReportStatisticsId> reportStatisticsIds) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public ReportStatisticsEntity getOne(ReportStatisticsId reportStatisticsId) {
        return null;
    }

    @Override
    public ReportStatisticsEntity getById(ReportStatisticsId reportStatisticsId) {
        return null;
    }

    @Override
    public ReportStatisticsEntity getReferenceById(ReportStatisticsId reportStatisticsId) {
        return null;
    }

    @Override
    public <S extends ReportStatisticsEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends ReportStatisticsEntity> List<S> findAll(Example<S> example) {
        return List.of();
    }

    @Override
    public <S extends ReportStatisticsEntity> List<S> findAll(Example<S> example, Sort sort) {
        return List.of();
    }

    @Override
    public <S extends ReportStatisticsEntity> Page<S> findAll(
            Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ReportStatisticsEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ReportStatisticsEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends ReportStatisticsEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends ReportStatisticsEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends ReportStatisticsEntity> List<S> saveAll(Iterable<S> entities) {
        return List.of();
    }

    @Override
    public Optional<ReportStatisticsEntity> findById(ReportStatisticsId reportStatisticsId) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ReportStatisticsId reportStatisticsId) {
        return false;
    }

    @Override
    public List<ReportStatisticsEntity> findAll() {
        return List.of();
    }

    @Override
    public List<ReportStatisticsEntity> findAllById(
            Iterable<ReportStatisticsId> reportStatisticsIds) {
        return List.of();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(ReportStatisticsId reportStatisticsId) {}

    @Override
    public void delete(ReportStatisticsEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends ReportStatisticsId> reportStatisticsIds) {}

    @Override
    public void deleteAll(Iterable<? extends ReportStatisticsEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<ReportStatisticsEntity> findAll(Sort sort) {
        return List.of();
    }

    @Override
    public Page<ReportStatisticsEntity> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public boolean
            existsByReportStatisticsId_ReportIdAndReportStatisticsId_StatisticIdAndSuggestion(
                    Integer reportId, UUID statisticId, boolean suggestion) {
        return false;
    }

    @Override
    public List<ReportStatisticsEntity> findByReportStatisticsId_ReportIdAndSuggestion(
            Integer reportId, boolean suggestion) {
        return null;
    }
}
