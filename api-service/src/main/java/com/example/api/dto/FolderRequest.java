package com.example.api.dto;

import lombok.Data;

@Data
public class FolderRequest {
    private String name;
    private Long parentId;
}
