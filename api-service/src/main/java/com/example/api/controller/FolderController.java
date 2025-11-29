package com.example.api.controller;

import com.example.api.dto.FolderRequest;
import com.example.common.entity.Folder;
import com.example.common.entity.User;
import com.example.common.repository.FolderRepository;
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

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@Tag(name = "Folder Management", description = "Operations for managing email folders and organization")
@SecurityRequirement(name = "BearerAuth")
public class FolderController {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @Operation(summary = "List all folders", description = "Retrieves all email folders for the authenticated user")
    @ApiResponse(responseCode = "200", description = "List of folders", content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class)))
    @GetMapping
    public ResponseEntity<List<Folder>> getFolders(
            @Parameter(hidden = true) Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Folder> folders = folderRepository.findByUser(user);
        return ResponseEntity.ok(folders);
    }

    @Operation(summary = "Create a new folder", description = "Creates a new email folder. Can optionally specify a parent folder for nested organization.")
    @ApiResponse(responseCode = "200", description = "Folder created successfully")
    @PostMapping
    public ResponseEntity<?> createFolder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Folder details", required = true, content = @Content(schema = @Schema(implementation = FolderRequest.class))) @RequestBody FolderRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();

        Folder folder = new Folder();
        folder.setName(request.getName());
        folder.setUser(user);

        if (request.getParentId() != null) {
            Folder parent = folderRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent folder not found"));
            folder.setParent(parent);
        }

        folderRepository.save(folder);
        return ResponseEntity.ok("Folder created successfully");
    }

    @Operation(summary = "Update folder", description = "Updates folder properties like name or parent folder")
    @ApiResponse(responseCode = "200", description = "Folder updated successfully")
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateFolder(
            @Parameter(description = "Folder ID") @PathVariable("id") Long id,
            @RequestBody Folder updates,
            @Parameter(hidden = true) Authentication authentication) {
        // Implement update logic
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete folder", description = "Permanently deletes a folder and optionally its contents")
    @ApiResponse(responseCode = "200", description = "Folder deleted successfully")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(
            @Parameter(description = "Folder ID") @PathVariable("id") Long id,
            @Parameter(hidden = true) Authentication authentication) {
        folderRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
