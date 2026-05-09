package com.example.watchaura.service;

import com.example.watchaura.dto.HoanTraDTO;
import com.example.watchaura.dto.HoanTraExcelRow;
import com.example.watchaura.dto.HoanTraRequest;
import com.example.watchaura.dto.HoanTraUocTinhDTO;
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

    Map<String, Object> getHoanTraPaged(int page, int size, String trangThai, String keyword, String loaiHoanTra, String tuNgay, String denNgay);

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

    
    HoanTraDTO doiTrangThai(Integer id, String trangThaiMoi, Integer idNhanVienXuLy, Boolean themVaoKho, String ghiChuXuLy);

    /**
     * Tính số tiền hoàn ước tính dựa trên danh sách sản phẩm hoàn
     * Áp dụng logic: tổng tiền hàng hoàn - tỷ lệ voucher - phí vận chuyển
     */
    HoanTraUocTinhDTO tinhSoTienHoanUocTinh(Integer hoaDonId, Integer khachHangId, List<Map<String, Object>> chiTietList);
}
