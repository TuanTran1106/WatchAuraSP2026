package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO nhận kết quả VNPay trả về (Return URL & IPN)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VNPayResponseDTO {

    /** Mã phản hồi: "00" = thành công */
    private String responseCode;

    /** Mã giao dịch VNPay */
    private String transactionNo;

    /** Mã đơn hàng của bạn (vnp_TxnRef) */
    private String maDonHang;

    /** Số tiền thanh toán (đã chia 100) */
    private String soTien;

    /** Ngân hàng thanh toán */
    private String bankCode;

    /** Thời gian thanh toán */
    private String payDate;

    /** Thông báo kết quả hiển thị cho người dùng */
    private String message;

    /** Thanh toán thành công? */
    private boolean success;
}