package me.t65.reportgenapi.stubs;

import me.t65.reportgenapi.db.postgres.entities.ReportArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.id.ReportArticlesId;
import me.t65.reportgenapi.db.postgres.repository.ReportArticlesRepository;

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
public class ReportArticlesRepositoryStub implements ReportArticlesRepository {

    @Override
    public void flush() {}

    @Override
    public <S extends ReportArticlesEntity> S saveAndFlush(S entity) {
        return null;
    }

    @Override
    public <S extends ReportArticlesEntity> List<S> saveAllAndFlush(Iterable<S> entities) {
        return null;
    }

    @Override
    public void deleteAllInBatch(Iterable<ReportArticlesEntity> entities) {}

    @Override
    public void deleteAllByIdInBatch(Iterable<ReportArticlesId> reportArticlesIds) {}

    @Override
    public void deleteAllInBatch() {}

    @Override
    public ReportArticlesEntity getOne(ReportArticlesId reportArticlesId) {
        return null;
    }

    @Override
    public ReportArticlesEntity getById(ReportArticlesId reportArticlesId) {
        return null;
    }

    @Override
    public ReportArticlesEntity getReferenceById(ReportArticlesId reportArticlesId) {
        return null;
    }

    @Override
    public <S extends ReportArticlesEntity> Optional<S> findOne(Example<S> example) {
        return Optional.empty();
    }

    @Override
    public <S extends ReportArticlesEntity> List<S> findAll(Example<S> example) {
        return null;
    }

    @Override
    public <S extends ReportArticlesEntity> List<S> findAll(Example<S> example, Sort sort) {
        return null;
    }

    @Override
    public <S extends ReportArticlesEntity> Page<S> findAll(Example<S> example, Pageable pageable) {
        return null;
    }

    @Override
    public <S extends ReportArticlesEntity> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends ReportArticlesEntity> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public <S extends ReportArticlesEntity, R> R findBy(
            Example<S> example, Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        return null;
    }

    @Override
    public <S extends ReportArticlesEntity> S save(S entity) {
        return null;
    }

    @Override
    public <S extends ReportArticlesEntity> List<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<ReportArticlesEntity> findById(ReportArticlesId reportArticlesId) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(ReportArticlesId reportArticlesId) {
        return false;
    }

    @Override
    public List<ReportArticlesEntity> findAll() {
        return null;
    }

    @Override
    public List<ReportArticlesEntity> findAllById(Iterable<ReportArticlesId> reportArticlesIds) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(ReportArticlesId reportArticlesId) {}

    @Override
    public void delete(ReportArticlesEntity entity) {}

    @Override
    public void deleteAllById(Iterable<? extends ReportArticlesId> reportArticlesIds) {}

    @Override
    public void deleteAll(Iterable<? extends ReportArticlesEntity> entities) {}

    @Override
    public void deleteAll() {}

    @Override
    public List<ReportArticlesEntity> findAll(Sort sort) {
        return null;
    }

    @Override
    public Page<ReportArticlesEntity> findAll(Pageable pageable) {
        return null;
    }

    @Override
    public int countByReportArticlesId_ReportIdAndSuggestion(Integer reportId, boolean suggestion) {
        return 0;
    }

    @Override
    public boolean existsByReportArticlesId_ReportIdAndReportArticlesId_ArticleIdAndSuggestion(
            Integer reportId, UUID articleId, boolean suggestion) {
        return false;
    }

    @Override
    public List<ReportArticlesEntity> findByReportArticlesId_ReportIdAndSuggestion(
            Integer reportId, boolean suggestion) {
        return null;
    }

    @Override
    public List<ReportArticlesEntity>
            findByReportArticlesId_ReportIdAndSuggestionOrderByArticleRankAsc(
                    Integer reportId, boolean suggestion) {
        return null;
    }

    @Override
    public List<ReportArticlesEntity> findByReportArticlesId_ReportIdInAndSuggestion(
            Collection<Integer> reportIds, boolean suggestion) {
        return null;
    }

    @Override
    public void deleteAllByReportId(Integer reportId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllByReportId'");
    }

    @Override
    public boolean existsByReportArticlesIdReportId(int id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
                "Unimplemented method 'existsByReportArticlesIdReportId'");
    }
}
