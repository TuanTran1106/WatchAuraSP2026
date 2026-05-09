package com.example.watchaura.service.impl;

import com.example.watchaura.dto.*;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.ExcelService;
import com.example.watchaura.service.HoanTraService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoanTraServiceImpl implements HoanTraService {

    private final HoanTraRepository hoanTraRepository;
    private final HoanTraChiTietRepository hoanTraChiTietRepository;
    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SerialSanPhamRepository serialSanPhamRepository;
    private final ExcelService excelService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public List<HoanTraDTO> getAllHoanTra() {
        return hoanTraRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HoanTraDTO getHoanTraById(Integer id) {
        HoanTra hoanTra = hoanTraRepository.findByIdWithChiTiet(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + id));
        return convertToDTO(hoanTra);
    }

    @Override
    @Transactional
    public HoanTraDTO getHoanTraByMaHoanTra(String maHoanTra) {
        HoanTra hoanTra = hoanTraRepository.findByMaHoanTraWithChiTiet(maHoanTra)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với mã: " + maHoanTra));
        return convertToDTO(hoanTra);
    }

    @Override
    public List<HoanTraDTO> getHoanTraByKhachHangId(Integer khachHangId) {
        return hoanTraRepository.findByKhachHangIdOrderByNgayYeuCauDesc(khachHangId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HoanTraDTO> getHoanTraByTrangThai(String trangThai) {
        return hoanTraRepository.findByTrangThaiOrderByNgayYeuCauDesc(trangThai).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getHoanTraPaged(int page, int size, String trangThai, String keyword, String loaiHoanTra, String tuNgay, String denNgay) {
        Map<String, Object> result = new HashMap<>();

        // Parse date filters
        LocalDateTime tuNgayTime = null;
        LocalDateTime denNgayTime = null;
        if (tuNgay != null && !tuNgay.isEmpty()) {
            tuNgayTime = LocalDateTime.parse(tuNgay + "T00:00:00");
        }
        if (denNgay != null && !denNgay.isEmpty()) {
            denNgayTime = LocalDateTime.parse(denNgay + "T23:59:59");
        }

        Page<HoanTra> hoanTraPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            hoanTraPage = hoanTraRepository.searchHoanTra(
                    trangThai != null && !trangThai.isEmpty() ? trangThai : null,
                    keyword.trim(),
                    loaiHoanTra != null && !loaiHoanTra.isEmpty() ? loaiHoanTra : null,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayYeuCau"))
            );
        } else if (trangThai != null && !trangThai.isEmpty()) {
            hoanTraPage = hoanTraRepository.searchByFilters(
                    trangThai,
                    loaiHoanTra != null && !loaiHoanTra.isEmpty() ? loaiHoanTra : null,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayYeuCau"))
            );
        } else if (loaiHoanTra != null && !loaiHoanTra.isEmpty()) {
            hoanTraPage = hoanTraRepository.findByLoaiHoanTraWithDateRange(
                    loaiHoanTra,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayYeuCau"))
            );
        } else {
            hoanTraPage = hoanTraRepository.findAllOrderByNgayYeuCauDesc(
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayYeuCau"))
            );
        }

        // Apply date filter in memory if needed
        final LocalDateTime finalTuNgayTime = tuNgayTime;
        final LocalDateTime finalDenNgayTime = denNgayTime;
        List<HoanTra> statsList = hoanTraPage.getContent();
        List<HoanTra> displayList;
        
        if (finalTuNgayTime != null || finalDenNgayTime != null) {
            displayList = statsList.stream()
                    .filter(h -> {
                        LocalDateTime ngayYeuCau = h.getNgayYeuCau();
                        if (ngayYeuCau == null) return false;
                        if (finalTuNgayTime != null && ngayYeuCau.isBefore(finalTuNgayTime)) return false;
                        if (finalDenNgayTime != null && ngayYeuCau.isAfter(finalDenNgayTime)) return false;
                        return true;
                    })
                    .collect(Collectors.toList());
        } else {
            displayList = statsList;
        }

        // Convert filtered list to DTOs
        List<HoanTraDTO> content = displayList.stream()
                .map(this::convertToDTOWithChiTiet)
                .collect(Collectors.toList());

        result.put("content", content);
        result.put("totalElements", displayList.size());
        result.put("totalPages", (int) Math.ceil((double) displayList.size() / size));
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("hasNext", page < (int) Math.ceil((double) displayList.size() / size) - 1);
        result.put("hasPrevious", page > 0);

        // Stats for current page (using displayList if date filter is active)
        // Stats for TRA_HANG (current page)
        long choXuLy = statsList.stream()
                .filter(h -> HoanTra.TRANG_THAI_CHO_XU_LY.equals(h.getTrangThai())).count();
        long dangXuLy = statsList.stream()
                .filter(h -> HoanTra.TRANG_THAI_DANG_XU_LY.equals(h.getTrangThai())).count();
        long daXuLy = statsList.stream()
                .filter(h -> HoanTra.TRANG_THAI_DA_XU_LY.equals(h.getTrangThai())).count();
        long tuChoi = statsList.stream()
                .filter(h -> HoanTra.TRANG_THAI_TU_CHOI.equals(h.getTrangThai())).count();


        // Total stats from DB
        long totalChoXuLy = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_CHO_XU_LY);
        long totalDangXuLy = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_DANG_XU_LY);
        long totalDaXuLy = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_DA_XU_LY);
        long totalTuChoi = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_TU_CHOI);


        // Total elements for current filter
        long totalTongSo = (finalTuNgayTime != null || finalDenNgayTime != null) ? displayList.size() : hoanTraPage.getTotalElements();

        result.put("stats", new HashMap<>() {{
            put("choXuLy", choXuLy);
            put("dangXuLy", dangXuLy);
            put("daXuLy", daXuLy);
            put("tuChoi", tuChoi);
            put("totalChoXuLy", totalChoXuLy);
            put("totalDangXuLy", totalDangXuLy);
            put("totalDaXuLy", totalDaXuLy);
            put("totalTuChoi", totalTuChoi);
            put("tongSo", totalTongSo);
        }});

        return result;
    }

    @Override
    @Transactional
    public HoanTraDTO createHoanTra(HoanTraRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + request.getIdHoaDon()));

        KhachHang khachHang = hoaDon.getKhachHang();
        if (khachHang == null) {
            khachHang = khachHangRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng mặc định"));
        }

        HoanTra hoanTra = new HoanTra();
        hoanTra.setMaHoanTra(generateMaHoanTra());
        hoanTra.setHoaDon(hoaDon);
        hoanTra.setKhachHang(khachHang);
        hoanTra.setLyDo(request.getLyDo());
        hoanTra.setLoaiHoanTra(request.getLoaiHoanTra());

        // Set thông tin tài khoản ngân hàng để hoàn tiền
        if (request.getSoTaiKhoan() != null && !request.getSoTaiKhoan().isEmpty()) {
            hoanTra.setSoTaiKhoan(request.getSoTaiKhoan());
            hoanTra.setTenNganHang(request.getTenNganHang());
            hoanTra.setTenChuTaiKhoan(request.getTenChuTaiKhoan());
        }

        // Set initial status
        hoanTra.setTrangThai(HoanTra.TRANG_THAI_CHO_XU_LY);

        hoanTra.setSoTienHoanTra(BigDecimal.ZERO);
        hoanTra.setNgayYeuCau(LocalDateTime.now());

        hoanTra = hoanTraRepository.save(hoanTra);

        // Tính tổng số tiền hoàn từ các chi tiết (không bao gồm phí ship)
        BigDecimal tongTienTuChiTiet = BigDecimal.ZERO;

        if (request.getChiTietList() != null && !request.getChiTietList().isEmpty()) {
            for (HoanTraRequest.HoanTraChiTietRequest chiTietRequest : request.getChiTietList()) {
                HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(chiTietRequest.getIdHoaDonChiTiet())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn chi tiết với ID: " + chiTietRequest.getIdHoaDonChiTiet()));

                SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(chiTietRequest.getIdSanPhamChiTiet())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + chiTietRequest.getIdSanPhamChiTiet()));

                // Lấy đơn giá từ hóa đơn chi tiết (cùng nguồn với trang create)
                BigDecimal donGia = hoaDonChiTiet.getDonGia();
                if (donGia == null) {
                    donGia = BigDecimal.ZERO;
                }
                BigDecimal soTienHoan = donGia.multiply(BigDecimal.valueOf(chiTietRequest.getSoLuongHoanTra()));
                tongTienTuChiTiet = tongTienTuChiTiet.add(soTienHoan);

                HoanTraChiTiet chiTiet = new HoanTraChiTiet();
                chiTiet.setHoanTra(hoanTra);
                chiTiet.setSanPhamChiTiet(sanPhamChiTiet);
                chiTiet.setHoaDonChiTiet(hoaDonChiTiet);
                chiTiet.setSoLuongHoanTra(chiTietRequest.getSoLuongHoanTra());
                chiTiet.setDonGiaTaiThoiDiemMua(donGia);
                chiTiet.setSoTienHoan(soTienHoan);
                chiTiet.setHinhAnh(chiTietRequest.getHinhAnh());

                hoanTraChiTietRepository.save(chiTiet);

                if (chiTietRequest.getSerialsHoanTra() != null && !chiTietRequest.getSerialsHoanTra().isEmpty()) {
                    for (String serial : chiTietRequest.getSerialsHoanTra()) {
                        SerialSanPham serialSanPham = serialSanPhamRepository.findByMaSerial(serial.trim()).orElse(null);
                        if (serialSanPham != null) {
                            serialSanPham.setTrangThai(SerialSanPham.TRANG_THAI_DA_TRA_HANG);
                            serialSanPhamRepository.save(serialSanPham);
                        }
                    }
                }
            }
        }

        // Tính số tiền hoàn = tổng tiền từ chi tiết - tỷ lệ giảm giá (không cộng phí ship)
        BigDecimal tongTienTamTinh = hoaDon.getTongTienTamTinh() != null
                ? hoaDon.getTongTienTamTinh()
                : BigDecimal.ZERO;
        BigDecimal tienGiam = hoaDon.getTienGiam() != null
                ? hoaDon.getTienGiam()
                : BigDecimal.ZERO;

        BigDecimal tongTienHoan = tongTienTuChiTiet;
        // Áp dụng tỷ lệ giảm giá nếu có
        if (tongTienTamTinh.compareTo(BigDecimal.ZERO) > 0 && tienGiam.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tiLeGiam = tienGiam.divide(tongTienTamTinh, 4, java.math.RoundingMode.HALF_UP);
            BigDecimal soTienDuocGiam = tongTienTuChiTiet.multiply(tiLeGiam).setScale(0, java.math.RoundingMode.HALF_UP);
            tongTienHoan = tongTienTuChiTiet.subtract(soTienDuocGiam);
        }

        hoanTra.setSoTienHoanTra(tongTienHoan);
        hoanTra = hoanTraRepository.save(hoanTra);

        return convertToDTO(hoanTra);
    }

    @Override
    @Transactional
    public HoanTraDTO updateHoanTra(Integer id, HoanTraRequest request) {
        HoanTra hoanTra = hoanTraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + id));

        if (HoanTra.TRANG_THAI_DA_XU_LY.equals(hoanTra.getTrangThai()) 
                || HoanTra.TRANG_THAI_TU_CHOI.equals(hoanTra.getTrangThai())) {
            throw new RuntimeException("Không thể cập nhật hoàn trả đã xử lý hoặc từ chối");
        }

        hoanTra.setLyDo(request.getLyDo());
        if (request.getGhiChuXuLy() != null) {
            hoanTra.setGhiChuXuLy(request.getGhiChuXuLy());
        }

        hoanTra = hoanTraRepository.save(hoanTra);
        return convertToDTO(hoanTra);
    }

    @Override
    @Transactional
    public HoanTraDTO xuLyHoanTra(Integer id, String ghiChuXuLy, Integer idNhanVienXuLy) {
        HoanTra hoanTra = hoanTraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + id));

        if (!HoanTra.TRANG_THAI_CHO_XU_LY.equals(hoanTra.getTrangThai()) 
                && !HoanTra.TRANG_THAI_DANG_XU_LY.equals(hoanTra.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể xử lý hoàn trả đang chờ xử lý");
        }

        KhachHang nhanVien = khachHangRepository.findById(idNhanVienXuLy)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + idNhanVienXuLy));

        hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_XU_LY);
        hoanTra.setNhanVienXuLy(nhanVien);
        hoanTra.setGhiChuXuLy(ghiChuXuLy);
        hoanTra.setNgayXuLy(LocalDateTime.now());

        List<HoanTraChiTiet> chiTietList = hoanTraChiTietRepository.findByHoanTraId(id);
        for (HoanTraChiTiet chiTiet : chiTietList) {
            SanPhamChiTiet sanPhamChiTiet = chiTiet.getSanPhamChiTiet();
            int currentStock = sanPhamChiTiet.getSoLuongTon() != null ? sanPhamChiTiet.getSoLuongTon() : 0;
            sanPhamChiTiet.setSoLuongTon(currentStock + chiTiet.getSoLuongHoanTra());
            sanPhamChiTietRepository.save(sanPhamChiTiet);
        }

        hoanTra = hoanTraRepository.save(hoanTra);
        return convertToDTO(hoanTra);
    }

    @Override
    @Transactional
    public HoanTraDTO tuChoiHoanTra(Integer id, String ghiChuXuLy, Integer idNhanVienXuLy) {
        HoanTra hoanTra = hoanTraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + id));

        String trangThaiHienTai = hoanTra.getTrangThai();

        // Kiểm tra trạng thái có thể từ chối
        boolean coTheTuChoi = HoanTra.TRANG_THAI_CHO_XU_LY.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DANG_XU_LY.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_DUYET.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_NHAN_HANG.equals(trangThaiHienTai);

        if (!coTheTuChoi) {
            throw new RuntimeException("Không thể từ chối hoàn trả ở trạng thái: " + trangThaiHienTai);
        }

        KhachHang nhanVien = khachHangRepository.findById(idNhanVienXuLy)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + idNhanVienXuLy));

        hoanTra.setTrangThai(HoanTra.TRANG_THAI_TU_CHOI);
        hoanTra.setNhanVienXuLy(nhanVien);
        hoanTra.setGhiChuXuLy(ghiChuXuLy);
        hoanTra.setNgayXuLy(LocalDateTime.now());

        List<HoanTraChiTiet> chiTietList = hoanTraChiTietRepository.findByHoanTraId(id);
        for (HoanTraChiTiet chiTiet : chiTietList) {
            List<SerialSanPham> serials = serialSanPhamRepository.findByHoaDonChiTietIdOrderByIdAsc(
                    chiTiet.getHoaDonChiTiet().getId());
            for (SerialSanPham serial : serials) {
                if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
                    serial.setTrangThai(SerialSanPham.TRANG_THAI_DA_BAN);
                    serial.setGhiChu("Từ chối trả hàng - Serial trả lại cho khách");
                    serialSanPhamRepository.save(serial);
                }
            }
        }

        hoanTra = hoanTraRepository.save(hoanTra);
        return convertToDTO(hoanTra);
    }

    @Override
    @Transactional
    public void deleteHoanTra(Integer id) {
        HoanTra hoanTra = hoanTraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + id));

        if (HoanTra.TRANG_THAI_DA_XU_LY.equals(hoanTra.getTrangThai())) {
            throw new RuntimeException("Không thể xóa hoàn trả đã xử lý");
        }

        hoanTraChiTietRepository.deleteAll(hoanTra.getChiTietList());
        hoanTraRepository.delete(hoanTra);
    }

    @Override
    public ImportHoanTraResponse previewImportExcel(MultipartFile file) {
        List<HoanTraExcelRow> rows = parseExcelFile(file);
        List<String> errors = new ArrayList<>();
        List<String> previews = new ArrayList<>();

        Map<String, List<HoanTraExcelRow>> groupedByMaHoaDon = new HashMap<>();
        for (HoanTraExcelRow row : rows) {
            if (row.hasError()) {
                errors.add("Dòng " + row.getRowIndex() + ": " + row.getErrorMessage());
            } else {
                groupedByMaHoaDon.computeIfAbsent(row.getMaHoaDon(), k -> new ArrayList<>()).add(row);
            }
        }

        for (Map.Entry<String, List<HoanTraExcelRow>> entry : groupedByMaHoaDon.entrySet()) {
            HoaDon hoaDon = hoaDonRepository.findByMaDonHang(entry.getKey()).orElse(null);
            if (hoaDon == null) {
                for (HoanTraExcelRow row : entry.getValue()) {
                    errors.add("Dòng " + row.getRowIndex() + ": Mã hóa đơn '" + row.getMaHoaDon() + "' không tồn tại");
                }
            } else {
                Set<String> seenSerials = new HashSet<>();
                for (HoanTraExcelRow row : entry.getValue()) {
                    if (seenSerials.contains(row.getMaSerial())) {
                        errors.add("Dòng " + row.getRowIndex() + ": Serial '" + row.getMaSerial() + "' bị trùng trong file");
                    } else {
                        seenSerials.add(row.getMaSerial());
                        SerialSanPham serial = serialSanPhamRepository.findByMaSerial(row.getMaSerial()).orElse(null);
                        if (serial == null) {
                            errors.add("Dòng " + row.getRowIndex() + ": Serial '" + row.getMaSerial() + "' không tồn tại trong hệ thống");
                        } else if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
                            errors.add("Dòng " + row.getRowIndex() + ": Serial '" + row.getMaSerial() + "' đã được hoàn trả trước đó");
                        } else if (serial.getHoaDonChiTiet() == null || 
                                   !serial.getHoaDonChiTiet().getHoaDon().getMaDonHang().equals(entry.getKey())) {
                            errors.add("Dòng " + row.getRowIndex() + ": Serial '" + row.getMaSerial() + "' không thuộc hóa đơn '" + entry.getKey() + "'");
                        }
                    }
                }
            }
        }

        if (errors.isEmpty()) {
            previews.add("Sẵn sàng import " + rows.size() + " serial từ " + groupedByMaHoaDon.size() + " hóa đơn");
            for (Map.Entry<String, List<HoanTraExcelRow>> entry : groupedByMaHoaDon.entrySet()) {
                previews.add("Hóa đơn " + entry.getKey() + ": " + entry.getValue().size() + " serial");
            }
        }

        return ImportHoanTraResponse.builder()
                .success(errors.isEmpty())
                .message(errors.isEmpty() ? "Dữ liệu hợp lệ" : "Có " + errors.size() + " lỗi")
                .totalSerials(rows.size())
                .successCount(rows.size() - errors.size())
                .errorCount(errors.size())
                .errorSerials(errors)
                .previewSerials(previews)
                .build();
    }

    @Override
    @Transactional
    public ImportHoanTraResponse importFromExcel(MultipartFile file, Integer idNhanVienXuLy) {
        List<HoanTraExcelRow> rows = parseExcelFile(file);
        List<String> errors = new ArrayList<>();
        List<String> importedSerials = new ArrayList<>();

        Map<String, List<HoanTraExcelRow>> groupedByMaHoaDon = new HashMap<>();
        for (HoanTraExcelRow row : rows) {
            if (!row.hasError()) {
                groupedByMaHoaDon.computeIfAbsent(row.getMaHoaDon(), k -> new ArrayList<>()).add(row);
            } else {
                errors.add("Dòng " + row.getRowIndex() + ": " + row.getErrorMessage());
            }
        }

        for (Map.Entry<String, List<HoanTraExcelRow>> entry : groupedByMaHoaDon.entrySet()) {
            HoaDon hoaDon = hoaDonRepository.findByMaDonHang(entry.getKey()).orElse(null);
            if (hoaDon == null) {
                for (HoanTraExcelRow row : entry.getValue()) {
                    errors.add("Dòng " + row.getRowIndex() + ": Mã hóa đơn '" + row.getMaHoaDon() + "' không tồn tại");
                }
                continue;
            }

            KhachHang khachHang = hoaDon.getKhachHang();
            if (khachHang == null) {
                khachHang = khachHangRepository.findById(1)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng mặc định"));
            }

            HoanTra hoanTra = new HoanTra();
            hoanTra.setMaHoanTra(generateMaHoanTra());
            hoanTra.setHoaDon(hoaDon);
            hoanTra.setKhachHang(khachHang);
            hoanTra.setLyDo(entry.getValue().get(0).getLyDo() != null ? entry.getValue().get(0).getLyDo() : "Hoàn trả qua Excel");
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_XU_LY);
            hoanTra.setNgayYeuCau(LocalDateTime.now());
            hoanTra.setNgayXuLy(LocalDateTime.now());

            if (idNhanVienXuLy != null) {
                khachHangRepository.findById(idNhanVienXuLy).ifPresent(hoanTra::setNhanVienXuLy);
            }

            hoanTra = hoanTraRepository.save(hoanTra);

            BigDecimal tongTienHoan = BigDecimal.ZERO;
            Map<Integer, Integer> soLuongByHoaDonChiTiet = new HashMap<>();

            for (HoanTraExcelRow row : entry.getValue()) {
                SerialSanPham serial = serialSanPhamRepository.findByMaSerial(row.getMaSerial()).orElse(null);
                if (serial != null && serial.getHoaDonChiTiet() != null) {
                    HoaDonChiTiet hoaDonChiTiet = serial.getHoaDonChiTiet();
                    
                    serial.setTrangThai(SerialSanPham.TRANG_THAI_DA_TRA_HANG);
                    serialSanPhamRepository.save(serial);

                    soLuongByHoaDonChiTiet.merge(hoaDonChiTiet.getId(), 1, Integer::sum);
                    importedSerials.add(row.getMaSerial());
                }
            }

            for (Map.Entry<Integer, Integer> soLuongEntry : soLuongByHoaDonChiTiet.entrySet()) {
                Integer idHoaDonChiTiet = soLuongEntry.getKey();
                Integer soLuong = soLuongEntry.getValue();

                HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(idHoaDonChiTiet)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn chi tiết"));
                SanPhamChiTiet sanPhamChiTiet = hoaDonChiTiet.getSanPhamChiTiet();

                // Lấy đơn giá gốc từ SanPhamChiTiet (chưa trừ khuyến mãi)
                BigDecimal donGia = sanPhamChiTiet.getGiaBan();
                if (donGia == null) {
                    donGia = BigDecimal.ZERO;
                }
                BigDecimal soTienHoan = donGia.multiply(BigDecimal.valueOf(soLuong));

                HoanTraChiTiet chiTiet = new HoanTraChiTiet();
                chiTiet.setHoanTra(hoanTra);
                chiTiet.setSanPhamChiTiet(sanPhamChiTiet);
                chiTiet.setHoaDonChiTiet(hoaDonChiTiet);
                chiTiet.setSoLuongHoanTra(soLuong);
                chiTiet.setDonGiaTaiThoiDiemMua(donGia);
                chiTiet.setSoTienHoan(soTienHoan);

                hoanTraChiTietRepository.save(chiTiet);
                tongTienHoan = tongTienHoan.add(soTienHoan);

                int currentStock = sanPhamChiTiet.getSoLuongTon() != null ? sanPhamChiTiet.getSoLuongTon() : 0;
                sanPhamChiTiet.setSoLuongTon(currentStock + soLuong);
                sanPhamChiTietRepository.save(sanPhamChiTiet);
            }

            hoanTra.setSoTienHoanTra(tongTienHoan);
            hoanTraRepository.save(hoanTra);
        }

        String message;
        if (errors.isEmpty() && !importedSerials.isEmpty()) {
            message = "Import thành công " + importedSerials.size() + " serial từ " + groupedByMaHoaDon.size() + " hóa đơn";
        } else if (importedSerials.isEmpty()) {
            message = "Import thất bại: không có serial nào được import";
        } else {
            message = "Import " + importedSerials.size() + " serial thành công, " + errors.size() + " lỗi";
        }

        return ImportHoanTraResponse.builder()
                .success(errors.isEmpty())
                .message(message)
                .totalSerials(rows.size())
                .successCount(importedSerials.size())
                .errorCount(errors.size())
                .errorSerials(errors)
                .previewSerials(importedSerials.stream().limit(10).collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<HoanTraExcelRow> validateExcelData(MultipartFile file) {
        return parseExcelFile(file);
    }

    private List<HoanTraExcelRow> parseExcelFile(MultipartFile file) {
        List<HoanTraExcelRow> rows = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                HoanTraExcelRow excelRow = new HoanTraExcelRow();
                excelRow.setRowIndex(i + 1);

                Cell cellMaHoaDon = row.getCell(0);
                Cell cellMaSerial = row.getCell(1);
                Cell cellLyDo = row.getCell(2);

                String maHoaDon = getCellValueAsString(cellMaHoaDon);
                String maSerial = getCellValueAsString(cellMaSerial);
                String lyDo = getCellValueAsString(cellLyDo);

                if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
                    excelRow.setErrorMessage("Mã hóa đơn trống");
                    rows.add(excelRow);
                    continue;
                }
                excelRow.setMaHoaDon(maHoaDon.trim());

                if (maSerial == null || maSerial.trim().isEmpty()) {
                    excelRow.setErrorMessage("Mã serial trống");
                    rows.add(excelRow);
                    continue;
                }
                excelRow.setMaSerial(maSerial.trim());
                excelRow.setLyDo(lyDo);

                rows.add(excelRow);
            }
        } catch (IOException e) {
            log.error("Lỗi khi đọc file Excel: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage());
        }

        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private String generateMaHoanTra() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String random = String.format("%04d", new Random().nextInt(10000));
        return "HT" + timestamp + random;
    }

    private HoanTraDTO convertToDTO(HoanTra hoanTra) {
        HoanTraDTO dto = HoanTraDTO.builder()
                .id(hoanTra.getId())
                .maHoanTra(hoanTra.getMaHoanTra())
                .idHoaDon(hoanTra.getHoaDon() != null ? hoanTra.getHoaDon().getId() : null)
                .maDonHang(hoanTra.getHoaDon() != null ? hoanTra.getHoaDon().getMaDonHang() : null)
                .idKhachHang(hoanTra.getKhachHang() != null ? hoanTra.getKhachHang().getId() : null)
                .tenKhachHang(hoanTra.getKhachHang() != null ? hoanTra.getKhachHang().getTenNguoiDung() : null)
                .sdtKhachHang(hoanTra.getKhachHang() != null ? hoanTra.getKhachHang().getSdt() : null)
                .idNhanVienXuLy(hoanTra.getNhanVienXuLy() != null ? hoanTra.getNhanVienXuLy().getId() : null)
                .tenNhanVienXuLy(hoanTra.getNhanVienXuLy() != null ? hoanTra.getNhanVienXuLy().getTenNguoiDung() : null)
                .lyDo(hoanTra.getLyDo())
                .soTienHoanTra(hoanTra.getSoTienHoanTra())
                .trangThai(hoanTra.getTrangThai())
                .trangThaiHienThi(getTrangThaiHienThi(hoanTra.getTrangThai()))
                .ghiChuXuLy(hoanTra.getGhiChuXuLy())
                .loaiHoanTra(hoanTra.getLoaiHoanTra())
                .loaiHoanTraHienThi(getLoaiHoanTraHienThi(hoanTra.getLoaiHoanTra()))
                .ngayYeuCau(hoanTra.getNgayYeuCau())
                .ngayXuLy(hoanTra.getNgayXuLy())
                .phuongThucHoanTien(hoanTra.getPhuongThucHoanTien())
                .phuongThucHoanTienHienThi(getPhuongThucHienThi(hoanTra.getPhuongThucHoanTien()))
                .soTienHoanThucTe(hoanTra.getSoTienHoanThucTe())
                .ghiChuHoanTien(hoanTra.getGhiChuHoanTien())
                .ngayHoanTien(hoanTra.getNgayHoanTien())
                // Thông tin tài khoản ngân hàng
                .soTaiKhoan(hoanTra.getSoTaiKhoan())
                .tenNganHang(hoanTra.getTenNganHang())
                .tenChuTaiKhoan(hoanTra.getTenChuTaiKhoan())
                // Thông tin hóa đơn gốc
                .tongTienHoaDon(hoanTra.getHoaDon() != null ? hoanTra.getHoaDon().getTongTienTamTinh() : null)
                .phiGiaoHang(hoanTra.getHoaDon() != null ? hoanTra.getHoaDon().getPhiVanChuyen() : null)
                .voucherGiam(hoanTra.getHoaDon() != null ? hoanTra.getHoaDon().getTienGiam() : null)
                .build();

        if (hoanTra.getChiTietList() != null && !hoanTra.getChiTietList().isEmpty()) {
            List<HoanTraChiTietDTO> chiTietDTOs = new ArrayList<>();
            for (HoanTraChiTiet chiTiet : hoanTra.getChiTietList()) {
                HoanTraChiTietDTO chiTietDTO = convertChiTietToDTO(chiTiet);
                chiTietDTOs.add(chiTietDTO);
            }
            dto.setChiTietList(chiTietDTOs);
        }

        return dto;
    }

    /**
     * Convert to DTO với chi tiết đầy đủ - dùng cho API paged để tính lại số tiền hoàn trên frontend
     */
    private HoanTraDTO convertToDTOWithChiTiet(HoanTra hoanTra) {
        HoanTraDTO dto = convertToDTO(hoanTra);

        // Đã có trong convertToDTO rồi, nhưng đảm bảo có chi tiết
        if (hoanTra.getChiTietList() != null && !hoanTra.getChiTietList().isEmpty()) {
            List<HoanTraChiTietDTO> chiTietDTOs = new ArrayList<>();
            for (HoanTraChiTiet chiTiet : hoanTra.getChiTietList()) {
                HoanTraChiTietDTO chiTietDTO = convertChiTietToDTO(chiTiet);
                chiTietDTOs.add(chiTietDTO);
            }
            dto.setChiTietList(chiTietDTOs);
        }

        // Thông tin hóa đơn gốc cho tính lại số tiền hoàn
        dto.setTongTienHoaDon(hoanTra.getHoaDon() != null ? hoanTra.getHoaDon().getTongTienTamTinh() : null);
        dto.setVoucherGiam(hoanTra.getHoaDon() != null ? hoanTra.getHoaDon().getTienGiam() : null);

        return dto;
    }

    private HoanTraChiTietDTO convertChiTietToDTO(HoanTraChiTiet chiTiet) {
        SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();

        String tenBienThe = buildTenBienThe(
                spct != null ? spct.getMauSac() : null,
                spct != null ? spct.getKichThuoc() : null,
                spct != null ? spct.getChatLieuDay() : null
        );

        HoanTraChiTietDTO dto = HoanTraChiTietDTO.builder()
                .id(chiTiet.getId())
                .idHoanTra(chiTiet.getHoanTra() != null ? chiTiet.getHoanTra().getId() : null)
                .idSanPhamChiTiet(spct != null ? spct.getId() : null)
                .tenSanPham(spct != null && spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : null)
                .maSanPham(spct != null && spct.getSanPham() != null ? spct.getSanPham().getMaSanPham() : null)
                .mauSac(spct != null && spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : null)
                .kichThuoc(spct != null && spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : null)
                .chatLieuDay(spct != null && spct.getChatLieuDay() != null ? spct.getChatLieuDay().getTenChatLieu() : null)
                .tenBienThe(tenBienThe)
                .idHoaDonChiTiet(chiTiet.getHoaDonChiTiet() != null ? chiTiet.getHoaDonChiTiet().getId() : null)
                .soLuongHoanTra(chiTiet.getSoLuongHoanTra())
                .donGiaTaiThoiDiemMua(chiTiet.getDonGiaTaiThoiDiemMua())
                .soTienHoan(chiTiet.getSoTienHoan())
                .hinhAnh(chiTiet.getHinhAnh())
                .build();

        List<String> serialsHoanTra = new ArrayList<>();
        List<HoanTraChiTietDTO.SerialInfo> serialsChiTiet = new ArrayList<>();

        // Hiển thị serials đã trả hàng
        if (chiTiet.getHoaDonChiTiet() != null && chiTiet.getHoaDonChiTiet().getSerialSanPhams() != null) {
            for (SerialSanPham sp : chiTiet.getHoaDonChiTiet().getSerialSanPhams()) {
                if (sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
                    serialsHoanTra.add(sp.getMaSerial());
                }
                HoanTraChiTietDTO.SerialInfo si = HoanTraChiTietDTO.SerialInfo.builder()
                        .maSerial(sp.getMaSerial())
                        .trangThai(String.valueOf(sp.getTrangThai()))
                        .trangThaiHienThi(getSerialTrangThaiText(sp.getTrangThai()))
                        .daDuocChon(sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG)
                        .build();
                serialsChiTiet.add(si);
            }
        }

        dto.setSerialsHoanTra(serialsHoanTra);
        dto.setSerialsChiTiet(serialsChiTiet);

        if (chiTiet.getHoaDonChiTiet() != null) {
            Integer daHoan = hoanTraChiTietRepository.sumSoLuongHoanTraByHoaDonChiTietId(chiTiet.getHoaDonChiTiet().getId());
            int soLuongDaHoan = daHoan != null ? daHoan : 0;
            dto.setSoLuongDaHoanTra(soLuongDaHoan);
            dto.setSoLuongConLai((chiTiet.getHoaDonChiTiet().getSoLuong() - soLuongDaHoan));
        }

        return dto;
    }

    private String getTrangThaiHienThi(String trangThai) {
        if (trangThai == null) return "Không xác định";
        switch (trangThai) {
            case HoanTra.TRANG_THAI_CHO_XU_LY:
                return "Chờ xử lý";
            case HoanTra.TRANG_THAI_DANG_XU_LY:
                return "Đang xử lý";
            case HoanTra.TRANG_THAI_DA_XU_LY:
                return "Đã xử lý";
            case HoanTra.TRANG_THAI_TU_CHOI:
                return "Từ chối";
            case HoanTra.TRANG_THAI_DA_DUYET:
                return "Đã duyệt";
            case HoanTra.TRANG_THAI_DA_NHAN_HANG:
                return "Đã nhận hàng";
            case HoanTra.TRANG_THAI_DA_HOAN_TIEN:
                return "Đã hoàn tiền";
            default:
                return trangThai;
        }
    }

    private String getLoaiHoanTraHienThi(String loaiHoanTra) {
        if (loaiHoanTra == null) return "Không xác định";
        switch (loaiHoanTra) {
            case HoanTra.LOAI_TRA_HANG:
                return "Trả hàng";
            default:
                return loaiHoanTra;
        }
    }

    private String getPhuongThucHienThi(String phuongThuc) {
        if (phuongThuc == null) return "Chưa hoàn tiền";
        switch (phuongThuc) {
            case HoanTra.PHUONG_THUC_TIEN_MAT:
                return "Tiền mặt";
            case HoanTra.PHUONG_THUC_CHUYEN_KHOAN:
                return "Chuyển khoản";
            case HoanTra.PHUONG_THUC_VI_DIEN_TU:
                return "Ví điện tử";
            default:
                return phuongThuc;
        }
    }

    public byte[] generateHoanTraTemplateExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("HoanTra");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã Hóa Đơn", "Mã Serial", "Lý do (tùy chọn)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 1; i <= 10; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue("HD" + String.format("%08d", i));
                row.createCell(1).setCellValue("SERIAL_" + String.format("%06d", i));
                row.createCell(2).setCellValue("Sản phẩm lỗi");
            }

            sheet.setColumnWidth(0, 4000);
            sheet.setColumnWidth(1, 4000);
            sheet.setColumnWidth(2, 6000);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Lỗi khi tạo template Excel: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tạo template Excel: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getHoaDonChoKhachHangTra(Integer hoaDonId, Integer khachHangId) {
        Map<String, Object> result = new HashMap<>();
        
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

        if (khachHangId != null && hoaDon.getKhachHang() != null 
                && !khachHangId.equals(hoaDon.getKhachHang().getId())) {
            throw new RuntimeException("Bạn không có quyền xem hóa đơn này");
        }

        String tt = hoaDon.getTrangThaiDonHang();
        boolean coTheHoanTra = "DA_GIAO".equals(tt) || "HOAN_THANH".equals(tt)
                || "DA_THANH_TOAN".equals(tt) || "DA THANH TOAN".equals(tt)
                || "DA_XAC_NHAN".equals(tt) || "DA_THANH_TOAN_ONL".equals(tt);
        if (!coTheHoanTra) {
            result.put("success", false);
            result.put("message", "Đơn hàng có trạng thái '" + tt + "' không thể hoàn trả.");
            return result;
        }

        result.put("success", true);
        result.put("id", hoaDon.getId());
        result.put("maDonHang", hoaDon.getMaDonHang());
        result.put("ngayDat", hoaDon.getNgayDat());
        result.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
        result.put("tenKhachHang", hoaDon.getTenKhachHang());
        result.put("sdtKhachHang", hoaDon.getSdtKhachHang());
        result.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
        result.put("tongTienHoaDon", hoaDon.getTongTienTamTinh());
        // Tính phí giao hàng: tongTienThanhToan - tongTienTamTinh + tienGiam
        BigDecimal tienGiamVal = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
        BigDecimal phiVanChuyen = hoaDon.getPhiVanChuyen();
        if (phiVanChuyen == null) {
            phiVanChuyen = hoaDon.getTongTienThanhToan()
                    .subtract(hoaDon.getTongTienTamTinh())
                    .add(tienGiamVal);
            if (phiVanChuyen.compareTo(BigDecimal.ZERO) < 0) {
                phiVanChuyen = BigDecimal.ZERO;
            }
        }
        result.put("phiGiaoHang", phiVanChuyen);
        result.put("voucherGiam", tienGiamVal);
        result.put("coTheHoanTra", coTheHoanTra);

        List<Map<String, Object>> chiTietList = new ArrayList<>();
        List<HoaDonChiTiet> chiTietEntities = hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDonId);
        for (HoaDonChiTiet hdct : chiTietEntities) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", hdct.getId());
            item.put("idSanPhamChiTiet", hdct.getSanPhamChiTiet().getId());
            item.put("tenSanPham", hdct.getSanPhamChiTiet().getSanPham() != null 
                    ? hdct.getSanPhamChiTiet().getSanPham().getTenSanPham() : "");
            item.put("maSanPham", hdct.getSanPhamChiTiet().getSanPham() != null 
                    ? hdct.getSanPhamChiTiet().getSanPham().getMaSanPham() : "");
            item.put("mauSac", hdct.getSanPhamChiTiet().getMauSac() != null 
                    ? hdct.getSanPhamChiTiet().getMauSac().getTenMauSac() : "");
            item.put("kichThuoc", hdct.getSanPhamChiTiet().getKichThuoc() != null 
                    ? hdct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc() : "");
            item.put("chatLieuDay", hdct.getSanPhamChiTiet().getChatLieuDay() != null 
                    ? hdct.getSanPhamChiTiet().getChatLieuDay().getTenChatLieu() : "");
            
            String tenBienThe = buildTenBienThe(
                    hdct.getSanPhamChiTiet().getMauSac(),
                    hdct.getSanPhamChiTiet().getKichThuoc(),
                    hdct.getSanPhamChiTiet().getChatLieuDay()
            );
            item.put("tenBienThe", tenBienThe);
            
            // Lấy hình ảnh sản phẩm
            String hinhAnh = null;
            if (hdct.getSanPhamChiTiet().getSanPham() != null) {
                hinhAnh = hdct.getSanPhamChiTiet().getSanPham().getHinhAnh();
            }
            item.put("hinhAnh", hinhAnh);
            
            item.put("soLuongMua", hdct.getSoLuong());
            item.put("donGia", hdct.getDonGia());
            item.put("thanhTien", hdct.getThanhTien());

            Integer daHoan = hoanTraChiTietRepository.sumSoLuongHoanTraByHoaDonChiTietId(hdct.getId());
            int soLuongDaHoan = daHoan != null ? daHoan : 0;
            int soLuongConLai = hdct.getSoLuong() - soLuongDaHoan;
            item.put("soLuongDaHoanTra", soLuongDaHoan);
            item.put("soLuongConLai", soLuongConLai);

            List<Map<String, Object>> serials = new ArrayList<>();
            if (hdct.getSerialSanPhams() != null) {
                for (SerialSanPham sp : hdct.getSerialSanPhams()) {
                    Map<String, Object> serialMap = new HashMap<>();
                    serialMap.put("id", sp.getId());
                    serialMap.put("maSerial", sp.getMaSerial());
                    serialMap.put("trangThai", sp.getTrangThai());
                    serialMap.put("trangThaiHienThi", getSerialTrangThaiText(sp.getTrangThai()));
                    serialMap.put("daHoan", sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG);
                    serialMap.put("coTheChon", sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_BAN);
                    serials.add(serialMap);
                }
            }
            item.put("serials", serials);
            item.put("soLuongSerial", serials.size());

            chiTietList.add(item);
        }
        result.put("chiTietList", chiTietList);
        return result;
    }

    @Override
    public Map<String, Object> getSerialCoTheTra(Integer hoaDonId, Integer khachHangId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

        if (khachHangId != null && hoaDon.getKhachHang() != null 
                && !khachHangId.equals(hoaDon.getKhachHang().getId())) {
            throw new RuntimeException("Bạn không có quyền xem hóa đơn này");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", hoaDon.getId());
        result.put("maDonHang", hoaDon.getMaDonHang());

        List<Map<String, Object>> sanPhamList = new ArrayList<>();
        if (hoaDon.getChiTietList() != null) {
            for (HoaDonChiTiet hdct : hoaDon.getChiTietList()) {
                Map<String, Object> spMap = new HashMap<>();
                spMap.put("idHoaDonChiTiet", hdct.getId());
                spMap.put("idSanPhamChiTiet", hdct.getSanPhamChiTiet().getId());
                spMap.put("tenSanPham", hdct.getSanPhamChiTiet().getSanPham() != null 
                        ? hdct.getSanPhamChiTiet().getSanPham().getTenSanPham() : "");
                
                String tenBienThe = buildTenBienThe(
                        hdct.getSanPhamChiTiet().getMauSac(),
                        hdct.getSanPhamChiTiet().getKichThuoc(),
                        hdct.getSanPhamChiTiet().getChatLieuDay()
                );
                spMap.put("tenBienThe", tenBienThe);
                spMap.put("donGia", hdct.getDonGia());

                Integer daHoan = hoanTraChiTietRepository.sumSoLuongHoanTraByHoaDonChiTietId(hdct.getId());
                int soLuongDaHoan = daHoan != null ? daHoan : 0;
                int soLuongConLai = hdct.getSoLuong() - soLuongDaHoan;
                spMap.put("soLuongMua", hdct.getSoLuong());
                spMap.put("soLuongDaHoanTra", soLuongDaHoan);
                spMap.put("soLuongConLai", soLuongConLai);

                List<Map<String, Object>> serialList = new ArrayList<>();
                if (hdct.getSerialSanPhams() != null) {
                    for (SerialSanPham sp : hdct.getSerialSanPhams()) {
                        Map<String, Object> serialMap = new HashMap<>();
                        serialMap.put("id", sp.getId());
                        serialMap.put("maSerial", sp.getMaSerial());
                        serialMap.put("trangThai", sp.getTrangThai());
                        serialMap.put("trangThaiHienThi", getSerialTrangThaiText(sp.getTrangThai()));
                        serialMap.put("daHoan", sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG);
                        serialMap.put("coTheChon", sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_BAN);
                        serialList.add(serialMap);
                    }
                }
                spMap.put("serials", serialList);
                spMap.put("soLuongSerial", serialList.size());
                spMap.put("soLuongSerialCoTheTra", serialList.stream()
                        .filter(s -> Boolean.TRUE.equals(s.get("coTheChon"))).count());

                sanPhamList.add(spMap);
            }
        }
        result.put("sanPhamList", sanPhamList);
        return result;
    }

    @Override
    @Transactional
    public HoanTraDTO createHoanTraKhachHang(HoanTraRequest request, Integer khachHangId) {
        HoaDon hoaDon = hoaDonRepository.findById(request.getIdHoaDon())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + request.getIdHoaDon()));

        if (khachHangId != null && hoaDon.getKhachHang() != null 
                && !khachHangId.equals(hoaDon.getKhachHang().getId())) {
            throw new RuntimeException("Bạn không có quyền tạo yêu cầu hoàn trả cho đơn hàng này");
        }

        String tt = hoaDon.getTrangThaiDonHang();
        boolean coTheHoanTra = "DA_GIAO".equals(tt) || "HOAN_THANH".equals(tt)
                || "DA_THANH_TOAN".equals(tt) || "DA THANH TOAN".equals(tt)
                || "DA_XAC_NHAN".equals(tt) || "DA_THANH_TOAN_ONL".equals(tt);
        if (!coTheHoanTra) {
            throw new RuntimeException("Đơn hàng có trạng thái '" + tt + "' không thể hoàn trả");
        }

        KhachHang khachHang = hoaDon.getKhachHang();
        if (khachHang == null) {
            khachHang = khachHangRepository.findById(1)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng mặc định"));
        }

        HoanTra hoanTra = new HoanTra();
        hoanTra.setMaHoanTra(generateMaHoanTra());
        hoanTra.setHoaDon(hoaDon);
        hoanTra.setKhachHang(khachHang);
        hoanTra.setLyDo(request.getLyDo());
        hoanTra.setLoaiHoanTra(request.getLoaiHoanTra() != null ? request.getLoaiHoanTra() : HoanTra.LOAI_TRA_HANG);
        hoanTra.setSoTienHoanTra(BigDecimal.ZERO);

        // Set thông tin tài khoản ngân hàng để hoàn tiền
        if (request.getSoTaiKhoan() != null && !request.getSoTaiKhoan().isEmpty()) {
            hoanTra.setSoTaiKhoan(request.getSoTaiKhoan());
            hoanTra.setTenNganHang(request.getTenNganHang());
            hoanTra.setTenChuTaiKhoan(request.getTenChuTaiKhoan());
        }

        // Set initial status
        hoanTra.setTrangThai(HoanTra.TRANG_THAI_CHO_XU_LY);

        hoanTra.setNgayYeuCau(LocalDateTime.now());

        hoanTra = hoanTraRepository.save(hoanTra);

        // Tính tổng số tiền hoàn từ các chi tiết (không bao gồm phí ship)
        BigDecimal tongTienTuChiTiet = BigDecimal.ZERO;

        if (request.getChiTietList() != null && !request.getChiTietList().isEmpty()) {
            for (HoanTraRequest.HoanTraChiTietRequest chiTietRequest : request.getChiTietList()) {
                HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(chiTietRequest.getIdHoaDonChiTiet())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn chi tiết với ID: " + chiTietRequest.getIdHoaDonChiTiet()));

                SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(chiTietRequest.getIdSanPhamChiTiet())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + chiTietRequest.getIdSanPhamChiTiet()));

                Integer daHoan = hoanTraChiTietRepository.sumSoLuongHoanTraByHoaDonChiTietId(hoaDonChiTiet.getId());
                int soLuongDaHoan = daHoan != null ? daHoan : 0;
                int soLuongConLai = hoaDonChiTiet.getSoLuong() - soLuongDaHoan;
                int soLuongYeuCau = chiTietRequest.getSoLuongHoanTra();

                if (soLuongYeuCau > soLuongConLai) {
                    throw new RuntimeException("Số lượng hoàn trả vượt quá số lượng có thể hoàn (" + soLuongConLai + ") cho sản phẩm " 
                            + sanPhamChiTiet.getSanPham().getTenSanPham());
                }

                // Lấy đơn giá từ hóa đơn chi tiết (cùng nguồn với trang create)
                BigDecimal donGia = hoaDonChiTiet.getDonGia();
                if (donGia == null) {
                    donGia = BigDecimal.ZERO;
                }
                BigDecimal soTienHoan = donGia.multiply(BigDecimal.valueOf(soLuongYeuCau));
                tongTienTuChiTiet = tongTienTuChiTiet.add(soTienHoan);

                HoanTraChiTiet chiTiet = new HoanTraChiTiet();
                chiTiet.setHoanTra(hoanTra);
                chiTiet.setSanPhamChiTiet(sanPhamChiTiet);
                chiTiet.setHoaDonChiTiet(hoaDonChiTiet);
                chiTiet.setSoLuongHoanTra(soLuongYeuCau);
                chiTiet.setDonGiaTaiThoiDiemMua(donGia);
                chiTiet.setSoTienHoan(soTienHoan);
                chiTiet.setHinhAnh(chiTietRequest.getHinhAnh());

                hoanTraChiTietRepository.save(chiTiet);

                if (chiTietRequest.getSerialsHoanTra() != null && !chiTietRequest.getSerialsHoanTra().isEmpty()) {
                    for (String serial : chiTietRequest.getSerialsHoanTra()) {
                        SerialSanPham serialSanPham = serialSanPhamRepository.findByMaSerial(serial.trim()).orElse(null);
                        if (serialSanPham != null) {
                            if (serialSanPham.getTrangThai() != SerialSanPham.TRANG_THAI_DA_BAN) {
                                throw new RuntimeException("Serial '" + serial + "' không ở trạng thái có thể hoàn trả");
                            }
                            if (serialSanPham.getHoaDonChiTiet() == null 
                                    || !serialSanPham.getHoaDonChiTiet().getId().equals(hoaDonChiTiet.getId())) {
                                throw new RuntimeException("Serial '" + serial + "' không thuộc sản phẩm đã chọn");
                            }
                            serialSanPham.setTrangThai(SerialSanPham.TRANG_THAI_DA_TRA_HANG);
                            serialSanPhamRepository.save(serialSanPham);
                        }
                    }
                }
            }
        }

        // Tính số tiền hoàn = tổng tiền từ chi tiết - tỷ lệ giảm giá (không cộng phí ship)
        BigDecimal tongTienTamTinh = hoaDon.getTongTienTamTinh() != null
                ? hoaDon.getTongTienTamTinh()
                : BigDecimal.ZERO;
        BigDecimal tienGiam = hoaDon.getTienGiam() != null
                ? hoaDon.getTienGiam()
                : BigDecimal.ZERO;

        BigDecimal tongTienHoan = tongTienTuChiTiet;
        // Áp dụng tỷ lệ giảm giá nếu có
        if (tongTienTamTinh.compareTo(BigDecimal.ZERO) > 0 && tienGiam.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tiLeGiam = tienGiam.divide(tongTienTamTinh, 4, java.math.RoundingMode.HALF_UP);
            BigDecimal soTienDuocGiam = tongTienTuChiTiet.multiply(tiLeGiam).setScale(0, java.math.RoundingMode.HALF_UP);
            tongTienHoan = tongTienTuChiTiet.subtract(soTienDuocGiam);
        }

        hoanTra.setSoTienHoanTra(tongTienHoan);
        hoanTra = hoanTraRepository.save(hoanTra);

        return convertToDTO(hoanTra);
    }

    private String buildTenBienThe(MauSac mauSac, KichThuoc kichThuoc, ChatLieuDay chatLieuDay) {
        List<String> parts = new ArrayList<>();
        if (mauSac != null && mauSac.getTenMauSac() != null) parts.add(mauSac.getTenMauSac());
        if (kichThuoc != null && kichThuoc.getTenKichThuoc() != null) parts.add(kichThuoc.getTenKichThuoc());
        if (chatLieuDay != null && chatLieuDay.getTenChatLieu() != null) parts.add(chatLieuDay.getTenChatLieu());
        return String.join(", ", parts);
    }

    private String getSerialTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        switch (trangThai) {
            case 0: return "Trong kho";
            case 1: return "Đã bán";
            case 2: return "Bảo hành";
            case 3: return "Đã hoàn trả";
            default: return "Không xác định";
        }
    }


    @Override
    public boolean existsByHoaDonId(Integer hoaDonId) {
        return hoanTraRepository.existsByHoaDonId(hoaDonId);
    }


    @Override
    @Transactional
    public HoanTraDTO getFirstHoanTraByHoaDonId(Integer hoaDonId) {
        return hoanTraRepository.findFirstByHoaDonIdWithChiTiet(hoaDonId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getHoaDonCoTheHoanTraForAdmin() {
        Map<String, Object> result = new HashMap<>();
        
        List<String> trangThaiCoTheHoan = Arrays.asList(
                "DA_GIAO", "HOAN_THANH", "DA_THANH_TOAN", "DA THANH TOAN", "DA_XAC_NHAN", "DA_THANH_TOAN_ONL"
        );
        
        List<HoaDon> hoaDonList = hoaDonRepository.findAll().stream()
                .filter(hd -> hd.getTrangThai() != null && hd.getTrangThai())
                .filter(hd -> trangThaiCoTheHoan.contains(hd.getTrangThaiDonHang()))
                .collect(Collectors.toList());
        
        List<Map<String, Object>> items = hoaDonList.stream()
                .map(hd -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", hd.getId());
                    item.put("maDonHang", hd.getMaDonHang());
                    item.put("tenKhachHang", hd.getTenKhachHang());
                    item.put("sdtKhachHang", hd.getSdtKhachHang());
                    item.put("ngayDat", hd.getNgayDat());
                    item.put("trangThaiDonHang", hd.getTrangThaiDonHang());
                    item.put("tongTienThanhToan", hd.getTongTienThanhToan());
                    
                    boolean daCoHoanTra = hoanTraRepository.existsByHoaDonId(hd.getId());
                    item.put("daCoHoanTra", daCoHoanTra);
                    
                    return item;
                })
                .collect(Collectors.toList());
        
        result.put("success", true);
        result.put("items", items);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getHoaDonChiTietForAdmin(Integer hoaDonId) {
        Map<String, Object> result = new HashMap<>();
        
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));
        
        List<String> trangThaiCoTheHoan = Arrays.asList(
                "DA_GIAO", "HOAN_THANH", "DA_THANH_TOAN", "DA THANH TOAN", "DA_XAC_NHAN", "DA_THANH_TOAN_ONL"
        );
        
        boolean coTheHoanTra = trangThaiCoTheHoan.contains(hoaDon.getTrangThaiDonHang());
        
        result.put("success", true);
        result.put("id", hoaDon.getId());
        result.put("maDonHang", hoaDon.getMaDonHang());
        result.put("ngayDat", hoaDon.getNgayDat());
        result.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
        result.put("tenKhachHang", hoaDon.getTenKhachHang());
        result.put("sdtKhachHang", hoaDon.getSdtKhachHang());
        result.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
        result.put("tongTienHoaDon", hoaDon.getTongTienTamTinh());
        // Tính phí giao hàng: tongTienThanhToan - tongTienTamTinh + tienGiam
        BigDecimal tienGiamVal = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
        BigDecimal phiVanChuyen = hoaDon.getPhiVanChuyen();
        if (phiVanChuyen == null) {
            phiVanChuyen = hoaDon.getTongTienThanhToan()
                    .subtract(hoaDon.getTongTienTamTinh())
                    .add(tienGiamVal);
            if (phiVanChuyen.compareTo(BigDecimal.ZERO) < 0) {
                phiVanChuyen = BigDecimal.ZERO;
            }
        }
        result.put("phiGiaoHang", phiVanChuyen);
        result.put("voucherGiam", tienGiamVal);
        result.put("coTheHoanTra", coTheHoanTra);
        
        List<Map<String, Object>> chiTietList = new ArrayList<>();
        List<HoaDonChiTiet> chiTietEntities = hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDonId);
        
        for (HoaDonChiTiet hdct : chiTietEntities) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", hdct.getId());
            item.put("idSanPhamChiTiet", hdct.getSanPhamChiTiet().getId());
            item.put("tenSanPham", hdct.getSanPhamChiTiet().getSanPham() != null 
                    ? hdct.getSanPhamChiTiet().getSanPham().getTenSanPham() : "");
            item.put("maSanPham", hdct.getSanPhamChiTiet().getSanPham() != null 
                    ? hdct.getSanPhamChiTiet().getSanPham().getMaSanPham() : "");
            
            String tenBienThe = buildTenBienThe(
                    hdct.getSanPhamChiTiet().getMauSac(),
                    hdct.getSanPhamChiTiet().getKichThuoc(),
                    hdct.getSanPhamChiTiet().getChatLieuDay()
            );
            item.put("tenBienThe", tenBienThe);
            
            // Lấy hình ảnh sản phẩm
            String hinhAnh = null;
            if (hdct.getSanPhamChiTiet().getSanPham() != null) {
                hinhAnh = hdct.getSanPhamChiTiet().getSanPham().getHinhAnh();
            }
            item.put("hinhAnh", hinhAnh);
            
            item.put("soLuongMua", hdct.getSoLuong());
            item.put("donGia", hdct.getDonGia());
            item.put("thanhTien", hdct.getThanhTien());
            
            Integer daHoan = hoanTraChiTietRepository.sumSoLuongHoanTraByHoaDonChiTietId(hdct.getId());
            int soLuongDaHoan = daHoan != null ? daHoan : 0;
            int soLuongConLai = hdct.getSoLuong() - soLuongDaHoan;
            item.put("soLuongDaHoanTra", soLuongDaHoan);
            item.put("soLuongConLai", soLuongConLai);
            
            List<Map<String, Object>> serials = new ArrayList<>();
            if (hdct.getSerialSanPhams() != null) {
                for (SerialSanPham sp : hdct.getSerialSanPhams()) {
                    Map<String, Object> serialMap = new HashMap<>();
                    serialMap.put("id", sp.getId());
                    serialMap.put("maSerial", sp.getMaSerial());
                    serialMap.put("trangThai", sp.getTrangThai());
                    serialMap.put("trangThaiHienThi", getSerialTrangThaiText(sp.getTrangThai()));
                    serialMap.put("daHoan", sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG);
                    serialMap.put("coTheChon", sp.getTrangThai() == SerialSanPham.TRANG_THAI_DA_BAN);
                    serials.add(serialMap);
                }
            }
            item.put("serials", serials);
            item.put("soLuongSerial", serials.size());
            
            chiTietList.add(item);
        }
        
        result.put("chiTietList", chiTietList);
        return result;
    }

    @Override
    @Transactional
    public HoanTraDTO xuLyHoanTraChiTiet(Integer hoanTraId, Integer hoanTraChiTietId, String ghiChuXuLy, Integer idNhanVienXuLy) {
        HoanTra hoanTra = hoanTraRepository.findByIdWithChiTiet(hoanTraId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + hoanTraId));

        if (!HoanTra.TRANG_THAI_CHO_XU_LY.equals(hoanTra.getTrangThai()) 
                && !HoanTra.TRANG_THAI_DANG_XU_LY.equals(hoanTra.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể xử lý hoàn trả đang chờ xử lý");
        }

        HoanTraChiTiet chiTiet = null;
        for (HoanTraChiTiet ct : hoanTra.getChiTietList()) {
            if (ct.getId().equals(hoanTraChiTietId)) {
                chiTiet = ct;
                break;
            }
        }
        
        if (chiTiet == null) {
            throw new RuntimeException("Không tìm thấy chi tiết hoàn trả với ID: " + hoanTraChiTietId);
        }

        KhachHang nhanVien = khachHangRepository.findById(idNhanVienXuLy)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + idNhanVienXuLy));

        SanPhamChiTiet sanPhamChiTiet = chiTiet.getSanPhamChiTiet();
        int currentStock = sanPhamChiTiet.getSoLuongTon() != null ? sanPhamChiTiet.getSoLuongTon() : 0;
        sanPhamChiTiet.setSoLuongTon(currentStock + chiTiet.getSoLuongHoanTra());
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_XU_LY);
        hoanTra.setNhanVienXuLy(nhanVien);
        hoanTra.setGhiChuXuLy(ghiChuXuLy);
        hoanTra.setNgayXuLy(LocalDateTime.now());

        hoanTra = hoanTraRepository.save(hoanTra);
        return convertToDTO(hoanTra);
    }

    @Override
    @Transactional
    public HoanTraDTO duyetDonTraHang(Integer id, boolean themVaoKho, Integer idNhanVienXuLy, String ghiChuXuLy) {
        HoanTra hoanTra = hoanTraRepository.findByIdWithChiTiet(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + id));

        KhachHang nhanVien = khachHangRepository.findById(idNhanVienXuLy)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + idNhanVienXuLy));

        String trangThaiHienTai = hoanTra.getTrangThai();
        String loaiHoanTra = hoanTra.getLoaiHoanTra();

        // === XỬ LÝ TRA_HANG WORKFLOW ===
        if (HoanTra.TRANG_THAI_CHO_XU_LY.equals(trangThaiHienTai)) {
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_DUYET);
            hoanTra.setNhanVienXuLy(nhanVien);
            hoanTra.setGhiChuXuLy(ghiChuXuLy);
            hoanTra.setNgayXuLy(LocalDateTime.now());
            hoanTra = hoanTraRepository.save(hoanTra);
            return convertToDTO(hoanTra);
        }

        if (HoanTra.TRANG_THAI_DA_DUYET.equals(trangThaiHienTai)) {
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_NHAN_HANG);
            hoanTra.setNhanVienXuLy(nhanVien);
            hoanTra.setGhiChuXuLy(ghiChuXuLy);
            hoanTra.setNgayXuLy(LocalDateTime.now());
            hoanTra = hoanTraRepository.save(hoanTra);
            return convertToDTO(hoanTra);
        }

        if (HoanTra.TRANG_THAI_DA_NHAN_HANG.equals(trangThaiHienTai)) {
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_HOAN_TIEN);
            hoanTra.setNhanVienXuLy(nhanVien);
            hoanTra.setGhiChuXuLy(ghiChuXuLy);
            hoanTra.setNgayXuLy(LocalDateTime.now());
            hoanTra.setNgayHoanTien(LocalDateTime.now());

            if (themVaoKho) {
                for (HoanTraChiTiet chiTiet : hoanTra.getChiTietList()) {
                    SanPhamChiTiet sanPhamChiTiet = chiTiet.getSanPhamChiTiet();
                    int currentStock = sanPhamChiTiet.getSoLuongTon() != null ? sanPhamChiTiet.getSoLuongTon() : 0;
                    sanPhamChiTiet.setSoLuongTon(currentStock + chiTiet.getSoLuongHoanTra());
                    sanPhamChiTietRepository.save(sanPhamChiTiet);

                    if (chiTiet.getHoaDonChiTiet() != null) {
                        List<SerialSanPham> serials = serialSanPhamRepository
                                .findByHoaDonChiTietIdOrderByIdAsc(chiTiet.getHoaDonChiTiet().getId());
                        for (SerialSanPham serial : serials) {
                            if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
                                serial.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                                serial.setHoaDonChiTiet(null);
                                serialSanPhamRepository.save(serial);
                            }
                        }
                    }
                }
            }

            hoanTra = hoanTraRepository.save(hoanTra);
            return convertToDTO(hoanTra);
        }

        throw new RuntimeException("Không thể duyệt đơn trả hàng ở trạng thái: " + trangThaiHienTai);
    }





    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getBuocXuLyNext(Integer hoanTraId) {
        Map<String, Object> result = new HashMap<>();
        HoanTra hoanTra = hoanTraRepository.findById(hoanTraId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + hoanTraId));

        String trangThaiHienTai = hoanTra.getTrangThai();
        String loaiHoanTra = hoanTra.getLoaiHoanTra();
        result.put("trangThaiHienTai", trangThaiHienTai);
        result.put("trangThaiHienThi", getTrangThaiHienThi(trangThaiHienTai));
        result.put("loaiHoanTra", loaiHoanTra);

        // === TRA_HANG workflow actions ===
        boolean canXacNhanNhanHang = HoanTra.TRANG_THAI_DA_DUYET.equals(trangThaiHienTai);
        boolean canHoanTien = HoanTra.TRANG_THAI_DA_NHAN_HANG.equals(trangThaiHienTai);
        boolean canDuyet = HoanTra.TRANG_THAI_CHO_XU_LY.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_DUYET.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_NHAN_HANG.equals(trangThaiHienTai);
        boolean canTuChoi = HoanTra.TRANG_THAI_CHO_XU_LY.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_DUYET.equals(trangThaiHienTai);

        result.put("canXacNhanNhanHang", canXacNhanNhanHang);
        result.put("canHoanTien", canHoanTien);
        result.put("canDuyet", canDuyet);
        result.put("canTuChoi", canTuChoi);
        result.put("daHoanTien", HoanTra.TRANG_THAI_DA_HOAN_TIEN.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_XU_LY.equals(trangThaiHienTai));

        String trangThaiText = "";
        if (HoanTra.TRANG_THAI_CHO_XU_LY.equals(trangThaiHienTai)) {
            trangThaiText = "Đơn chờ duyệt";
        } else if (HoanTra.TRANG_THAI_DA_DUYET.equals(trangThaiHienTai)) {
            trangThaiText = "Đã duyệt - Chờ nhận hàng";
        } else if (HoanTra.TRANG_THAI_DA_NHAN_HANG.equals(trangThaiHienTai)) {
            trangThaiText = "Đã nhận hàng - Sẵn sàng hoàn tiền";
        } else if (HoanTra.TRANG_THAI_DA_HOAN_TIEN.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_XU_LY.equals(trangThaiHienTai)) {
            trangThaiText = "Đã hoàn tiền";
        } else if (HoanTra.TRANG_THAI_TU_CHOI.equals(trangThaiHienTai)) {
            trangThaiText = "Từ chối";
        }
        result.put("trangThaiText", trangThaiText);

        return result;
    }


    @Override
    @Transactional
    public HoanTraDTO doiTrangThai(Integer id, String trangThaiMoi, Integer idNhanVienXuLy, Boolean themVaoKho, String ghiChuXuLy) {
        HoanTra hoanTra = hoanTraRepository.findByIdWithChiTiet(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + id));

        String trangThaiHienTai = hoanTra.getTrangThai();
        String loaiHoanTra = hoanTra.getLoaiHoanTra();

        KhachHang nhanVien = khachHangRepository.findById(idNhanVienXuLy)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + idNhanVienXuLy));

        // Validate transition
        boolean validTransition = isValidTransition(loaiHoanTra, trangThaiHienTai, trangThaiMoi);
        if (!validTransition) {
            throw new RuntimeException("Không thể chuyển từ trạng thái '" + getTrangThaiHienThi(trangThaiHienTai) + 
                    "' sang '" + getTrangThaiHienThi(trangThaiMoi) + "'");
        }

        // Execute the transition
        hoanTra.setTrangThai(trangThaiMoi);
        hoanTra.setNhanVienXuLy(nhanVien);
        if (ghiChuXuLy != null && !ghiChuXuLy.isEmpty()) {
            hoanTra.setGhiChuXuLy(ghiChuXuLy);
        }
        hoanTra.setNgayXuLy(LocalDateTime.now());

        // Execute side effects based on transition
        executeTransitionEffects(hoanTra, trangThaiHienTai, trangThaiMoi, themVaoKho);

        hoanTra = hoanTraRepository.save(hoanTra);
        return convertToDTO(hoanTra);
    }

    private boolean isValidTransition(String loaiHoanTra, String currentStatus, String newStatus) {
        if (loaiHoanTra == null) return false;

        // TRA_HANG workflow
        switch (currentStatus) {
            case HoanTra.TRANG_THAI_CHO_XU_LY:
                return HoanTra.TRANG_THAI_DA_DUYET.equals(newStatus)
                        || HoanTra.TRANG_THAI_TU_CHOI.equals(newStatus);
            case HoanTra.TRANG_THAI_DA_DUYET:
                return HoanTra.TRANG_THAI_DA_NHAN_HANG.equals(newStatus)
                        || HoanTra.TRANG_THAI_TU_CHOI.equals(newStatus);
            case HoanTra.TRANG_THAI_DA_NHAN_HANG:
                return HoanTra.TRANG_THAI_DA_HOAN_TIEN.equals(newStatus);
            default:
                return false;
        }
    }

    private void executeTransitionEffects(HoanTra hoanTra, String fromStatus, String toStatus, Boolean themVaoKho) {
        // If rejected (TU_CHOI), restore serial status
        if (HoanTra.TRANG_THAI_TU_CHOI.equals(toStatus)) {
            List<HoanTraChiTiet> chiTietList = hoanTraChiTietRepository.findByHoanTraId(hoanTra.getId());
            for (HoanTraChiTiet chiTiet : chiTietList) {
                if (chiTiet.getHoaDonChiTiet() != null) {
                    List<SerialSanPham> serials = serialSanPhamRepository
                            .findByHoaDonChiTietIdOrderByIdAsc(chiTiet.getHoaDonChiTiet().getId());
                    for (SerialSanPham serial : serials) {
                        if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
                            serial.setTrangThai(SerialSanPham.TRANG_THAI_DA_BAN);
                            serial.setGhiChu("Từ chối trả hàng - Serial trả lại cho khách");
                            serialSanPhamRepository.save(serial);
                        }
                    }
                }
            }
        }

        // If transitioning to DA_HOAN_TIEN and themVaoKho is true, add products back to stock
        if (HoanTra.TRANG_THAI_DA_HOAN_TIEN.equals(toStatus) && Boolean.TRUE.equals(themVaoKho)) {
            List<HoanTraChiTiet> chiTietList = hoanTraChiTietRepository.findByHoanTraId(hoanTra.getId());
            for (HoanTraChiTiet chiTiet : chiTietList) {
                SanPhamChiTiet sanPhamChiTiet = chiTiet.getSanPhamChiTiet();
                int currentStock = sanPhamChiTiet.getSoLuongTon() != null ? sanPhamChiTiet.getSoLuongTon() : 0;
                sanPhamChiTiet.setSoLuongTon(currentStock + chiTiet.getSoLuongHoanTra());
                sanPhamChiTietRepository.save(sanPhamChiTiet);

                // Also update serial status back to in-stock
                if (chiTiet.getHoaDonChiTiet() != null) {
                    List<SerialSanPham> serials = serialSanPhamRepository
                            .findByHoaDonChiTietIdOrderByIdAsc(chiTiet.getHoaDonChiTiet().getId());
                    for (SerialSanPham serial : serials) {
                        if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
                            serial.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                            serial.setHoaDonChiTiet(null);
                            serialSanPhamRepository.save(serial);
                        }
                    }
                }
            }
        }
    }

    @Override
    public HoanTraUocTinhDTO tinhSoTienHoanUocTinh(Integer hoaDonId, Integer khachHangId, List<Map<String, Object>> chiTietList) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElse(null);

        if (hoaDon == null) {
            return HoanTraUocTinhDTO.builder()
                    .coTheHoanTra(false)
                    .loiThuong("Không tìm thấy hóa đơn")
                    .build();
        }

        // Kiểm tra khách hàng
        if (khachHangId != null && !khachHangId.equals(hoaDon.getKhachHang().getId())) {
            return HoanTraUocTinhDTO.builder()
                    .coTheHoanTra(false)
                    .loiThuong("Hóa đơn không thuộc về khách hàng này")
                    .build();
        }

        // Lấy thông tin hóa đơn gốc
        BigDecimal tongTienTamTinhGoc = hoaDon.getTongTienTamTinh() != null ? hoaDon.getTongTienTamTinh() : BigDecimal.ZERO;
        BigDecimal tienGiamGoc = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
        BigDecimal phiVanChuyen = hoaDon.getPhiVanChuyen() != null ? hoaDon.getPhiVanChuyen() : BigDecimal.ZERO;
        BigDecimal tongTienThanhToanGoc = hoaDon.getTongTienThanhToan() != null ? hoaDon.getTongTienThanhToan() : BigDecimal.ZERO;

        // Tính tổng tiền hàng hoàn và số lượng
        BigDecimal tongTienMatHangHoan = BigDecimal.ZERO;
        int soLuongHoan = 0;

        if (chiTietList != null && !chiTietList.isEmpty()) {
            for (Map<String, Object> chiTiet : chiTietList) {
                BigDecimal donGia = BigDecimal.ZERO;
                Integer soLuong = 0;

                Object donGiaObj = chiTiet.get("donGia");
                Object soLuongObj = chiTiet.get("soLuongHoanTra");

                if (donGiaObj instanceof BigDecimal) {
                    donGia = (BigDecimal) donGiaObj;
                } else if (donGiaObj instanceof Number) {
                    donGia = BigDecimal.valueOf(((Number) donGiaObj).doubleValue());
                }

                if (soLuongObj instanceof Number) {
                    soLuong = ((Number) soLuongObj).intValue();
                }

                tongTienMatHangHoan = tongTienMatHangHoan.add(donGia.multiply(BigDecimal.valueOf(soLuong)));
                soLuongHoan += soLuong;
            }
        }

        // Tính voucher giảm theo tỷ lệ
        BigDecimal tienVoucherGiam = BigDecimal.ZERO;
        if (tongTienTamTinhGoc.compareTo(BigDecimal.ZERO) > 0 && tienGiamGoc.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tiLe = tongTienMatHangHoan.divide(tongTienTamTinhGoc, 4, java.math.RoundingMode.HALF_UP);
            tienVoucherGiam = tienGiamGoc.multiply(tiLe).setScale(0, java.math.RoundingMode.HALF_UP);
        }

        // Tính số tiền hoàn ước tính = tổng tiền hàng hoàn - voucher giảm (tỷ lệ) - phí vận chuyển
        BigDecimal soTienHoanUocTinh = tongTienMatHangHoan.subtract(tienVoucherGiam);
        if (phiVanChuyen.compareTo(BigDecimal.ZERO) > 0) {
            soTienHoanUocTinh = soTienHoanUocTinh.subtract(phiVanChuyen);
        }
        if (soTienHoanUocTinh.compareTo(BigDecimal.ZERO) < 0) {
            soTienHoanUocTinh = BigDecimal.ZERO;
        }

        return HoanTraUocTinhDTO.builder()
                .idHoaDon(hoaDonId)
                .maDonHang(hoaDon.getMaDonHang())
                .tongTienMatHang(tongTienMatHangHoan)
                .phiVanChuyen(phiVanChuyen)
                .tienVoucherGiam(tienVoucherGiam)
                .soTienHoanUocTinh(soTienHoanUocTinh)
                .soLuongHoan(soLuongHoan)
                .tongTienTamTinhGoc(tongTienTamTinhGoc)
                .tienGiamGoc(tienGiamGoc)
                .tongTienThanhToanGoc(tongTienThanhToanGoc)
                .coTheHoanTra(true)
                .loiThuong(null)
                .build();
    }
}
