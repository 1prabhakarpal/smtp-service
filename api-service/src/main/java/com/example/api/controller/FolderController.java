package com.example.api.controller;

import com.example.api.dto.FolderRequest;
import com.example.common.entity.Folder;
import com.example.common.entity.User;
import com.example.common.repository.FolderRepository;
import com.example.common.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Folder>> getFolders(Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        User user = userRepository.findByUsername(username).orElseThrow();
        List<Folder> folders = folderRepository.findByUser(user);
        return ResponseEntity.ok(folders);
    }

    @PostMapping
    public ResponseEntity<?> createFolder(@RequestBody FolderRequest request, Authentication authentication) {
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

    @PatchMapping("/{id}")
    public ResponseEntity<?> updateFolder(@PathVariable("id") Long id, @RequestBody Folder updates,
            Authentication authentication) {
        // Implement update logic
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFolder(@PathVariable("id") Long id, Authentication authentication) {
        folderRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
