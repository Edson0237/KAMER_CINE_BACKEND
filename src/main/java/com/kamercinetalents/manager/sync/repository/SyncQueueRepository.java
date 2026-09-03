package com.kamercinetalents.manager.sync.repository;

import com.kamercinetalents.manager.sync.domain.SyncQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface SyncQueueRepository extends JpaRepository<SyncQueueEntity, UUID> {

    List<SyncQueueEntity> findByUtilisateurIdOrderByHorodatageClientAsc(UUID utilisateurId);

    @Query("SELECT q FROM SyncQueueEntity q WHERE q.statut = :statut ORDER BY q.horodatageClient ASC")
    List<SyncQueueEntity> findByStatut(String statut);
}
