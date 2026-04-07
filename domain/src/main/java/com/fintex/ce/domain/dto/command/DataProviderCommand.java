package com.fintex.ce.domain.dto.command;

import com.fintex.sm.model.DataProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(description = "Base command with data provider selection")
public class DataProviderCommand extends CalculationCommand {
  @Schema(description = "Data providers to use for fetching security data", example = "[\"MORNINGSTAR\"]")
  private List<DataProvider> dataProviders;
}
