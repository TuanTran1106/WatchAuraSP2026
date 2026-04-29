package com.example.watchaura.service;

import com.example.watchaura.dto.HoanTraDTO;
import com.example.watchaura.dto.HoanTraExcelRow;
import com.example.watchaura.dto.HoanTraRequest;
import com.example.watchaura.dto.ImportHoanTraResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface HoanTraService {

    List<HoanTraDTO> getAllHoanTra();

    HoanTraDTO getHoanTraById(Integer id);

    HoanTraDTO getHoanTraByMaHoanTra(String maHoanTra);

    List<HoanTraDTO> getHoanTraByKhachHangId(Integer khachHangId);

    List<HoanTraDTO> getHoanTraByTrangThai(String trangThai);

    Map<String, Object> getHoanTraPaged(int page, int size, String trangThai, String keyword);

    HoanTraDTO createHoanTra(HoanTraRequest request);

    HoanTraDTO updateHoanTra(Integer id, HoanTraRequest request);

    HoanTraDTO xuLyHoanTra(Integer id, String ghiChuXuLy, Integer idNhanVienXuLy);

    HoanTraDTO tuChoiHoanTra(Integer id, String ghiChuXuLy, Integer idNhanVienXuLy);

    void deleteHoanTra(Integer id);

    ImportHoanTraResponse previewImportExcel(MultipartFile file);

    ImportHoanTraResponse importFromExcel(MultipartFile file, Integer idNhanVienXuLy);

    List<HoanTraExcelRow> validateExcelData(MultipartFile file);

    byte[] generateHoanTraTemplateExcel();

    Map<String, Object> getHoaDonChoKhachHangTra(Integer hoaDonId, Integer khachHangId);

    Map<String, Object> getSerialCoTheTra(Integer hoaDonId, Integer khachHangId);

    HoanTraDTO createHoanTraKhachHang(HoanTraRequest request, Integer khachHangId);

    boolean existsByHoaDonId(Integer hoaDonId);

    HoanTraDTO getFirstHoanTraByHoaDonId(Integer hoaDonId);

    Map<String, Object> getHoaDonCoTheHoanTraForAdmin();

    Map<String, Object> getHoaDonChiTietForAdmin(Integer hoaDonId);

    HoanTraDTO xuLyHoanTraChiTiet(Integer hoanTraId, Integer hoanTraChiTietId, String ghiChuXuLy, Integer idNhanVienXuLy);

    HoanTraDTO duyetDonTraHang(Integer id, boolean themVaoKho, Integer idNhanVienXuLy, String ghiChuXuLy);

    Map<String, Object> getBuocXuLyNext(Integer hoanTraId);

    // === DOI_HANG specific methods ===
    /**
     * Xử lý CHỌN SERIAL MỚI - Cấp serial mới cho khách
     * - Chọn serial từ danh sách trong kho
     * - Trừ số lượng tồn kho
     * - Đánh dấu serial mới là ĐÃ BÁN
     * Áp dụng cho trạng thái CHỌN SERIAL MỚI → ĐÃ ĐỔI
     *
     * @param hoanTraId ID phiếu đổi hàng
     * @param serialsMoi Map<chiTietId, serialMoi>
     * @param idNhanVienXuLy ID nhân viên xử lý
     * @param ghiChuXuLy Ghi chú xử lý
     */
    HoanTraDTO xuLyDoiHangHoanTat(Integer hoanTraId, Map<Integer, String> serialsMoi,
                                   Integer idNhanVienXuLy, String ghiChuXuLy);

    /**
     * Hoàn tất đổi hàng - Xử lý serial cũ
     * Hỏi admin: Serial cũ có lưu vào kho không hay lỗi?
     * - luuKho = true: serial cũ → TRONG_KHO (hàng bình thường)
     * - luuKho = false: serial cũ → LỖI (hàng lỗi)
     * Áp dụng cho trạng thái ĐÃ ĐỔI → KẾT THÚC
     *
     * @param hoanTraId ID phiếu đổi hàng
     * @param serialCuLoi Map<chiTietId, true (lỗi) / false (lưu kho)>
     * @param idNhanVienXuLy ID nhân viên xử lý
     * @param ghiChuXuLy Ghi chú xử lý
     */
    HoanTraDTO hoanTatDoiHang(Integer hoanTraId, Map<Integer, Boolean> serialCuLoi,
                               Integer idNhanVienXuLy, String ghiChuXuLy);

    /**
     * Lấy serial có thể đổi cho sản phẩm trong đơn hàng
     * Trả về danh sách serial trong kho cùng loại sản phẩm
     */
    Map<String, Object> getSerialCoTheDoi(Integer hoaDonId, Integer sanPhamChiTietId);
}
