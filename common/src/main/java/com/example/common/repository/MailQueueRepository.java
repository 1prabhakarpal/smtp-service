package com.example.common.repository;

import com.example.common.entity.OutboundQueue;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MailQueueRepository extends JpaRepository<OutboundQueue, Long> {
    List<OutboundQueue> findByStatusAndNextRetryAtBeforeOrderByNextRetryAtAsc(String status, LocalDateTime nextRetryAt,
            Pageable pageable);
}
