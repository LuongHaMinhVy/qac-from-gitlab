package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findAllByRecipientAccountId(Integer recipientId, Pageable pageable);

    Long countByRecipientAccountIdAndIsReadFalse(Integer recipientId);
}
