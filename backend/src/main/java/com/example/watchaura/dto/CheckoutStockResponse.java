package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutStockResponse {

    private boolean success;
    private String message;

    @Builder.Default
    private List<StockWarningItem> warnings = new ArrayList<>();

    private Integer totalWarnings;

    public static CheckoutStockResponse ok() {
        return CheckoutStockResponse.builder()
                .success(true)
                .message("Thanh toán thành công.")
                .warnings(new ArrayList<>())
                .totalWarnings(0)
                .build();
    }

    public static CheckoutStockResponse ok(String message) {
        return CheckoutStockResponse.builder()
                .success(true)
                .message(message)
                .warnings(new ArrayList<>())
                .totalWarnings(0)
                .build();
    }

    public static CheckoutStockResponse withWarnings(List<StockWarningItem> warnings, String message) {
        return CheckoutStockResponse.builder()
                .success(true)
                .message(message)
                .warnings(warnings)
                .totalWarnings(warnings.size())
                .build();
    }

    public static CheckoutStockResponse error(String message) {
        return CheckoutStockResponse.builder()
                .success(false)
                .message(message)
                .warnings(new ArrayList<>())
                .totalWarnings(0)
                .build();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}
