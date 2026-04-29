package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoanTraExcelRow {

    private int rowIndex;
    private String maHoaDon;
    private String maSerial;
    private String lyDo;
    private Integer soLuong;
    private String errorMessage;

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}
