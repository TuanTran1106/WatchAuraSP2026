package com.example.watchaura.dto.ghn;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnServiceDTO {
    @JsonAlias({"service_id", "serviceID", "ServiceID"})
    private Integer serviceId;

    @JsonAlias({"short_name", "ShortName"})
    private String shortName;
}

