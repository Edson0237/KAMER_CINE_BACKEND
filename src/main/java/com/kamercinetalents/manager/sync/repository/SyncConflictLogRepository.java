package com.kamercinetalents.manager.sync.repository;

import com.kamercinetalents.manager.sync.domain.SyncConflictLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SyncConflictLogRepository extends JpaRepository<SyncConflictLogEntity, UUID> {

    List<SyncConflictLogEntity> findBySyncQueueId(UUID syncQueueId);
}
