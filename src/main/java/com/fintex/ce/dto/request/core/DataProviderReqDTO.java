package com.fintex.ce.dto.request.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.config.enumeration.DataProvider;
import lombok.Data;

import java.util.List;

@Data
public class DataProviderReqDTO {

    @JsonProperty("dataProviders")
    private List<DataProvider> dataProviders;

}
