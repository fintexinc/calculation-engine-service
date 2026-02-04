package com.fintex.ce.adapter.rest.dto.request.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.domain.enumeration.DataProvider;
import lombok.Data;

import java.util.List;

@Data
public class DataProviderReqDTO {

  @JsonProperty("dataProviders")
  private List<DataProvider> dataProviders;

}
