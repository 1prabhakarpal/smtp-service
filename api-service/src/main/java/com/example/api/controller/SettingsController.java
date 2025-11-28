package com.example.api.controller;

import com.example.common.entity.Settings;
import com.example.common.entity.User;
import com.example.common.repository.SettingsRepository;
import com.example.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Settings> getSettings(Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();

        Settings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> {
                    Settings s = new Settings();
                    s.setUser(user);
                    s.setTheme("light");
                    s.setNotificationsEnabled(true);
                    return settingsRepository.save(s);
                });

        return ResponseEntity.ok(settings);
    }

    @PatchMapping
    public ResponseEntity<Settings> updateSettings(@RequestBody Settings updatedSettings,
            Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();

        Settings settings = settingsRepository.findByUser(user)
                .orElseGet(() -> {
                    Settings s = new Settings();
                    s.setUser(user);
                    return s;
                });

        if (updatedSettings.getTheme() != null)
            settings.setTheme(updatedSettings.getTheme());
        if (updatedSettings.getSignature() != null)
            settings.setSignature(updatedSettings.getSignature());
        settings.setNotificationsEnabled(updatedSettings.isNotificationsEnabled());

        return ResponseEntity.ok(settingsRepository.save(settings));
    }
}
