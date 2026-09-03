package com.kamercinetalents.manager.notification.repository;

import com.kamercinetalents.manager.notification.domain.TemplateNotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TemplateNotificationRepository extends JpaRepository<TemplateNotificationEntity, UUID> {
    Optional<TemplateNotificationEntity> findByCodeAndCanalAndActifTrue(String code, String canal);
    Optional<TemplateNotificationEntity> findByCode(String code);
}
