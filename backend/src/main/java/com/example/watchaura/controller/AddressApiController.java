package com.example.watchaura.controller;

import com.example.watchaura.dto.AddressDefaultResponse;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.service.DiaChiService;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class AddressApiController {

    private final DiaChiService diaChiService;
    private final KhachHangService khachHangService;

    @GetMapping("/default")
    public ResponseEntity<?> getDefaultAddress(HttpSession session) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }
        Integer khachHangId = (Integer) userId;

        DiaChi dc = diaChiService.getDiaChiMacDinhByKhachHang(khachHangId).orElse(null);
        if (dc == null) {
            return ResponseEntity.noContent().build();
        }

        KhachHang kh = khachHangService.getById(khachHangId);
        String ten = kh.getTenNguoiDung() != null ? kh.getTenNguoiDung() : "";
        String sdt = kh.getSdt() != null ? kh.getSdt() : "";

        StringBuilder sb = new StringBuilder();
        if (dc.getDiaChiCuThe() != null && !dc.getDiaChiCuThe().isBlank()) sb.append(dc.getDiaChiCuThe());
        if (dc.getPhuongXa() != null && !dc.getPhuongXa().isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(dc.getPhuongXa()); }
        if (dc.getQuanHuyen() != null && !dc.getQuanHuyen().isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(dc.getQuanHuyen()); }
        if (dc.getTinhThanh() != null && !dc.getTinhThanh().isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(dc.getTinhThanh()); }

        AddressDefaultResponse resp = new AddressDefaultResponse(
                dc.getId(),
                ten,
                sdt,
                sb.toString(),
                dc.getGhnProvinceId(),
                dc.getGhnDistrictId(),
                dc.getGhnWardCode()
        );

        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<?> list(HttpSession session) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }
        Integer khachHangId = (Integer) userId;

        KhachHang kh = khachHangService.getById(khachHangId);
        String ten = kh.getTenNguoiDung() != null ? kh.getTenNguoiDung() : "";
        String sdt = kh.getSdt() != null ? kh.getSdt() : "";

        DiaChi dcDefault = diaChiService.getDiaChiMacDinhByKhachHang(khachHangId).orElse(null);
        Integer defaultId = dcDefault != null ? dcDefault.getId() : null;

        List<DiaChi> list = diaChiService.getByKhachHang(khachHangId);
        List<Map<String, Object>> addresses = list.stream().map(dc -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", dc.getId());
            m.put("diaChiCuThe", dc.getDiaChiCuThe());
            m.put("phuongXa", dc.getPhuongXa());
            m.put("quanHuyen", dc.getQuanHuyen());
            m.put("tinhThanh", dc.getTinhThanh());
            m.put("ghnProvinceId", dc.getGhnProvinceId());
            m.put("ghnDistrictId", dc.getGhnDistrictId());
            m.put("ghnWardCode", dc.getGhnWardCode());
            m.put("macDinh", defaultId != null && defaultId.equals(dc.getId()));
            return m;
        }).toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("tenNguoiNhan", ten);
        resp.put("sdtNguoiNhan", sdt);
        resp.put("addresses", addresses);
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    public ResponseEntity<?> create(
            HttpSession session,
            @RequestParam("diaChiCuThe") String diaChiCuThe,
            @RequestParam("tinhThanh") String tinhThanh,
            @RequestParam("quanHuyen") String quanHuyen,
            @RequestParam("phuongXa") String phuongXa,
            @RequestParam("ghnProvinceId") Integer ghnProvinceId,
            @RequestParam("ghnDistrictId") Integer ghnDistrictId,
            @RequestParam("ghnWardCode") String ghnWardCode
    ) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }
        Integer khachHangId = (Integer) userId;

        DiaChi dc = new DiaChi();
        dc.setDiaChiCuThe(diaChiCuThe);
        dc.setTinhThanh(tinhThanh);
        dc.setQuanHuyen(quanHuyen);
        dc.setPhuongXa(phuongXa);
        dc.setGhnProvinceId(ghnProvinceId);
        dc.setGhnDistrictId(ghnDistrictId);
        dc.setGhnWardCode(ghnWardCode);
        dc.setMacDinh(false);
        DiaChi saved = diaChiService.create(khachHangId, dc);

        return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
    }
}

