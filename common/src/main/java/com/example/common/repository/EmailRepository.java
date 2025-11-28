package com.example.common.repository;

import com.example.common.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {
        List<Email> findByRecipient(String recipient);

        org.springframework.data.domain.Page<Email> findByUser_Id(Long userId,
                        org.springframework.data.domain.Pageable pageable);

        org.springframework.data.domain.Page<Email> findByUser_IdAndFolder_Id(Long userId, Long folderId,
                        org.springframework.data.domain.Pageable pageable);
}
