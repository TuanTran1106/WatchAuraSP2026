package com.example.watchaura.dto.ghn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GhnApiResponse<T> {
    private Integer code;
    private String message;
    private T data;
}

