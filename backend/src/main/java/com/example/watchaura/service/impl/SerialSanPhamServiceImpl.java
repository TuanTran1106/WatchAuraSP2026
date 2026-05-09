package com.example.watchaura.service.impl;

import com.example.watchaura.dto.HoaDonSerialSelectionDTO;
import com.example.watchaura.dto.SerialSelectionDTO;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.SerialSanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SerialSanPhamServiceImpl implements SerialSanPhamService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SerialSanPhamRepository serialSanPhamRepository;

    @Override
    @Transactional(readOnly = true)
    public HoaDonSerialSelectionDTO getSerialSelectionData(Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        if (chiTiets.isEmpty()) {
            throw new RuntimeException("Hóa đơn không có sản phẩm nào");
        }

        List<HoaDonSerialSelectionDTO.BienTheSerialGroup> groups = new ArrayList<>();

        for (HoaDonChiTiet chiTiet : chiTiets) {
            SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
            String tenSanPham = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm";
            String tenBienThe = buildTenBienThe(spct);
            Integer spctId = spct.getId();
            int soLuongMua = chiTiet.getSoLuong() != null ? chiTiet.getSoLuong() : 0;

            // Lấy serial trong kho (chưa bán)
            List<SerialSanPham> availableSerials = serialSanPhamRepository
                    .findBySanPhamChiTietIdAndTrangThai(spctId, SerialSanPham.TRANG_THAI_TRONG_KHO);

            // Lấy serial đã gán cho hóa đơn chi tiết này
            List<SerialSanPham> assignedSerials = serialSanPhamRepository
                    .findByHoaDonChiTietIdOrderByIdAsc(chiTiet.getId());

            Set<Integer> assignedIds = assignedSerials.stream()
                    .map(SerialSanPham::getId)
                    .collect(Collectors.toSet());

            List<SerialSelectionDTO> serialDTOs = new ArrayList<>();

            // Thêm serial đã gán trước (đánh dấu daChon = true)
            for (SerialSanPham serial : assignedSerials) {
                serialDTOs.add(SerialSelectionDTO.builder()
                        .id(serial.getId())
                        .maSerial(serial.getMaSerial())
                        .sanPhamChiTietId(spctId)
                        .tenSanPham(tenSanPham)
                        .tenBienThe(tenBienThe)
                        .trangThai(serial.getTrangThai())
                        .ngayTao(serial.getNgayTao())
                        .daChon(true)
                        .build());
            }

            // Thêm serial trong kho (đánh dấu daChon = false)
            for (SerialSanPham serial : availableSerials) {
                if (!assignedIds.contains(serial.getId())) {
                    serialDTOs.add(SerialSelectionDTO.builder()
                            .id(serial.getId())
                            .maSerial(serial.getMaSerial())
                            .sanPhamChiTietId(spctId)
                            .tenSanPham(tenSanPham)
                            .tenBienThe(tenBienThe)
                            .trangThai(serial.getTrangThai())
                            .ngayTao(serial.getNgayTao())
                            .daChon(false)
                            .build());
                }
            }

            // Sắp xếp theo mã serial
            serialDTOs.sort(Comparator.comparing(SerialSelectionDTO::getMaSerial));

            HoaDonSerialSelectionDTO.BienTheSerialGroup group = HoaDonSerialSelectionDTO.BienTheSerialGroup.builder()
                    .hoaDonChiTietId(chiTiet.getId())
                    .sanPhamChiTietId(spctId)
                    .tenSanPham(tenSanPham)
                    .tenBienThe(tenBienThe)
                    .soLuongMua(soLuongMua)
                    .soLuongDaChon(assignedSerials.size())
                    .serials(serialDTOs)
                    .build();

            groups.add(group);
        }

        return HoaDonSerialSelectionDTO.builder()
                .hoaDonId(hoaDonId)
                .maDonHang(hoaDon.getMaDonHang())
                .trangThaiDonHang(getTrangThaiHienThi(hoaDon.getTrangThaiDonHang()))
                .bienTheGroups(groups)
                .build();
    }

    private String getTrangThaiHienThi(String trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "CHO_THANH_TOAN" -> "Chờ thanh toán";
            case "DA_XAC_NHAN" -> "Đã xác nhận";
            case "DANG_GIAO" -> "Đang giao";
            case "DA_GIAO" -> "Đã giao";
            case "DA_HUY" -> "Đã hủy";
            case "HOAN_THANH" -> "Hoàn thành";
            case "DA THANH TOAN", "DA_THANH_TOAN", "DA_THANH_TOAN_ONL" -> "Đã thanh toán";
            default -> trangThai;
        };
    }

    @Override
    @Transactional
    public void assignSerialsToOrder(Integer hoaDonId, Map<Integer, List<Integer>> serialsByBienThe) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        for (HoaDonChiTiet chiTiet : chiTiets) {
            Integer hoaDonChiTietId = chiTiet.getId();
            int soLuongMua = chiTiet.getSoLuong() != null ? chiTiet.getSoLuong() : 0;

            // Giải phóng serial cũ (nếu có)
            List<SerialSanPham> currentAssigned = serialSanPhamRepository
                    .findByHoaDonChiTietIdOrderByIdAsc(hoaDonChiTietId);

            for (SerialSanPham serial : currentAssigned) {
                serial.setHoaDonChiTiet(null);
                serial.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                serial.setNgayXuatKho(null);
                serial.setNgayHetBaoHanh(null);
            }
            if (!currentAssigned.isEmpty()) {
                serialSanPhamRepository.saveAll(currentAssigned);
            }

            // Lấy danh sách serial mới được chọn
            List<Integer> selectedSerialIds = serialsByBienThe.get(hoaDonChiTietId);
            if (selectedSerialIds == null || selectedSerialIds.isEmpty()) {
                continue;
            }

            // Kiểm tra số lượng
            if (selectedSerialIds.size() != soLuongMua) {
                throw new RuntimeException("Số lượng serial được chọn (" + selectedSerialIds.size()
                        + ") không khớp với số lượng mua (" + soLuongMua + ")");
            }

            LocalDateTime now = LocalDateTime.now();
            for (Integer serialId : selectedSerialIds) {
                SerialSanPham serial = serialSanPhamRepository.findById(serialId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy serial với ID: " + serialId));

                // Kiểm tra serial có trong kho không
                if (serial.getTrangThai() != SerialSanPham.TRANG_THAI_TRONG_KHO) {
                    throw new RuntimeException("Serial " + serial.getMaSerial() + " không còn trong kho");
                }

                // Kiểm tra serial có thuộc đúng biến thể không
                if (serial.getSanPhamChiTiet().getId() != chiTiet.getSanPhamChiTiet().getId()) {
                    throw new RuntimeException("Serial " + serial.getMaSerial() + " không thuộc biến thể sản phẩm này");
                }

                // Gán serial cho hóa đơn chi tiết
                serial.setHoaDonChiTiet(chiTiet);
                serial.setTrangThai(SerialSanPham.TRANG_THAI_DA_BAN);
                serial.setNgayXuatKho(now);
                serial.setNgayHetBaoHanh(now.plusMonths(12));
                serialSanPhamRepository.save(serial);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<SerialSelectionDTO> getAvailableSerialsBySanPhamChiTietId(Integer sanPhamChiTietId) {
        List<SerialSanPham> serials = serialSanPhamRepository
                .findBySanPhamChiTietIdAndTrangThai(sanPhamChiTietId, SerialSanPham.TRANG_THAI_TRONG_KHO);

        return serials.stream()
                .map(serial -> {
                    SanPhamChiTiet spct = serial.getSanPhamChiTiet();
                    return SerialSelectionDTO.builder()
                            .id(serial.getId())
                            .maSerial(serial.getMaSerial())
                            .sanPhamChiTietId(sanPhamChiTietId)
                            .tenSanPham(spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm")
                            .tenBienThe(buildTenBienThe(spct))
                            .trangThai(serial.getTrangThai())
                            .ngayTao(serial.getNgayTao())
                            .daChon(false)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private String buildTenBienThe(SanPhamChiTiet spct) {
        if (spct == null) return null;
        List<String> parts = new ArrayList<>();
        if (spct.getMauSac() != null && !spct.getMauSac().getTenMauSac().isBlank()) {
            parts.add("Màu: " + spct.getMauSac().getTenMauSac().trim());
        }
        if (spct.getKichThuoc() != null && !spct.getKichThuoc().getTenKichThuoc().isBlank()) {
            parts.add("Kích thước: " + spct.getKichThuoc().getTenKichThuoc().trim());
        }
        if (spct.getChatLieuDay() != null && !spct.getChatLieuDay().getTenChatLieu().isBlank()) {
            parts.add("Dây: " + spct.getChatLieuDay().getTenChatLieu().trim());
        }
        var lm = spct.getSanPham() != null ? spct.getSanPham().getLoaiMay() : null;
        if (lm != null && !lm.getTenLoaiMay().isBlank()) {
            parts.add("Loại máy: " + lm.getTenLoaiMay().trim());
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }
}
