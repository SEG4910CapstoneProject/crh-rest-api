package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.IOCArticlesEntity;
import me.t65.reportgenapi.db.postgres.entities.id.IOCArticlesId;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface IOCArticlesEntityRepository
        extends JpaRepository<IOCArticlesEntity, IOCArticlesId> {
    List<IOCArticlesEntity> findByIocArticlesId_ArticleIdIn(Collection<UUID> articleIds);
}
