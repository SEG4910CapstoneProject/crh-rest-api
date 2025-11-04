package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.UserTagEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserTagRepository extends JpaRepository<UserTagEntity, Long> {
    List<UserTagEntity> findByUserId(Long userId);

    boolean existsByUserIdAndTagName(Long userId, String tagName);

    Optional<UserTagEntity> findByUserIdAndTagId(Long userId, Long tagId);
}
