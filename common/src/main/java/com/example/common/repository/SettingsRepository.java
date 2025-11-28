package com.example.common.repository;

import com.example.common.entity.Settings;
import com.example.common.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SettingsRepository extends JpaRepository<Settings, Long> {
    Optional<Settings> findByUser(User user);
}
