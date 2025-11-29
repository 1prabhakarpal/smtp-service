package com.example.api.controller;

import com.example.common.entity.User;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Operations", description = "Administrative operations for system management (requires ADMIN role)")
@SecurityRequirement(name = "BearerAuth")
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "List all users", description = "Retrieves a list of all registered users (Admin only)")
    @ApiResponse(responseCode = "200", description = "List of users", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @Operation(summary = "Create a new user", description = "Creates a new user account (Admin only)")
    @ApiResponse(responseCode = "200", description = "User created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @PostMapping("/users")
    public ResponseEntity<User> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User details", required = true, content = @Content(schema = @Schema(implementation = User.class))) @RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Operation(summary = "Update user", description = "Updates user properties like username or roles (Admin only)")
    @ApiResponse(responseCode = "200", description = "User updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class)))
    @PatchMapping("/users/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "User ID") @PathVariable("id") Long id,
            @RequestBody User updates) {
        User user = userRepository.findById(id).orElseThrow();
        if (updates.getUsername() != null)
            user.setUsername(updates.getUsername());
        if (updates.getRoles() != null)
            user.setRoles(updates.getRoles());
        return ResponseEntity.ok(userRepository.save(user));
    }

    @Operation(summary = "Delete user", description = "Permanently deletes a user account (Admin only)")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @Parameter(description = "User ID") @PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get system statistics", description = "Retrieves system statistics and metrics (Admin only)")
    @ApiResponse(responseCode = "200", description = "System statistics", content = @Content(mediaType = "application/json"))
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", userRepository.count());
        // Add more stats as needed
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get domain configuration", description = "Retrieves the current domain configuration (Admin only)")
    @ApiResponse(responseCode = "200", description = "Domain configuration")
    @GetMapping("/domain")
    public ResponseEntity<?> getDomain() {
        // Placeholder
        return ResponseEntity.ok("example.com");
    }

    @Operation(summary = "Regenerate DKIM keys", description = "Regenerates DKIM signing keys for the email domain (Admin only)")
    @ApiResponse(responseCode = "200", description = "DKIM keys regenerated")
    @PostMapping("/dkim/regenerate")
    public ResponseEntity<?> regenerateDkim() {
        // Placeholder
        return ResponseEntity.ok("Regenerated");
    }
}
