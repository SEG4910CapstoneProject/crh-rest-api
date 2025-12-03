package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u.email FROM UserEntity u")
    List<String> findAllEmails();
}
