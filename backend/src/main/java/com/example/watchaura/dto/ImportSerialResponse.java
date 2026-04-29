package com.example.watchaura.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportSerialResponse {

    private Boolean success;
    private String message;
    private Integer totalSerials;
    private Integer successCount;
    private Integer errorCount;

    @Builder.Default
    private List<String> errorSerials = new ArrayList<>();

    @Builder.Default
    private List<String> previewSerials = new ArrayList<>();
}
