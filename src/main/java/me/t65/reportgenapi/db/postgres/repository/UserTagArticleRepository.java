package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.UserTagArticleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserTagArticleRepository
        extends JpaRepository<UserTagArticleEntity, UserTagArticleEntity.PK> {
    List<UserTagArticleEntity> findByTagId(Long tagId);

    List<UserTagArticleEntity> findByArticleId(UUID articleId);

    void deleteByTagId(Long tagId);
}
