package com.example.watchaura.controller;

import com.example.watchaura.dto.AddressDefaultResponse;
import com.example.watchaura.dto.DiaChiRequest;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.service.DiaChiService;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        String tenKh = kh.getTenNguoiDung() != null ? kh.getTenNguoiDung() : "";
        String sdtKh = kh.getSdt() != null ? kh.getSdt() : "";
        String ten = (dc.getTenNguoiNhan() != null && !dc.getTenNguoiNhan().isBlank()) ? dc.getTenNguoiNhan().trim() : tenKh;
        String sdt = (dc.getSdtNguoiNhan() != null && !dc.getSdtNguoiNhan().isBlank()) ? dc.getSdtNguoiNhan().trim() : sdtKh;

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

        List<DiaChi> list = new ArrayList<>(diaChiService.getByKhachHang(khachHangId));
        list.sort((a, b) -> {
            boolean ad = defaultId != null && defaultId.equals(a.getId());
            boolean bd = defaultId != null && defaultId.equals(b.getId());
            if (ad != bd) {
                return ad ? -1 : 1;
            }
            int ida = a.getId() != null ? a.getId() : 0;
            int idb = b.getId() != null ? b.getId() : 0;
            return Integer.compare(idb, ida);
        });

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
            m.put("tenNguoiNhan", dc.getTenNguoiNhan());
            m.put("sdtNguoiNhan", dc.getSdtNguoiNhan());
            return m;
        }).toList();

        Map<String, Object> resp = new HashMap<>();
        resp.put("tenNguoiNhan", ten);
        resp.put("sdtNguoiNhan", sdt);
        resp.put("addresses", addresses);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(HttpSession session, @PathVariable("id") Integer id) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "UNAUTHORIZED"));
        }
        Integer khachHangId = (Integer) userId;
        DiaChi dc;
        try {
            dc = diaChiService.getById(id);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Không tìm thấy địa chỉ."));
        }
        if (dc.getKhachHang() == null || !dc.getKhachHang().getId().equals(khachHangId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Không được phép."));
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", dc.getId());
        m.put("tenNguoiNhan", dc.getTenNguoiNhan());
        m.put("sdtNguoiNhan", dc.getSdtNguoiNhan());
        m.put("diaChiCuThe", dc.getDiaChiCuThe());
        m.put("phuongXa", dc.getPhuongXa());
        m.put("quanHuyen", dc.getQuanHuyen());
        m.put("tinhThanh", dc.getTinhThanh());
        m.put("ghnProvinceId", dc.getGhnProvinceId());
        m.put("ghnDistrictId", dc.getGhnDistrictId());
        m.put("ghnWardCode", dc.getGhnWardCode());
        m.put("macDinh", Boolean.TRUE.equals(dc.getMacDinh()));
        return ResponseEntity.ok(m);
    }

    @PostMapping
    public ResponseEntity<?> create(
            HttpSession session,
            @Valid @ModelAttribute DiaChiRequest request,
            BindingResult bindingResult
    ) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }
        Integer khachHangId = (Integer) userId;
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", formatBindingErrors(bindingResult)));
        }

        List<DiaChi> existing = diaChiService.getByKhachHang(khachHangId);

        DiaChi dc = new DiaChi();
        dc.setDiaChiCuThe(request.getDiaChiCuThe());
        dc.setTinhThanh(request.getTinhThanh());
        dc.setQuanHuyen(request.getQuanHuyen());
        dc.setPhuongXa(request.getPhuongXa());
        dc.setGhnProvinceId(request.getGhnProvinceId());
        dc.setGhnDistrictId(request.getGhnDistrictId());
        dc.setGhnWardCode(request.getGhnWardCode());
        dc.setTenNguoiNhan(blankToNull(request.getTenNguoiNhan()));
        dc.setSdtNguoiNhan(blankToNull(request.getSdtNguoiNhan()));
        dc.setMacDinh(existing.isEmpty());
        DiaChi saved = diaChiService.create(khachHangId, dc);

        return ResponseEntity.ok(Map.of("success", true, "id", saved.getId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            HttpSession session,
            @PathVariable("id") Integer id,
            @Valid @ModelAttribute DiaChiRequest request,
            BindingResult bindingResult
    ) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("UNAUTHORIZED");
        }
        Integer khachHangId = (Integer) userId;
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", formatBindingErrors(bindingResult)));
        }

        DiaChi current = diaChiService.getById(id);
        if (current.getKhachHang() == null || !current.getKhachHang().getId().equals(khachHangId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("success", false, "message", "Không được phép."));
        }

        DiaChi patch = new DiaChi();
        patch.setDiaChiCuThe(request.getDiaChiCuThe());
        patch.setTinhThanh(request.getTinhThanh());
        patch.setQuanHuyen(request.getQuanHuyen());
        patch.setPhuongXa(request.getPhuongXa());
        patch.setGhnProvinceId(request.getGhnProvinceId());
        patch.setGhnDistrictId(request.getGhnDistrictId());
        patch.setGhnWardCode(request.getGhnWardCode());
        patch.setTenNguoiNhan(blankToNull(request.getTenNguoiNhan()));
        patch.setSdtNguoiNhan(blankToNull(request.getSdtNguoiNhan()));
        patch.setMacDinh(Boolean.TRUE.equals(current.getMacDinh()));
        try {
            diaChiService.update(id, patch);
            return ResponseEntity.ok(Map.of("success", true, "id", id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Cập nhật thất bại."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(HttpSession session, @PathVariable("id") Integer id) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "UNAUTHORIZED"));
        }
        Integer khachHangId = (Integer) userId;
        try {
            diaChiService.deleteForKhachHang(khachHangId, id);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Không xóa được địa chỉ."));
        }
    }

    private static String blankToNull(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        return s.trim();
    }

    private static String formatBindingErrors(BindingResult br) {
        Set<String> msgs = new LinkedHashSet<>();
        for (FieldError fe : br.getFieldErrors()) {
            String m = fe.getDefaultMessage();
            if (m != null && !m.isBlank()) {
                msgs.add(m.trim());
            }
        }
        for (ObjectError ge : br.getGlobalErrors()) {
            String m = ge.getDefaultMessage();
            if (m != null && !m.isBlank()) {
                msgs.add(m.trim());
            }
        }
        if (msgs.isEmpty()) {
            return "Thông tin không hợp lệ.";
        }
        return String.join(" · ", msgs);
    }

    /** Đặt địa chỉ làm mặc định (dùng từ trang thanh toán). */
    @PostMapping("/set-default")
    public ResponseEntity<?> setDefault(HttpSession session, @RequestParam("diaChiId") Integer diaChiId) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("success", false, "message", "UNAUTHORIZED"));
        }
        Integer khachHangId = (Integer) userId;
        try {
            diaChiService.setDiaChiMacDinh(khachHangId, diaChiId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Không thể đặt mặc định."));
        }
    }
}

