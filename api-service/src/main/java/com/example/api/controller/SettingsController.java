package com.example.api.controller;

import com.example.common.entity.Settings;
import com.example.common.entity.User;
import com.example.common.repository.SettingsRepository;
import com.example.common.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "User Settings", description = "Manage user preferences and settings")
@SecurityRequirement(name = "BearerAuth")
public class SettingsController {

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Get user settings", description = "Retrieves the settings for the authenticated user. Creates default settings if none exist.")
    @ApiResponse(responseCode = "200", description = "User settings", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Settings.class)))
    @GetMapping
    public ResponseEntity<Settings> getSettings(
            @Parameter(hidden = true) Authentication authentication) {
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

    @Operation(summary = "Update user settings", description = "Updates user settings such as theme, signature, and notification preferences")
    @ApiResponse(responseCode = "200", description = "Settings updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Settings.class)))
    @PatchMapping
    public ResponseEntity<Settings> updateSettings(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated settings", required = true, content = @Content(schema = @Schema(implementation = Settings.class))) @RequestBody Settings updatedSettings,
            @Parameter(hidden = true) Authentication authentication) {
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
