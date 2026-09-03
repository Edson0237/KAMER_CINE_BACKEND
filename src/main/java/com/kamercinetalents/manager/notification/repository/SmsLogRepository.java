package com.kamercinetalents.manager.notification.repository;

import com.kamercinetalents.manager.notification.domain.SmsLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SmsLogRepository extends JpaRepository<SmsLogEntity, UUID> {
    List<SmsLogEntity> findByNotificationId(UUID notificationId);
}
