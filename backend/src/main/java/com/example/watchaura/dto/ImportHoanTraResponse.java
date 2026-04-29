package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportHoanTraResponse {

    private boolean success;
    private String message;
    private int totalSerials;
    private int successCount;
    private int errorCount;
    private List<String> errorSerials;
    private List<String> previewSerials;
}
