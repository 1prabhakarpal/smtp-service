package com.example.common.repository;

import com.example.common.entity.OutboundQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutboundQueueRepository extends JpaRepository<OutboundQueue, Long> {
    List<OutboundQueue> findByStatus(String status);
}
