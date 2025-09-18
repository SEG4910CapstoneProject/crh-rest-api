package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.ReportEntity;
import me.t65.reportgenapi.db.postgres.entities.ReportType;
import me.t65.reportgenapi.db.postgres.repository.ReportRepository;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
public class ReportRepositoryStub implements ReportRepository {

    @Override
    public void flush() {}

    @Override
    public <S extends ReportEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends ReportEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<ReportEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<Integer> integers) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public ReportEntity getOne(Integer integer) {
        return null;
    }

    @Override
    public ReportEntity getById(Integer integer) {
        return null;
    }

    @Override
    public ReportEntity getReferenceById(Integer integer) {
        return null;
    }

    @Override
    public <S extends ReportEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends ReportEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends ReportEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends ReportEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ReportEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ReportEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends ReportEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends ReportEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends ReportEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<ReportEntity> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public List<ReportEntity> findAll() {
        return null;
    }

    @Override
    public List<ReportEntity> findAllById(Iterable<Integer> integers) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Integer integer) {}

    @Override
    public void delete(ReportEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends Integer> integers) {}

    @Override
    public void deleteAll(Iterable<? extends ReportEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<ReportEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<ReportEntity> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public List<ReportEntity> findByGenerateDateBetweenAndReportType(
            Instant generateDateStart,
            Instant generateDateEnd,
            ReportType reportType,
            Pageable pageable) {
        return null;
    }

    @Override
    public long countByGenerateDateBetweenAndReportType(
            Instant generateDateStart, Instant generateDateEnd, ReportType reportType) {
        return 0;
    }

    @Override
    public long countByReportType(ReportType reportType) {
        return 0;
    }

    @Override
    public List<ReportEntity> findByReportType(ReportType reportType /* , Pageable pageable*/) {
        return null;
    }

    @Override
    public ReportEntity findFirstByOrderByGenerateDateDesc() {
        return null;
    }

    @Override
    public List<ReportEntity> findByGenerateDateLessThanEqual(Instant generateDateEnd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'findByGenerateDateLessThanEqual'");
    }

    @Override
    public List<ReportEntity> findByGenerateDateGreaterThanEqual(Instant generateDateStart) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'findByGenerateDateGreaterThanEqual'");
    }

    @Override
    public List<ReportEntity> findByGenerateDateBetween(
            Instant generateDateStart, Instant generateDateEnd) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByGenerateDateBetween'");
    }

    @Override
    public List<ReportEntity> findByReportId(long reportId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findByReportId'");
    }

    @Override
    public List<ReportEntity> findByGenerateDateLessThanEqualAndReportId(
            Instant generateDateEnd, long reportId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'findByGenerateDateLessThanEqualAndReportId'");
    }

    @Override
    public List<ReportEntity> findByGenerateDateGreaterThanEqualAndReportId(
            Instant generateDateStart, long reportId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'findByGenerateDateGreaterThanEqualAndReportId'");
    }

    @Override
    public List<ReportEntity> findByGenerateDateBetweenAndReportId(
            Instant generateDateStart, Instant generateDateEnd, long reportId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'findByGenerateDateBetweenAndReportId'");
    }
}
