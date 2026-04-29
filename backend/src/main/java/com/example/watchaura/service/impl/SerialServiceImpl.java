package com.example.watchaura.service.impl;

import com.example.watchaura.dto.ImportSerialResponse;
import com.example.watchaura.entity.SerialSanPham;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.SerialSanPhamRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.SerialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SerialServiceImpl implements SerialService {

    private final SerialSanPhamRepository serialSanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    @Override
    public ImportSerialResponse validateSerials(List<String> serials) {
        List<String> errorSerials = new ArrayList<>();
        List<String> previewSerials = new ArrayList<>();
        
        if (serials == null || serials.isEmpty()) {
            return ImportSerialResponse.builder()
                    .success(false)
                    .message("Danh sách serial trống")
                    .totalSerials(0)
                    .successCount(0)
                    .errorCount(0)
                    .errorSerials(errorSerials)
                    .previewSerials(previewSerials)
                    .build();
        }

        Set<String> seenSerials = new HashSet<>();
        int successCount = 0;
        
        for (int i = 0; i < serials.size(); i++) {
            String serial = serials.get(i);
            
            if (serial == null || serial.trim().isEmpty()) {
                errorSerials.add("Dòng " + (i + 1) + ": serial trống");
                continue;
            }
            
            serial = serial.trim();
            
            if (seenSerials.contains(serial)) {
                errorSerials.add("Dòng " + (i + 1) + ": serial '" + serial + "' bị trùng trong danh sách");
                continue;
            }
            seenSerials.add(serial);
            
            if (serialSanPhamRepository.findByMaSerial(serial).isPresent()) {
                errorSerials.add("Dòng " + (i + 1) + ": serial '" + serial + "' đã tồn tại trong hệ thống");
                continue;
            }
            
            successCount++;
            if (previewSerials.size() < 10) {
                previewSerials.add(serial);
            }
        }

        return ImportSerialResponse.builder()
                .success(errorSerials.isEmpty())
                .message(errorSerials.isEmpty() ? "Tất cả serial hợp lệ" : "Có " + errorSerials.size() + " serial không hợp lệ")
                .totalSerials(serials.size())
                .successCount(successCount)
                .errorCount(errorSerials.size())
                .errorSerials(errorSerials)
                .previewSerials(previewSerials)
                .build();
    }

    @Override
    @Transactional
    public ImportSerialResponse importSerialsToVariant(Integer idSanPhamChiTiet, List<String> serials) {
        List<String> errorSerials = new ArrayList<>();
        List<String> importedSerials = new ArrayList<>();
        
        if (idSanPhamChiTiet == null) {
            return ImportSerialResponse.builder()
                    .success(false)
                    .message("ID sản phẩm chi tiết không được để trống")
                    .totalSerials(0)
                    .successCount(0)
                    .errorCount(0)
                    .errorSerials(errorSerials)
                    .previewSerials(importedSerials)
                    .build();
        }
        
        if (serials == null || serials.isEmpty()) {
            return ImportSerialResponse.builder()
                    .success(false)
                    .message("Danh sách serial trống")
                    .totalSerials(0)
                    .successCount(0)
                    .errorCount(0)
                    .errorSerials(errorSerials)
                    .previewSerials(importedSerials)
                    .build();
        }

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(idSanPhamChiTiet)
                .orElse(null);
        
        if (sanPhamChiTiet == null) {
            return ImportSerialResponse.builder()
                    .success(false)
                    .message("Không tìm thấy sản phẩm chi tiết với ID: " + idSanPhamChiTiet)
                    .totalSerials(serials.size())
                    .successCount(0)
                    .errorCount(serials.size())
                    .errorSerials(List.of("Sản phẩm chi tiết không tồn tại"))
                    .previewSerials(importedSerials)
                    .build();
        }

        Set<String> seenSerials = new HashSet<>();
        LocalDateTime now = LocalDateTime.now();
        int currentSerialCount = sanPhamChiTiet.getSerialSanPhams() != null 
                ? sanPhamChiTiet.getSerialSanPhams().size() 
                : 0;
        
        for (int i = 0; i < serials.size(); i++) {
            String serial = serials.get(i);
            
            if (serial == null || serial.trim().isEmpty()) {
                errorSerials.add("Dòng " + (i + 1) + ": serial trống");
                continue;
            }
            
            serial = serial.trim();
            
            if (seenSerials.contains(serial)) {
                errorSerials.add("Dòng " + (i + 1) + ": serial '" + serial + "' bị trùng trong danh sách");
                continue;
            }
            seenSerials.add(serial);
            
            // Kiểm tra serial đã tồn tại trong biến thể hiện tại chưa
            boolean existsInCurrentVariant = serialSanPhamRepository
                    .findByMaSerial(serial)
                    .map(s -> s.getSanPhamChiTiet() != null 
                              && s.getSanPhamChiTiet().getId().equals(idSanPhamChiTiet))
                    .orElse(false);
            
            if (existsInCurrentVariant) {
                // Serial đã tồn tại trong biến thể hiện tại -> bỏ qua (không lỗi, không thêm)
                continue;
            }
            
            // Kiểm tra serial đã tồn tại trong biến thể khác chưa
            if (serialSanPhamRepository.findByMaSerial(serial).isPresent()) {
                errorSerials.add("Dòng " + (i + 1) + ": serial '" + serial + "' đã tồn tại trong biến thể khác");
                continue;
            }
            
            SerialSanPham serialSanPham = new SerialSanPham();
            serialSanPham.setMaSerial(serial);
            serialSanPham.setSanPhamChiTiet(sanPhamChiTiet);
            serialSanPham.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
            serialSanPham.setNgayTao(now);
            
            serialSanPhamRepository.save(serialSanPham);
            importedSerials.add(serial);
            currentSerialCount++;
        }

        // Luôn cập nhật soLuongTon = tổng số serial trong kho (cũ + mới)
        int totalSerialsInStock = (int) serialSanPhamRepository
                .countBySanPhamChiTietIdAndTrangThai(idSanPhamChiTiet, SerialSanPham.TRANG_THAI_TRONG_KHO);
        sanPhamChiTiet.setSoLuongTon(totalSerialsInStock);
        sanPhamChiTietRepository.save(sanPhamChiTiet);

        String message;
        if (importedSerials.isEmpty() && errorSerials.isEmpty()) {
            message = "Tất cả serial đã tồn tại trong biến thể này, không có serial mới để thêm. Tổng: " + totalSerialsInStock + " serial";
        } else if (errorSerials.isEmpty()) {
            message = "Import thành công " + importedSerials.size() + " serial. Tổng: " + totalSerialsInStock + " serial";
        } else if (importedSerials.isEmpty()) {
            message = "Import thất bại: tất cả serial đều có lỗi";
        } else {
            message = "Import " + importedSerials.size() + " serial thành công, " + errorSerials.size() + " serial có lỗi. Tổng: " + totalSerialsInStock + " serial";
        }

        return ImportSerialResponse.builder()
                .success(errorSerials.isEmpty())
                .message(message)
                .totalSerials(serials.size())
                .successCount(importedSerials.size())
                .errorCount(errorSerials.size())
                .errorSerials(errorSerials)
                .previewSerials(importedSerials.stream().limit(10).toList())
                .build();
    }
}
