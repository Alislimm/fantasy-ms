package com.example.fantasy.repository;

import com.example.fantasy.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserIdOrderByTimestampDesc(Long userId);
    
    Page<Notification> findByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);
    
    List<Notification> findByUserIdAndReadStatusOrderByTimestampDesc(Long userId, Boolean readStatus);
    
    long countByUserIdAndReadStatus(Long userId, Boolean readStatus);
    
    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true WHERE n.user.id = :userId AND n.readStatus = false")
    void markAllAsReadForUser(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true WHERE n.id = :notificationId AND n.user.id = :userId")
    void markAsReadByUserAndId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
}