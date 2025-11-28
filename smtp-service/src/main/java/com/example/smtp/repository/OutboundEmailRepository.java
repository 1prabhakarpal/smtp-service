package com.example.smtp.repository;

import com.example.smtp.entity.OutboundEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboundEmailRepository extends JpaRepository<OutboundEmail, Long> {

    @Query("SELECT e FROM OutboundEmail e WHERE e.status = 'PENDING' AND e.nextRetryAt <= :now")
    List<OutboundEmail> findDueForDelivery(@Param("now") LocalDateTime now);
}
