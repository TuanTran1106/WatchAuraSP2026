package com.example.watchaura.service;

public class SerialSelectionRequiredException extends RuntimeException {

    private final Integer hoaDonId;

    public SerialSelectionRequiredException(Integer hoaDonId) {
        super("Cần chọn serial trước khi xác nhận đơn hàng");
        this.hoaDonId = hoaDonId;
    }

    public Integer getHoaDonId() {
        return hoaDonId;
    }
}
