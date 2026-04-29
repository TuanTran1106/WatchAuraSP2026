package com.example.watchaura.service;

import com.example.watchaura.dto.ImportSerialResponse;

import java.util.List;

public interface SerialService {

    ImportSerialResponse validateSerials(List<String> serials);

    ImportSerialResponse importSerialsToVariant(Integer idSanPhamChiTiet, List<String> serials);
}
