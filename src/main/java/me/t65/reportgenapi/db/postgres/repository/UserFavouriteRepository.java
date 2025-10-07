package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.UserFavouriteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserFavouriteRepository extends JpaRepository<UserFavouriteEntity, Long> {
    List<UserFavouriteEntity> findByUserId(Long userId);
    void deleteByUserIdAndArticleId(Long userId, UUID articleId);
    boolean existsByUserIdAndArticleId(Long userId, UUID articleId);
}
