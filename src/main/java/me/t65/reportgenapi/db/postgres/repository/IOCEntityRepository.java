package me.t65.reportgenapi.db.postgres.repository;

import me.t65.reportgenapi.db.postgres.entities.IOCEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IOCEntityRepository extends JpaRepository<IOCEntity, Integer> {}
