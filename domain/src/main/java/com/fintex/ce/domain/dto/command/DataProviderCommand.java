package com.fintex.ce.domain.dto.command;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DataProviderCommand implements CalculationCommand {
  private List<DataProvider> dataProviders;
}
