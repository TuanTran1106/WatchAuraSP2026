package com.example.watchaura.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "KhachHang")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class KhachHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_nguoi_dung", nullable = false, unique = true, length = 30)
    private String maNguoiDung;

    @Column(name = "ten_nguoi_dung", nullable = false, length = 100)
    private String tenNguoiDung;

    @Column(length = 100, unique = true)
    private String email;

    @Column(length = 20)
    private String sdt;

    @Column(name = "mat_khau", nullable = false, length = 255)
    private String matKhau;

    @Column(name = "gioi_tinh", length = 10)
    private String gioiTinh;

    @Column(name = "ngay_sinh")
    @PastOrPresent(message = "Ngày sinh không được ở tương lai")
    private LocalDate ngaySinh;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chuc_vu")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ChucVu chucVu;

    @PrePersist
    protected void onCreate() {
        this.ngayTao = LocalDateTime.now();
    }
}
