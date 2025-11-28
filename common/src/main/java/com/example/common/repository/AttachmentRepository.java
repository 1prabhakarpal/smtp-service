package com.example.common.repository;

import com.example.common.entity.Attachment;
import com.example.common.entity.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByEmail(Email email);
}
