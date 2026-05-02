package com.example.watchaura.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestOrderRequest {
    @NotBlank(message = "Họ tên không được để trống.")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự.")
    private String hoTen;

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ.")
    private String sdt;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không hợp lệ.")
    @Size(max = 100, message = "Email tối đa 100 ký tự.")
    private String email;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống.")
    @Size(max = 255, message = "Địa chỉ cụ thể tối đa 255 ký tự.")
    private String diaChiCuThe;

    @NotBlank(message = "Tên phường/xã không được để trống.")
    @Size(max = 100, message = "Phường/xã tối đa 100 ký tự.")
    private String phuongXa;

    @NotBlank(message = "Tên quận/huyện không được để trống.")
    @Size(max = 100, message = "Quận/huyện tối đa 100 ký tự.")
    private String quanHuyen;

    @NotBlank(message = "Tên tỉnh/thành không được để trống.")
    @Size(max = 100, message = "Tỉnh/thành tối đa 100 ký tự.")
    private String tinhThanh;

    private Integer ghnProvinceId;
    private Integer ghnDistrictId;

    @NotBlank(message = "Mã phường GHN không được để trống.")
    @Size(max = 20, message = "Mã phường GHN tối đa 20 ký tự.")
    private String ghnWardCode;

    @Size(max = 255, message = "Ghi chú tối đa 255 ký tự.")
    private String ghiChu;

    @Size(max = 50, message = "Phương thức thanh toán tối đa 50 ký tự.")
    private String phuongThucThanhToan;
}
