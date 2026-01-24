package com.example.watchaura.service.impl;

import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.DiaChiRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.DiaChiService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class DiaChiServiceImpl implements DiaChiService {
    @Autowired
    private DiaChiRepository diaChiRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public List<DiaChi> getByKhachHang(Integer khachHangId) {
        return diaChiRepository.findByKhachHangId(khachHangId);
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
                    .findByKhachHangId(khachHangId);

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
        dc.setMacDinh(diaChi.getMacDinh());

        return diaChiRepository.save(dc);
    }

    @Override
    public void delete(Integer id) {
        getById(id);
        diaChiRepository.deleteById(id);
    }
}
