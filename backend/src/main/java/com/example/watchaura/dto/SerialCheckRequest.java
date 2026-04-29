package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialCheckRequest {

    private String maSerial;
    private Integer hoanTraId;

    public SerialCheckRequest(String maSerial) {
        this.maSerial = maSerial;
    }
}
