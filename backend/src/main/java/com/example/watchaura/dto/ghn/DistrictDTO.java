package com.example.watchaura.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class DistrictDTO {
    @JsonAlias("DistrictID")
    private Integer districtId;

    @JsonAlias("DistrictName")
    private String districtName;
}

