package com.campuspath.backend.notification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findTop50ByUserIdAndNotifyAtLessThanEqualOrderByNotifyAtDesc(Integer userId, Instant now);

    long countByUserIdAndReadFalseAndNotifyAtLessThanEqual(Integer userId, Instant now);

    Optional<Notification> findByIdAndUserId(Integer id, Integer userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.read = false")
    int markAllRead(@Param("userId") Integer userId);
    
    @Modifying
    @Query(value = """
            INSERT INTO public.notifications (user_id, title, message, notify_at, source_type, source_key)
            VALUES (:userId, :title, :message, :notifyAt, :sourceType, :sourceKey)
            ON CONFLICT (user_id, source_type, source_key) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("userId") Integer userId, @Param("title") String title,
                       @Param("message") String message, @Param("notifyAt") Instant notifyAt,
                       @Param("sourceType") String sourceType, @Param("sourceKey") String sourceKey);
}