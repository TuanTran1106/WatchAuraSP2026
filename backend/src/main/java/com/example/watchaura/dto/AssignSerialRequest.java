package com.example.watchaura.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignSerialRequest {

    @NotNull(message = "ID hóa đơn không được null")
    private Integer hoaDonId;

    @NotEmpty(message = "Danh sách serial không được rỗng")
    private Map<Integer, List<Integer>> serialsByBienThe;

    private String trangThaiMoi;
}
