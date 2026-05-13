package com.example.watchaura.validator;

import com.example.watchaura.dto.KhuyenMaiRequest;
import com.example.watchaura.util.KhuyenMaiPricing;
import java.math.BigDecimal;
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
        }

        if ("PHAN_TRAM".equals(loaiCanon) && req.getGiaTriGiam() != null) {
            BigDecimal value = req.getGiaTriGiam();
            if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(BigDecimal.valueOf(100)) > 0) {
                errors.rejectValue("giaTriGiam", "range", "Giảm theo % phải lớn hơn 0 và không quá 100%.");
            }
        }

        if ("TIEN".equals(loaiCanon) && req.getGiaTriGiam() != null) {
            if (req.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
                errors.rejectValue("giaTriGiam", "range", "Giảm theo tiền phải lớn hơn 0.");
            }
            if (req.getDonToiThieu() != null
                    && req.getDonToiThieu().compareTo(BigDecimal.ZERO) > 0
                    && req.getGiaTriGiam().compareTo(req.getDonToiThieu()) >= 0) {
                errors.rejectValue("giaTriGiam", "range", "Giảm tiền phải nhỏ hơn đơn tối thiểu.");
            }
        }

        if (req.getNgayBatDau() != null && req.getNgayKetThuc() != null
                && !req.getNgayKetThuc().isAfter(req.getNgayBatDau())) {
            errors.rejectValue("ngayKetThuc", "date_order", "Ngày kết thúc phải sau ngày bắt đầu.");
        }

        if (req.getDonToiThieu() != null && req.getDonToiThieu().compareTo(BigDecimal.ZERO) < 0) {
            errors.rejectValue("donToiThieu", "range", "Đơn tối thiểu phải lớn hơn hoặc bằng 0.");
        }

        // Phạm vi áp dụng có thể để trống từ form cũ; service sẽ mặc định ALL để tương thích dữ liệu hiện có.
    }
}
