package com.kamercinetalents.manager.notification.repository;

import com.kamercinetalents.manager.notification.domain.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {
    List<NotificationEntity> findByUtilisateurIdOrderByDateEnvoiDesc(UUID utilisateurId);
    long countByUtilisateurIdAndStatut(UUID utilisateurId, String statut);
}
