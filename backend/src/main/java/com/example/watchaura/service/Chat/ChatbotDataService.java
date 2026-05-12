package com.example.watchaura.service.Chat;

import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.SanPhamChatBotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ChatbotDataService {

    @Autowired
    private SanPhamChatBotRepository sanPhamChatBotRepository;

    public String buildSystemPrompt() {
        List<SanPhamChiTiet> products = sanPhamChatBotRepository.findAllAvailableForChatbot();

        StringBuilder sb = new StringBuilder();
        sb.append("""
            Bạn là chuyên gia tư vấn đồng hồ cao cấp của cửa hàng WatchAura.
            Nhiệm vụ của bạn là tư vấn sản phẩm dựa trên dữ liệu thực tế bên dưới.
            
            QUY TẮC QUAN TRỌNG:
            - Chỉ tư vấn sản phẩm CÓ TRONG DANH SÁCH bên dưới (còn hàng, đang bán).
            - Trả lời hoàn toàn bằng Tiếng Việt, lịch sự và thân thiện.
            - Khi đề cập giá, luôn định dạng VNĐ có dấu phẩy (VD: 1.500.000 VNĐ).
            - Gợi ý sản phẩm phù hợp ngân sách và nhu cầu khách hàng.
            - Nếu không có sản phẩm phù hợp, thông báo lịch sự và gợi ý thay thế gần nhất.
            - Không bịa đặt thông tin sản phẩm ngoài danh sách..
            - Luôn đề cập tồn kho khi gợi ý để khách biết sản phẩm còn hàng.
            - Khi khách hỏi về bảo quản, hướng dẫn cách sử dụng và vệ sinh đồng hồ.
            - Nếu khách hỏi về chính sách bảo hành, cung cấp thông tin chính xác về bảo hành của cửa hàng.
            - Nếu khách hỏi về thương hiệu, giải thích ngắn gọn về thương hiệu đó (nếu có trong danh sách).
            - Hãy tư vấn thêm về ngoại hình của sản phẩm bằng cách tìm kiếm thông tin trên internet
            
            """);

        if (products.isEmpty()) {
            sb.append("DANH SÁCH SẢN PHẨM: Hiện tại chưa có sản phẩm nào trong kho.\n");
        } else {
            sb.append("=== DANH SÁCH SẢN PHẨM HIỆN CÓ (").append(products.size()).append(" biến thể) ===\n\n");

            for (SanPhamChiTiet p : products) {
                sb.append(buildProductLine(p));
            }
        }

        sb.append("""
            
            === HƯỚNG DẪN TƯ VẤN ===
            - Khách hỏi theo giá: lọc sản phẩm theo giá_ban phù hợp ngân sách.
            - Khách hỏi theo thương hiệu: lọc theo thuong_hieu.
            - Khách hỏi theo loại (nam/nữ/thể thao): lọc theo danh_muc hoặc phong_cach.
            - Khách hỏi theo chất liệu dây: lọc theo chat_lieu_day.
            - Khách hỏi chống nước: đề cập do_chiu_nuoc (đơn vị ATM).
            - Luôn đề cập tồn kho khi gợi ý để khách biết sản phẩm còn hàng.
            """);

        return sb.toString();
    }

    private String buildProductLine(SanPhamChiTiet p) {
        StringBuilder line = new StringBuilder("- ");

        // Tên sản phẩm
        line.append("[").append(p.getSanPham().getMaSanPham()).append("] ");
        line.append(p.getSanPham().getTenSanPham());

        // Thương hiệu
        if (p.getSanPham().getThuongHieu() != null) {
            line.append(" | Thương hiệu: ").append(p.getSanPham().getThuongHieu().getTenThuongHieu());
        }

        // Danh mục
        if (p.getSanPham().getDanhMuc() != null) {
            line.append(" | Danh mục: ").append(p.getSanPham().getDanhMuc().getTenDanhMuc());
        }

        // Loại máy
        if (p.getSanPham().getLoaiMay() != null) {
            line.append(" | Loại máy: ").append(p.getSanPham().getLoaiMay().getTenLoaiMay());
        }

        // Màu sắc
        if (p.getMauSac() != null) {
            line.append(" | Màu: ").append(p.getMauSac().getTenMauSac());
        }

        // Chất liệu dây
        if (p.getChatLieuDay() != null) {
            line.append(" | Dây: ").append(p.getChatLieuDay().getTenChatLieu());
        }

        // Kích thước
        if (p.getKichThuoc() != null) {
            line.append(" | Size: ").append(p.getKichThuoc().getTenKichThuoc());
        }

        // Giá bán
        if (p.getGiaBan() != null) {
            line.append(" | Giá: ").append(formatGia(p.getGiaBan())).append(" VNĐ");
        }

        // Tồn kho
        line.append(" | Tồn kho: ").append(p.getSoLuongKhaDung());

        // Thông số kỹ thuật
        if (p.getDuongKinh() != null) {
            line.append(" | Đường kính: ").append(p.getDuongKinh()).append("mm");
        }
        if (p.getDoChiuNuoc() != null) {
            line.append(" | Chống nước: ").append(p.getDoChiuNuoc()).append("ATM");
        }

        // Phong cách
        if (p.getSanPham().getPhongCach() != null && !p.getSanPham().getPhongCach().isBlank()) {
            line.append(" | Phong cách: ").append(p.getSanPham().getPhongCach());
        }

        // Mô tả ngắn (giới hạn 100 ký tự để không làm prompt quá dài)
        if (p.getSanPham().getMoTa() != null && !p.getSanPham().getMoTa().isBlank()) {
            String moTa = p.getSanPham().getMoTa().trim();
            if (moTa.length() > 100) moTa = moTa.substring(0, 100) + "...";
            line.append(" | Mô tả: ").append(moTa);
        }

        line.append("\n");
        return line.toString();
    }

    private String formatGia(BigDecimal gia) {
        return String.format("%,.0f", gia);
    }
}