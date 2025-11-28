package com.example.common.repository;

import com.example.common.entity.Email;
import com.example.common.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmailRepository extends JpaRepository<Email, Long> {
    List<Email> findByFolder(Folder folder);
}
