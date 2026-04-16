package com.example.watchaura.dto.ghn;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GhnAvailableServicesRequest {
    @JsonProperty("shop_id")
    private Integer shopId;

    @JsonProperty("from_district")
    private Integer fromDistrict;

    @JsonProperty("to_district")
    private Integer toDistrict;
}

