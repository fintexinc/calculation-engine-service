package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DataProviderCommand {
  private List<DataProvider> dataProviders;
}
