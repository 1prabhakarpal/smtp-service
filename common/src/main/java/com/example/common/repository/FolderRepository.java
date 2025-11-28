package com.example.common.repository;

import com.example.common.entity.Folder;
import com.example.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {
    List<Folder> findByUser(User user);

    Optional<Folder> findByUserAndNameAndParent(User user, String name, Folder parent);
}
