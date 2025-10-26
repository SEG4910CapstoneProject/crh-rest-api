package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticlesRepository extends JpaRepository<ArticlesEntity, UUID> {
    List<ArticlesEntity> findByHashlink(long hashlink);

    List<ArticlesEntity> findBySourceId(Integer sourceId);
}
