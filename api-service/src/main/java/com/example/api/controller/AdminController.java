package com.example.api.controller;

import com.example.common.entity.User;
import com.example.common.repository.UserRepository;
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
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return ResponseEntity.ok(userRepository.save(user));
    }

    @PatchMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @RequestBody User updates) {
        User user = userRepository.findById(id).orElseThrow();
        if (updates.getUsername() != null)
            user.setUsername(updates.getUsername());
        if (updates.getRoles() != null)
            user.setRoles(updates.getRoles());
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        userRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", userRepository.count());
        // Add more stats as needed
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/domain")
    public ResponseEntity<?> getDomain() {
        // Placeholder
        return ResponseEntity.ok("example.com");
    }

    @PostMapping("/dkim/regenerate")
    public ResponseEntity<?> regenerateDkim() {
        // Placeholder
        return ResponseEntity.ok("Regenerated");
    }
}
