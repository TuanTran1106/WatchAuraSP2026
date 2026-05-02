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
    public Map<String, Object> getHoanTraPaged(int page, int size, String trangThai, String keyword) {
        Map<String, Object> result = new HashMap<>();

        Page<HoanTra> hoanTraPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            hoanTraPage = hoanTraRepository.searchHoanTra(
                    trangThai != null && !trangThai.isEmpty() ? trangThai : null,
                    keyword.trim(),
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayYeuCau"))
            );
        } else if (trangThai != null && !trangThai.isEmpty()) {
            hoanTraPage = hoanTraRepository.findByTrangThaiOrderByNgayYeuCauDesc(
                    trangThai,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayYeuCau"))
            );
        } else {
            hoanTraPage = hoanTraRepository.findAllOrderByNgayYeuCauDesc(
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "ngayYeuCau"))
            );
        }

        List<HoanTraDTO> content = hoanTraPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        result.put("content", content);
        result.put("totalElements", hoanTraPage.getTotalElements());
        result.put("totalPages", hoanTraPage.getTotalPages());
        result.put("currentPage", page);
        result.put("pageSize", size);
        result.put("hasNext", hoanTraPage.hasNext());
        result.put("hasPrevious", hoanTraPage.hasPrevious());

        // Stats for TRA_HANG
        long choXuLy = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_CHO_XU_LY.equals(h.getTrangThai())).count();
        long dangXuLy = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_DANG_XU_LY.equals(h.getTrangThai())).count();
        long daXuLy = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_DA_XU_LY.equals(h.getTrangThai())).count();
        long tuChoi = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_TU_CHOI.equals(h.getTrangThai())).count();

        // Stats for DOI_HANG
        long choDuyetDoi = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_CHO_DUYET_DOI.equals(h.getTrangThai())).count();
        long daDuyetDoi = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_DA_DUYET_DOI.equals(h.getTrangThai())).count();
        long daNhanHangDoi = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI.equals(h.getTrangThai())).count();
        long chonSerialMoi = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_CHON_SERIAL_MOI.equals(h.getTrangThai())).count();
        long daDoi = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_DA_DOI.equals(h.getTrangThai())).count();
        long ketThuc = hoanTraPage.getContent().stream()
                .filter(h -> HoanTra.TRANG_THAI_KET_THUC.equals(h.getTrangThai())).count();

        long totalChoXuLy = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_CHO_XU_LY);
        long totalDangXuLy = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_DANG_XU_LY);
        long totalDaXuLy = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_DA_XU_LY);
        long totalTuChoi = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_TU_CHOI);

        // DOI_HANG total counts
        long totalChoDuyetDoi = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_CHO_DUYET_DOI);
        long totalDaDuyetDoi = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_DA_DUYET_DOI);
        long totalDaNhanHangDoi = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI);
        long totalChonSerialMoi = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_CHON_SERIAL_MOI);
        long totalDaDoi = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_DA_DOI);
        long totalKetThuc = hoanTraRepository.countByTrangThai(HoanTra.TRANG_THAI_KET_THUC);

        result.put("stats", new HashMap<>() {{
            put("choXuLy", choXuLy);
            put("dangXuLy", dangXuLy);
            put("daXuLy", daXuLy);
            put("tuChoi", tuChoi);
            put("totalChoXuLy", totalChoXuLy);
            put("totalDangXuLy", totalDangXuLy);
            put("totalDaXuLy", totalDaXuLy);
            put("totalTuChoi", totalTuChoi);
            // DOI_HANG stats
            put("choDuyetDoi", choDuyetDoi);
            put("daDuyetDoi", daDuyetDoi);
            put("daNhanHangDoi", daNhanHangDoi);
            put("chonSerialMoi", chonSerialMoi);
            put("daDoi", daDoi);
            put("ketThuc", ketThuc);
            put("totalChoDuyetDoi", totalChoDuyetDoi);
            put("totalDaDuyetDoi", totalDaDuyetDoi);
            put("totalDaNhanHangDoi", totalDaNhanHangDoi);
            put("totalChonSerialMoi", totalChonSerialMoi);
            put("totalDaDoi", totalDaDoi);
            put("totalKetThuc", totalKetThuc);
            put("tongSo", hoanTraPage.getTotalElements());
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

        // Set initial status based on type
        if (HoanTra.LOAI_DOI_HANG.equals(request.getLoaiHoanTra())) {
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_CHO_DUYET_DOI);
        } else {
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_CHO_XU_LY);
        }

        hoanTra.setSoTienHoanTra(BigDecimal.ZERO);
        hoanTra.setNgayYeuCau(LocalDateTime.now());

        hoanTra = hoanTraRepository.save(hoanTra);

        // Số tiền hoàn = tổng tiền thanh toán của hóa đơn gốc
        BigDecimal tongTienHoan = hoaDon.getTongTienThanhToan() != null 
                ? hoaDon.getTongTienThanhToan() 
                : BigDecimal.ZERO;

        if (request.getChiTietList() != null && !request.getChiTietList().isEmpty()) {
            for (HoanTraRequest.HoanTraChiTietRequest chiTietRequest : request.getChiTietList()) {
                HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(chiTietRequest.getIdHoaDonChiTiet())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn chi tiết với ID: " + chiTietRequest.getIdHoaDonChiTiet()));

                SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(chiTietRequest.getIdSanPhamChiTiet())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + chiTietRequest.getIdSanPhamChiTiet()));

                // Lấy đơn giá gốc từ SanPhamChiTiet (chưa trừ khuyến mãi)
                BigDecimal donGia = sanPhamChiTiet.getGiaBan();
                if (donGia == null) {
                    donGia = BigDecimal.ZERO;
                }
                BigDecimal soTienHoan = donGia.multiply(BigDecimal.valueOf(chiTietRequest.getSoLuongHoanTra()));

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
        String loaiHoanTra = hoanTra.getLoaiHoanTra();

        // Kiểm tra trạng thái có thể từ chối
        boolean coTheTuChoi;
        if (HoanTra.LOAI_DOI_HANG.equals(loaiHoanTra)) {
            coTheTuChoi = HoanTra.TRANG_THAI_CHO_DUYET_DOI.equals(trangThaiHienTai)
                    || HoanTra.TRANG_THAI_DA_DUYET_DOI.equals(trangThaiHienTai)
                    || HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI.equals(trangThaiHienTai)
                    || HoanTra.TRANG_THAI_CHON_SERIAL_MOI.equals(trangThaiHienTai);
        } else {
            coTheTuChoi = HoanTra.TRANG_THAI_CHO_XU_LY.equals(trangThaiHienTai)
                    || HoanTra.TRANG_THAI_DANG_XU_LY.equals(trangThaiHienTai)
                    || HoanTra.TRANG_THAI_DA_DUYET.equals(trangThaiHienTai)
                    || HoanTra.TRANG_THAI_DA_NHAN_HANG.equals(trangThaiHienTai);
        }

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
                    serial.setGhiChu("Từ chối đổi/trả hàng - Serial trả lại cho khách");
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
                // DOI_HANG fields
                .serialCuLoiHienThi(getSerialCuLoiHienThi(hoanTra.getLoaiHoanTra(), hoanTra.getTrangThai()))
                .build();

        if (hoanTra.getChiTietList() != null && !hoanTra.getChiTietList().isEmpty()) {
            List<HoanTraChiTietDTO> chiTietDTOs = new ArrayList<>();
            List<String> allSerialsMoi = new ArrayList<>();
            for (HoanTraChiTiet chiTiet : hoanTra.getChiTietList()) {
                HoanTraChiTietDTO chiTietDTO = convertChiTietToDTO(chiTiet);
                chiTietDTOs.add(chiTietDTO);
                // Collect all serial mới from chi tiết
                if (chiTietDTO.getSerialMoi() != null && !chiTietDTO.getSerialMoi().isEmpty()) {
                    allSerialsMoi.add(chiTietDTO.getSerialMoi());
                }
            }
            dto.setChiTietList(chiTietDTOs);
            dto.setSerialsMoiList(allSerialsMoi);
        }

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
                // DOI_HANG fields
                .serialMoi(chiTiet.getSerialMoi())
                .serialCuLoi(chiTiet.getSerialCuLoi())
                .serialCuLoiHienThi(getSerialCuLoiHienThiChiTiet(chiTiet.getSerialCuLoi()))
                .build();

        List<String> serialsHoanTra = new ArrayList<>();
        List<HoanTraChiTietDTO.SerialInfo> serialsChiTiet = new ArrayList<>();

        // Kiểm tra xem có đơn đổi hàng không - nếu có, chỉ hiển thị serial mới
        HoanTra hoanTraEntity = chiTiet.getHoanTra();
        boolean coDonDoiHangDaHoanTat = hoanTraEntity != null 
                && HoanTra.LOAI_DOI_HANG.equals(hoanTraEntity.getLoaiHoanTra())
                && (HoanTra.TRANG_THAI_DA_DOI.equals(hoanTraEntity.getTrangThai())
                    || HoanTra.TRANG_THAI_KET_THUC.equals(hoanTraEntity.getTrangThai()));

        // Nếu có serial mới từ đổi hàng và trạng thái đã đổi/kết thúc → chỉ hiển thị serial mới
        if (coDonDoiHangDaHoanTat && chiTiet.getSerialMoi() != null && !chiTiet.getSerialMoi().isEmpty()) {
            serialsHoanTra.add(chiTiet.getSerialMoi());
            HoanTraChiTietDTO.SerialInfo siMoi = HoanTraChiTietDTO.SerialInfo.builder()
                    .maSerial(chiTiet.getSerialMoi())
                    .trangThai(String.valueOf(SerialSanPham.TRANG_THAI_DA_BAN))
                    .trangThaiHienThi("Serial mới đã đổi")
                    .daDuocChon(true)
                    .build();
            serialsChiTiet.add(siMoi);
        } else if (chiTiet.getHoaDonChiTiet() != null && chiTiet.getHoaDonChiTiet().getSerialSanPhams() != null) {
            // Không có đổi hàng hoặc chưa hoàn tất → hiển thị serials theo logic cũ
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
            // === DOI_HANG statuses ===
            case HoanTra.TRANG_THAI_CHO_DUYET_DOI:
                return "Chờ duyệt đổi";
            case HoanTra.TRANG_THAI_DA_DUYET_DOI:
                return "Đã duyệt đổi";
            case HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI:
                return "Đã nhận hàng";
            case HoanTra.TRANG_THAI_CHON_SERIAL_MOI:
                return "Chọn serial mới";
            case HoanTra.TRANG_THAI_DA_DOI:
                return "Đã đổi";
            case HoanTra.TRANG_THAI_KET_THUC:
                return "Kết thúc";
            default:
                return trangThai;
        }
    }

    private String getLoaiHoanTraHienThi(String loaiHoanTra) {
        if (loaiHoanTra == null) return "Không xác định";
        switch (loaiHoanTra) {
            case HoanTra.LOAI_TRA_HANG:
                return "Trả hàng";
            case HoanTra.LOAI_DOI_HANG:
                return "Đổi hàng";
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
                || "DA_XAC_NHAN".equals(tt);
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
                || "DA_XAC_NHAN".equals(tt);
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

        // Set initial status based on type
        if (HoanTra.LOAI_DOI_HANG.equals(hoanTra.getLoaiHoanTra())) {
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_CHO_DUYET_DOI);
        } else {
            hoanTra.setTrangThai(HoanTra.TRANG_THAI_CHO_XU_LY);
        }

        hoanTra.setNgayYeuCau(LocalDateTime.now());

        hoanTra = hoanTraRepository.save(hoanTra);

        // Số tiền hoàn = tổng tiền thanh toán của hóa đơn gốc
        BigDecimal tongTienHoan = hoaDon.getTongTienThanhToan() != null 
                ? hoaDon.getTongTienThanhToan() 
                : BigDecimal.ZERO;

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

                // Lấy đơn giá gốc từ SanPhamChiTiet (chưa trừ khuyến mãi)
                BigDecimal donGia = sanPhamChiTiet.getGiaBan();
                if (donGia == null) {
                    donGia = BigDecimal.ZERO;
                }
                BigDecimal soTienHoan = donGia.multiply(BigDecimal.valueOf(soLuongYeuCau));

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

    // === DOI_HANG Helper Methods ===

    private String getSerialCuLoiHienThi(String loaiHoanTra, String trangThai) {
        if (!HoanTra.LOAI_DOI_HANG.equals(loaiHoanTra)) {
            return null;
        }
        if (HoanTra.TRANG_THAI_KET_THUC.equals(trangThai) ||
            HoanTra.TRANG_THAI_DA_DOI.equals(trangThai)) {
            return "Serial cũ đã được xử lý";
        }
        return null;
    }

    private String getSerialCuLoiHienThiChiTiet(Boolean serialCuLoi) {
        if (serialCuLoi == null) {
            return "Chưa xác định";
        }
        return serialCuLoi ? "Serial cũ bị lỗi" : "Serial mới đã đổi";
    }

    @Override
    public boolean existsByHoaDonId(Integer hoaDonId) {
        return hoanTraRepository.existsByHoaDonId(hoaDonId);
    }

    /**
     * Lấy serial trong kho có thể đổi cho sản phẩm
     * Trả về danh sách serial cùng loại sản phẩm (cùng SanPhamChiTiet) đang ở trạng thái TRONG_KHO
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getSerialCoTheDoi(Integer hoaDonId, Integer sanPhamChiTietId) {
        Map<String, Object> result = new HashMap<>();

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(sanPhamChiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + sanPhamChiTietId));

        // Tìm tất cả serial cùng loại (cùng sanPhamChiTiet) đang trong kho
        List<SerialSanPham> serials = serialSanPhamRepository
                .findBySanPhamChiTietIdAndTrangThai(sanPhamChiTietId, SerialSanPham.TRANG_THAI_TRONG_KHO);

        List<Map<String, Object>> serialList = new ArrayList<>();
        for (SerialSanPham serial : serials) {
            Map<String, Object> serialMap = new HashMap<>();
            serialMap.put("id", serial.getId());
            serialMap.put("maSerial", serial.getMaSerial());
            serialMap.put("trangThai", serial.getTrangThai());
            serialMap.put("trangThaiHienThi", getSerialTrangThaiText(serial.getTrangThai()));
            serialList.add(serialMap);
        }

        result.put("sanPhamChiTietId", sanPhamChiTietId);
        result.put("tenSanPham", spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "");
        result.put("soLuongSerialTrongKho", serialList.size());
        result.put("serialList", serialList);

        return result;
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
                "DA_GIAO", "HOAN_THANH", "DA_THANH_TOAN", "DA THANH TOAN", "DA_XAC_NHAN"
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
                "DA_GIAO", "HOAN_THANH", "DA_THANH_TOAN", "DA THANH TOAN", "DA_XAC_NHAN"
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

        // === XỬ LÝ DOI_HANG WORKFLOW ===
        if (HoanTra.LOAI_DOI_HANG.equals(loaiHoanTra)) {
            return xuLyDoiHangWorkflow(hoanTra, trangThaiHienTai, nhanVien, ghiChuXuLy);
        }

        // === XỬ LÝ TRA_HANG WORKFLOW (giữ nguyên logic cũ) ===
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

    /**
     * XỬ LÝ LUỒNG DOI_HANG
     * LUỒNG: CHỜ DUYỆT → ĐÃ DUYỆT → ĐÃ NHẬN HÀNG → CHỌN SERIAL MỚI → ĐÃ ĐỔI → KẾT THÚC
     *
     * - SR cũ → LỖI (đánh dấu serial cũ bị lỗi)
     * - SR mới → ĐÃ BÁN (serial mới được đổi, đánh dấu là đã bán cho khách)
     */
    private HoanTraDTO xuLyDoiHangWorkflow(HoanTra hoanTra, String trangThaiHienTai,
                                            KhachHang nhanVien, String ghiChuXuLy) {
        switch (trangThaiHienTai) {
            case HoanTra.TRANG_THAI_CHO_DUYET_DOI:
                // Bước 1: CHỜ DUYỆT → ĐÃ DUYỆT
                hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_DUYET_DOI);
                hoanTra.setNhanVienXuLy(nhanVien);
                hoanTra.setGhiChuXuLy(ghiChuXuLy);
                hoanTra.setNgayXuLy(LocalDateTime.now());
                hoanTra = hoanTraRepository.save(hoanTra);
                return convertToDTO(hoanTra);

            case HoanTra.TRANG_THAI_DA_DUYET_DOI:
                // Bước 2: ĐÃ DUYỆT → ĐÃ NHẬN HÀNG
                hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI);
                hoanTra.setNhanVienXuLy(nhanVien);
                hoanTra.setGhiChuXuLy(ghiChuXuLy);
                hoanTra.setNgayXuLy(LocalDateTime.now());
                hoanTra = hoanTraRepository.save(hoanTra);
                return convertToDTO(hoanTra);

            case HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI:
                // Bước 3: ĐÃ NHẬN HÀNG → CHỌN SERIAL MỚI
                hoanTra.setTrangThai(HoanTra.TRANG_THAI_CHON_SERIAL_MOI);
                hoanTra.setNhanVienXuLy(nhanVien);
                hoanTra.setGhiChuXuLy(ghiChuXuLy);
                hoanTra.setNgayXuLy(LocalDateTime.now());
                hoanTra = hoanTraRepository.save(hoanTra);
                return convertToDTO(hoanTra);

            default:
                throw new RuntimeException("Không thể duyệt đơn đổi hàng ở trạng thái: " + trangThaiHienTai);
        }
    }

    /**
     * Xử lý bước CHỌN SERIAL MỚI - Chọn serial và cấp cho khách
     * - Chọn serial mới từ danh sách trong kho
     * - Trừ số lượng tồn kho
     * - Cập nhật serial mới vào chi tiết
     * - Chuyển trạng thái sang ĐÃ ĐỔI
     */
    @Override
    @Transactional
    public HoanTraDTO xuLyDoiHangHoanTat(Integer hoanTraId, Map<Integer, String> serialsMoi,
                                          Integer idNhanVienXuLy,
                                          String ghiChuXuLy) {
        HoanTra hoanTra = hoanTraRepository.findByIdWithChiTiet(hoanTraId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + hoanTraId));

        if (!HoanTra.LOAI_DOI_HANG.equals(hoanTra.getLoaiHoanTra())) {
            throw new RuntimeException("Chỉ áp dụng cho loại đổi hàng");
        }

        if (!HoanTra.TRANG_THAI_CHON_SERIAL_MOI.equals(hoanTra.getTrangThai())) {
            throw new RuntimeException("Đơn đổi hàng phải ở trạng thái 'Chọn serial mới' để thực hiện bước này");
        }

        KhachHang nhanVien = khachHangRepository.findById(idNhanVienXuLy)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + idNhanVienXuLy));

        // Xử lý từng chi tiết - cấp serial mới
        for (HoanTraChiTiet chiTiet : hoanTra.getChiTietList()) {
            Integer chiTietId = chiTiet.getId();
            String serialMoi = serialsMoi != null ? serialsMoi.get(chiTietId) : null;

            if (serialMoi == null || serialMoi.isEmpty()) {
                throw new RuntimeException("Vui lòng chọn serial mới cho tất cả sản phẩm");
            }

            // Cấp serial mới cho khách
            capSerialMoiChoKhach(chiTiet, serialMoi, hoanTra);

            // Cập nhật chi tiết - lưu serial mới
            chiTiet.setSerialMoi(serialMoi);
            hoanTraChiTietRepository.save(chiTiet);
        }

        // Cập nhật trạng thái phiếu đổi → ĐÃ ĐỔI
        hoanTra.setTrangThai(HoanTra.TRANG_THAI_DA_DOI);
        hoanTra.setNhanVienXuLy(nhanVien);
        hoanTra.setGhiChuXuLy(ghiChuXuLy);
        hoanTra.setNgayXuLy(LocalDateTime.now());
        hoanTra = hoanTraRepository.save(hoanTra);

        return convertToDTO(hoanTra);
    }

    /**
     * Hoàn tất đổi hàng - Xử lý serial cũ
     * - Hỏi admin: Serial cũ có lưu vào kho không hay lỗi?
     * - Nếu luuKho = true: serial cũ → TRONG_KHO (hàng bình thường)
     * - Nếu luuKho = false: serial cũ → LỖI (hàng lỗi, không bán)
     */
    @Override
    @Transactional
    public HoanTraDTO hoanTatDoiHang(Integer hoanTraId, Map<Integer, Boolean> serialCuLoi,
                                       Integer idNhanVienXuLy, String ghiChuXuLy) {
        HoanTra hoanTra = hoanTraRepository.findByIdWithChiTiet(hoanTraId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + hoanTraId));

        if (!HoanTra.LOAI_DOI_HANG.equals(hoanTra.getLoaiHoanTra())) {
            throw new RuntimeException("Chỉ áp dụng cho loại đổi hàng");
        }

        if (!HoanTra.TRANG_THAI_DA_DOI.equals(hoanTra.getTrangThai())) {
            throw new RuntimeException("Đơn đổi hàng phải ở trạng thái 'Đã đổi' để hoàn tất");
        }

        KhachHang nhanVien = khachHangRepository.findById(idNhanVienXuLy)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + idNhanVienXuLy));

        // Cập nhật trạng thái phiếu đổi → KẾT THÚC
        hoanTra.setTrangThai(HoanTra.TRANG_THAI_KET_THUC);
        hoanTra.setNhanVienXuLy(nhanVien);
        hoanTra.setGhiChuXuLy(ghiChuXuLy);
        hoanTra.setNgayXuLy(LocalDateTime.now());
        hoanTra = hoanTraRepository.save(hoanTra);

        return convertToDTO(hoanTra);
    }

    /**
     * Cấp serial mới cho khách - trừ tồn kho và đánh dấu ĐÃ BÁN
     */
    private void capSerialMoiChoKhach(HoanTraChiTiet chiTiet, String serialMoi, HoanTra hoanTra) {
        SerialSanPham serial = serialSanPhamRepository.findByMaSerial(serialMoi.trim())
                .orElseThrow(() -> new RuntimeException("Serial '" + serialMoi + "' không tồn tại"));

        // Kiểm tra serial phải đang trong kho
        if (serial.getTrangThai() != SerialSanPham.TRANG_THAI_TRONG_KHO) {
            throw new RuntimeException("Serial '" + serialMoi + "' không còn trong kho. Vui lòng chọn serial khác");
        }

        // Kiểm tra serial cùng loại sản phẩm
        if (serial.getSanPhamChiTiet() == null ||
            !serial.getSanPhamChiTiet().getId().equals(chiTiet.getSanPhamChiTiet().getId())) {
            throw new RuntimeException("Serial '" + serialMoi + "' không cùng loại sản phẩm");
        }

        // Trừ tồn kho
        SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
        if (spct.getSoLuongTon() <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng trong kho");
        }
        spct.setSoLuongTon(spct.getSoLuongTon() - 1);
        sanPhamChiTietRepository.save(spct);

        // Cập nhật serial mới → ĐÃ BÁN
        serial.setTrangThai(SerialSanPham.TRANG_THAI_DA_BAN);
        serial.setHoaDonChiTiet(chiTiet.getHoaDonChiTiet());
        serial.setNgayXuatKho(LocalDateTime.now());
        serial.setGhiChu("Serial đổi hàng - " + hoanTra.getMaHoanTra());
        serialSanPhamRepository.save(serial);

        log.info("Đã cấp serial '{}' cho khách trong đơn đổi hàng {}", serialMoi, hoanTra.getMaHoanTra());
    }

    /**
     * Xử lý serial cũ sau khi hoàn tất đổi hàng
     * - luuKho = true: serial cũ → TRONG_KHO (hàng bình thường)
     * - luuKho = false: serial cũ → LỖI (hàng lỗi, không bán nữa)
     */
    private void xuLySerialCuDoiHang(HoanTraChiTiet chiTiet, Boolean luuKho) {
        if (chiTiet.getHoaDonChiTiet() == null) return;

        List<SerialSanPham> serials = serialSanPhamRepository
                .findByHoaDonChiTietIdOrderByIdAsc(chiTiet.getHoaDonChiTiet().getId());

        for (SerialSanPham serial : serials) {
            if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
                if (Boolean.TRUE.equals(luuKho)) {
                    // Serial cũ bình thường → đưa về kho
                    serial.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                    serial.setGhiChu("Serial cũ đổi hàng - Lưu kho");

                    // Tăng tồn kho
                    SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
                    spct.setSoLuongTon(spct.getSoLuongTon() + 1);
                    sanPhamChiTietRepository.save(spct);
                } else {
                    // Serial cũ bị lỗi → đánh dấu LỖI, không bán nữa
                    serial.setTrangThai(SerialSanPham.TRANG_THAI_LOI);
                    serial.setGhiChu("Serial cũ đổi hàng - LỖI");
                }
                serial.setHoaDonChiTiet(null);
                serialSanPhamRepository.save(serial);
            }
        }
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

        // === DOI_HANG workflow actions ===
        if (HoanTra.LOAI_DOI_HANG.equals(loaiHoanTra)) {
            return getBuocXuLyNextDoiHang(hoanTra, result);
        }

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

    /**
     * Lấy các bước xử lý tiếp theo cho DOI_HANG
     * LUỒNG: CHỜ DUYỆT → ĐÃ DUYỆT → ĐÃ NHẬN HÀNG → CHỌN SERIAL MỚI → ĐÃ ĐỔI → KẾT THÚC
     */
    private Map<String, Object> getBuocXuLyNextDoiHang(HoanTra hoanTra, Map<String, Object> result) {
        String trangThaiHienTai = hoanTra.getTrangThai();

        boolean canDuyet = HoanTra.TRANG_THAI_CHO_DUYET_DOI.equals(trangThaiHienTai);
        boolean canNhanHang = HoanTra.TRANG_THAI_DA_DUYET_DOI.equals(trangThaiHienTai);
        boolean canChonSerial = HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI.equals(trangThaiHienTai);
        boolean canHoanTat = HoanTra.TRANG_THAI_CHON_SERIAL_MOI.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_DOI.equals(trangThaiHienTai);
        boolean canTuChoi = HoanTra.TRANG_THAI_CHO_DUYET_DOI.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_DUYET_DOI.equals(trangThaiHienTai);
        boolean isHoanTat = HoanTra.TRANG_THAI_KET_THUC.equals(trangThaiHienTai)
                || HoanTra.TRANG_THAI_DA_DOI.equals(trangThaiHienTai);

        result.put("canDuyet", canDuyet);
        result.put("canNhanHang", canNhanHang);
        result.put("canChonSerial", canChonSerial);
        result.put("canHoanTat", canHoanTat);
        result.put("canTuChoi", canTuChoi);
        result.put("daHoanTat", isHoanTat);

        String trangThaiText = "";
        if (HoanTra.TRANG_THAI_CHO_DUYET_DOI.equals(trangThaiHienTai)) {
            trangThaiText = "Chờ duyệt đổi hàng";
        } else if (HoanTra.TRANG_THAI_DA_DUYET_DOI.equals(trangThaiHienTai)) {
            trangThaiText = "Đã duyệt - Chờ nhận hàng";
        } else if (HoanTra.TRANG_THAI_DA_NHAN_HANG_DOI.equals(trangThaiHienTai)) {
            trangThaiText = "Đã nhận hàng - Sẵn sàng chọn serial mới";
        } else if (HoanTra.TRANG_THAI_CHON_SERIAL_MOI.equals(trangThaiHienTai)) {
            trangThaiText = "Chọn serial mới để đổi";
        } else if (HoanTra.TRANG_THAI_DA_DOI.equals(trangThaiHienTai)) {
            trangThaiText = "Đã đổi - Chờ kết thúc";
        } else if (HoanTra.TRANG_THAI_KET_THUC.equals(trangThaiHienTai)) {
            trangThaiText = "Hoàn tất đổi hàng";
        } else if (HoanTra.TRANG_THAI_TU_CHOI.equals(trangThaiHienTai)) {
            trangThaiText = "Từ chối đổi hàng";
        }
        result.put("trangThaiText", trangThaiText);

        return result;
    }
}
