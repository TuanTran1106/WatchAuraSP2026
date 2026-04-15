package com.example.watchaura.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProvinceDTO {
    @JsonAlias("ProvinceID")
    private Integer provinceId;

    @JsonAlias("ProvinceName")
    private String provinceName;
}

