package com.example.common.repository;

import com.example.common.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DomainRepository extends JpaRepository<Domain, Long> {
    Optional<Domain> findByName(String name);
}
