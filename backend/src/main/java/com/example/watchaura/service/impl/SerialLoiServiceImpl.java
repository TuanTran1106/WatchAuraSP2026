package com.example.watchaura.service.impl;

import com.example.watchaura.dto.SerialLoiDTO;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.SerialLoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SerialLoiServiceImpl implements SerialLoiService {

    private final SerialLoiRepository serialLoiRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoanTraRepository hoanTraRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    @Override
    public SerialLoiDTO createSerialLoi(SerialLoiDTO dto) {
        if (serialLoiRepository.existsByMaSerial(dto.getMaSerial())) {
            throw new RuntimeException("Serial " + dto.getMaSerial() + " đã tồn tại trong danh sách lỗi");
        }

        SerialLoi entity = new SerialLoi();
        entity.setMaSerial(dto.getMaSerial());
        entity.setSanPhamTen(dto.getSanPhamTen());
        entity.setLyDo(dto.getLyDo());
        entity.setTrangThai(SerialLoi.TRANG_THAI_CHUA_XU_LY);

        if (dto.getHoanTraId() != null) {
            HoanTra hoanTra = hoanTraRepository.findById(dto.getHoanTraId()).orElse(null);
            entity.setHoanTra(hoanTra);
        }

        if (dto.getHoaDonChiTietId() != null) {
            HoaDonChiTiet hoaDonChiTiet = hoaDonChiTietRepository.findById(dto.getHoaDonChiTietId()).orElse(null);
            entity.setHoaDonChiTiet(hoaDonChiTiet);
        }

        if (dto.getNguoiTaoTen() != null) {
            KhachHang nguoiTao = khachHangRepository.findById(Integer.parseInt(dto.getNguoiTaoTen())).orElse(null);
            entity.setNguoiTao(nguoiTao);
        }

        entity = serialLoiRepository.save(entity);
        return convertToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public SerialLoiDTO getSerialLoiById(Integer id) {
        SerialLoi entity = serialLoiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy serial lỗi với ID: " + id));
        return convertToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public SerialLoiDTO getSerialLoiByMaSerial(String maSerial) {
        SerialLoi entity = serialLoiRepository.findByMaSerial(maSerial)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy serial: " + maSerial));
        return convertToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SerialLoiDTO> getAllSerialLoi(Pageable pageable) {
        return serialLoiRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SerialLoiDTO> getSerialLoiByTrangThai(String trangThai, Pageable pageable) {
        return serialLoiRepository.findByTrangThaiOrderByNgayTaoDesc(trangThai, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SerialLoiDTO> searchSerialLoi(String trangThai, String keyword, Pageable pageable) {
        // Handle null/empty values
        String trangThaiParam = (trangThai != null && !trangThai.isEmpty()) ? trangThai : null;
        String keywordParam = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;

        Page<SerialLoi> result;
        if (trangThaiParam != null && keywordParam != null) {
            // Both filters
            result = serialLoiRepository.searchSerialLoi(trangThaiParam, keywordParam, pageable);
        } else if (trangThaiParam != null) {
            // Only status filter
            result = serialLoiRepository.findByTrangThaiOrderByNgayTaoDesc(trangThaiParam, pageable);
        } else if (keywordParam != null) {
            // Only keyword filter
            result = serialLoiRepository.searchSerialLoi(null, keywordParam, pageable);
        } else {
            // No filters - get all
            result = serialLoiRepository.findAll(pageable);
        }
        return result.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SerialLoiDTO> getSerialLoiByHoanTraId(Integer hoanTraId) {
        return serialLoiRepository.findByHoanTraId(hoanTraId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SerialLoiDTO xuLySerialLoi(Integer id, Integer nhanVienId) {
        SerialLoi entity = serialLoiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy serial lỗi với ID: " + id));

        entity.setTrangThai(SerialLoi.TRANG_THAI_DA_XU_LY);
        entity.setNgayXuLy(LocalDateTime.now());

        KhachHang nguoiXuLy = khachHangRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + nhanVienId));
        entity.setNguoiXuLy(nguoiXuLy);

        entity = serialLoiRepository.save(entity);
        return convertToDTO(entity);
    }

    @Override
    public SerialLoiDTO huySerialLoi(Integer id, Integer nhanVienId) {
        SerialLoi entity = serialLoiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy serial lỗi với ID: " + id));

        entity.setTrangThai(SerialLoi.TRANG_THAI_HUY);
        entity.setNgayXuLy(LocalDateTime.now());

        KhachHang nguoiXuLy = khachHangRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + nhanVienId));
        entity.setNguoiXuLy(nguoiXuLy);

        entity = serialLoiRepository.save(entity);
        return convertToDTO(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByTrangThai(String trangThai) {
        return serialLoiRepository.countByTrangThai(trangThai);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getThongKe() {
        Map<String, Long> thongKe = new HashMap<>();
        thongKe.put("chuaXuLy", serialLoiRepository.countByTrangThai(SerialLoi.TRANG_THAI_CHUA_XU_LY));
        thongKe.put("daXuLy", serialLoiRepository.countByTrangThai(SerialLoi.TRANG_THAI_DA_XU_LY));
        thongKe.put("huy", serialLoiRepository.countByTrangThai(SerialLoi.TRANG_THAI_HUY));
        thongKe.put("tong", serialLoiRepository.count());
        return thongKe;
    }

    private SerialLoiDTO convertToDTO(SerialLoi entity) {
        SerialLoiDTO dto = SerialLoiDTO.builder()
                .id(entity.getId())
                .maSerial(entity.getMaSerial())
                .sanPhamTen(entity.getSanPhamTen())
                .lyDo(entity.getLyDo())
                .trangThai(entity.getTrangThai())
                .trangThaiText(getTrangThaiText(entity.getTrangThai()))
                .ngayTao(entity.getNgayTao())
                .ngayXuLy(entity.getNgayXuLy())
                .build();

        if (entity.getHoanTra() != null) {
            dto.setHoanTraId(entity.getHoanTra().getId());
            dto.setMaHoanTra(entity.getHoanTra().getMaHoanTra());
        }

        if (entity.getHoaDonChiTiet() != null) {
            dto.setHoaDonChiTietId(entity.getHoaDonChiTiet().getId());
        }

        if (entity.getNguoiTao() != null) {
            dto.setNguoiTaoTen(entity.getNguoiTao().getTenNguoiDung());
        }

        if (entity.getNguoiXuLy() != null) {
            dto.setNguoiXuLyTen(entity.getNguoiXuLy().getTenNguoiDung());
        }

        return dto;
    }

    private String getTrangThaiText(String trangThai) {
        return switch (trangThai) {
            case SerialLoi.TRANG_THAI_CHUA_XU_LY -> "Chưa xử lý";
            case SerialLoi.TRANG_THAI_DA_XU_LY -> "Đã xử lý";
            case SerialLoi.TRANG_THAI_HUY -> "Đã hủy";
            default -> trangThai;
        };
    }
}
