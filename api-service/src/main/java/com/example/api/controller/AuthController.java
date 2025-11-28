package com.example.api.controller;

import com.example.api.service.UserService;
import com.example.common.entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final com.example.api.security.JwtUtil jwtUtil;

    public AuthController(UserService userService, com.example.api.security.JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        try {
            User user = userService.register(username, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody com.example.api.dto.LoginRequest loginRequest) {
        if (userService.login(loginRequest.getUsername(), loginRequest.getPassword())) {
            String token = jwtUtil.generateToken(loginRequest.getUsername(), "USER");
            return ResponseEntity.ok(com.example.api.dto.LoginResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .username(loginRequest.getUsername())
                    .roles("USER")
                    .build());
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userService.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(user);
    }
}
