package com.example.api.controller;

import com.example.api.service.UserService;
import com.example.common.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Authentication", description = "User authentication and authorization endpoints")
public class AuthController {

    private final UserService userService;
    private final com.example.api.security.JwtUtil jwtUtil;

    public AuthController(UserService userService, com.example.api.security.JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "Register a new user", description = "Creates a new user account with username and password. Username must be unique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or username already exists", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"error\": \"Username already exists\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User registration credentials", required = true, content = @Content(examples = @ExampleObject(value = "{\"username\": \"john.doe\", \"password\": \"SecurePassword123!\"}"))) @RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        try {
            User user = userService.register(username, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "User login", description = "Authenticates user credentials and returns a JWT bearer token for subsequent API calls")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.example.api.dto.LoginResponse.class), examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\", \"type\": \"Bearer\", \"username\": \"john.doe\", \"roles\": \"USER\"}"))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Invalid credentials\"")))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "User login credentials", required = true, content = @Content(schema = @Schema(implementation = com.example.api.dto.LoginRequest.class), examples = @ExampleObject(value = "{\"username\": \"john.doe\", \"password\": \"SecurePassword123!\"}"))) @RequestBody com.example.api.dto.LoginRequest loginRequest) {
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

    @Operation(summary = "User logout", description = "Logs out the current user. Note: With JWT, client should discard the token.", security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponse(responseCode = "200", description = "Logged out successfully")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }

    @Operation(summary = "Get current user information", description = "Returns the profile information of the currently authenticated user", security = @SecurityRequirement(name = "BearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Current user information", content = @Content(mediaType = "application/json", schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    })
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            @Parameter(hidden = true) org.springframework.security.core.Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userService.findByUsername(username).orElseThrow();
        return ResponseEntity.ok(user);
    }
}
