
package com.example.watchaura.service.impl;

import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.DiaChiRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.DiaChiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class DiaChiServiceImpl implements DiaChiService {
    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public List<DiaChi> getByKhachHang(Integer khachHangId) {
        return diaChiRepository.findByKhachHangIdAndDeletedFalse(khachHangId);
    }

    @Override
    public Optional<DiaChi> getDiaChiMacDinhByKhachHang(Integer khachHangId) {
        return diaChiRepository.findFirstByKhachHangIdAndMacDinhTrueAndDeletedFalse(khachHangId);
    }

    @Override
    public void setDiaChiMacDinh(Integer khachHangId, Integer diaChiId) {
        DiaChi dc = getById(diaChiId);
        if (dc.getKhachHang() == null || !dc.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Địa chỉ không thuộc tài khoản của bạn.");
        }
        List<DiaChi> list = diaChiRepository.findByKhachHangIdAndDeletedFalse(khachHangId);
        for (DiaChi other : list) {
            other.setMacDinh(diaChiId.equals(other.getId()));
            diaChiRepository.save(other);
        }
    }

    @Override
    public DiaChi getById(Integer id) {
        return diaChiRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy địa chỉ"));
    }

    @Override
    public DiaChi create(Integer khachHangId, DiaChi diaChi) {

        KhachHang khachHang = khachHangRepository.findById(khachHangId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy khách hàng"));

        diaChi.setKhachHang(khachHang);

        if (Boolean.TRUE.equals(diaChi.getMacDinh())) {
            List<DiaChi> list = diaChiRepository
                    .findByKhachHangIdAndDeletedFalse(khachHangId);

            for (DiaChi dc : list) {
                dc.setMacDinh(false);
            }
        }

        return diaChiRepository.save(diaChi);
    }

    @Override
    public DiaChi update(Integer id, DiaChi diaChi) {
        DiaChi dc = getById(id);

        dc.setDiaChiCuThe(diaChi.getDiaChiCuThe());
        dc.setPhuongXa(diaChi.getPhuongXa());
        dc.setQuanHuyen(diaChi.getQuanHuyen());
        dc.setTinhThanh(diaChi.getTinhThanh());
        dc.setGhnProvinceId(diaChi.getGhnProvinceId());
        dc.setGhnDistrictId(diaChi.getGhnDistrictId());
        dc.setGhnWardCode(diaChi.getGhnWardCode());
        dc.setTenNguoiNhan(diaChi.getTenNguoiNhan());
        dc.setSdtNguoiNhan(diaChi.getSdtNguoiNhan());
        dc.setMacDinh(diaChi.getMacDinh());

        if (Boolean.TRUE.equals(diaChi.getMacDinh())) {
            List<DiaChi> list = diaChiRepository.findByKhachHangIdAndDeletedFalse(dc.getKhachHang().getId());
            for (DiaChi other : list) {
                if (!other.getId().equals(id)) {
                    other.setMacDinh(false);
                    diaChiRepository.save(other);
                }
            }
        }

        return diaChiRepository.save(dc);
    }

    @Override
    public void delete(Integer id) {
        DiaChi dc = getById(id);
        dc.setDeleted(true);
        if (Boolean.TRUE.equals(dc.getMacDinh())) {
            dc.setMacDinh(false);
        }
        diaChiRepository.save(dc);
    }

    @Override
    public void deleteForKhachHang(Integer khachHangId, Integer diaChiId) {
        DiaChi dc = getById(diaChiId);
        if (dc.getKhachHang() == null || !dc.getKhachHang().getId().equals(khachHangId)) {
            throw new RuntimeException("Địa chỉ không thuộc tài khoản của bạn.");
        }
        boolean wasDefault = Boolean.TRUE.equals(dc.getMacDinh());
        dc.setDeleted(true);
        dc.setMacDinh(false);
        diaChiRepository.save(dc);
        if (!wasDefault) {
            return;
        }
        List<DiaChi> remaining = diaChiRepository.findByKhachHangIdAndDeletedFalse(khachHangId);
        if (remaining.isEmpty()) {
            return;
        }
        remaining.sort(Comparator.comparing(DiaChi::getId));
        setDiaChiMacDinh(khachHangId, remaining.get(0).getId());
    }
}

