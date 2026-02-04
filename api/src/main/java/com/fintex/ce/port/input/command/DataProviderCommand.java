package com.fintex.ce.port.input.command;

import com.fintex.ce.domain.enumeration.DataProvider;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class DataProviderCommand {
  private List<DataProvider> dataProviders;
}
