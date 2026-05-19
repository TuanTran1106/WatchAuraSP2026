package com.example.watchaura.validator;

import com.example.watchaura.dto.KhuyenMaiRequest;
import com.example.watchaura.util.KhuyenMaiPricing;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class KhuyenMaiValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return KhuyenMaiRequest.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (!(target instanceof KhuyenMaiRequest req)) {
            return;
        }

        String loaiCanon = KhuyenMaiPricing.chuanHoaMaLuu(req.getLoaiGiam());
        if (loaiCanon == null) {
            errors.rejectValue("loaiGiam", "invalid", "Loại giảm không hợp lệ.");
            return;
        }

        if (!"PHAN_TRAM".equals(loaiCanon)) {
            errors.rejectValue("loaiGiam", "invalid", "Khuyến mãi sản phẩm chỉ hỗ trợ giảm theo %.");
            return;
        }

        if (req.getGiaTriGiam() != null) {
            BigDecimal value = req.getGiaTriGiam();
            if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
                errors.rejectValue("giaTriGiam", "range", "Giảm theo % phải lớn hơn 0 và không quá 100%.");
            }
        }

        if (req.getNgayBatDau() != null && req.getNgayKetThuc() != null
                && !req.getNgayKetThuc().isAfter(req.getNgayBatDau())) {
            errors.rejectValue("ngayKetThuc", "date_order", "Ngày kết thúc phải sau ngày bắt đầu.");
        }

        if (req.getId() == null && req.getNgayBatDau() != null
                && req.getNgayBatDau().isBefore(LocalDateTime.now())) {
            errors.rejectValue("ngayBatDau", "past", "Ngày bắt đầu không được trong quá khứ khi tạo mới.");
        }

        if (req.getId() == null && req.getNgayKetThuc() != null
                && req.getNgayKetThuc().isBefore(LocalDateTime.now())) {
            errors.rejectValue("ngayKetThuc", "past", "Ngày kết thúc không được trong quá khứ khi tạo mới.");
        }
    }
}
