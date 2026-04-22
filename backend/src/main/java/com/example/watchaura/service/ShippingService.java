package com.example.watchaura.service;

import com.example.watchaura.dto.ShippingFeeRequest;
import com.example.watchaura.dto.ShippingFeeResponse;
import com.example.watchaura.dto.ShippingLocationOption;

import java.util.List;

public interface ShippingService {
    ShippingFeeResponse calculateFee(ShippingFeeRequest request, Integer khachHangId);

    List<ShippingLocationOption> getProvinces();

    List<ShippingLocationOption> getDistricts(Integer provinceId);

    List<ShippingLocationOption> getWards(Integer districtId);
}
