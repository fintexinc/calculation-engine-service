package com.fintex.ce.adapter.rest.dto.request.core;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import lombok.Data;

import java.util.List;

@Data
public class DataProviderReqDTO {

  private List<DataProvider> dataProviders;

}
