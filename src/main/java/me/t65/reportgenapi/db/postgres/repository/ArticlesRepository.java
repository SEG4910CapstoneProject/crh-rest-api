package me.t65.reportgenapi.db.postgres.repository;

import jakarta.transaction.Transactional;
import me.t65.reportgenapi.db.postgres.entities.ArticlesEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticlesRepository extends JpaRepository<ArticlesEntity, UUID> {
    List<ArticlesEntity> findByHashlink(long hashlink);

}
