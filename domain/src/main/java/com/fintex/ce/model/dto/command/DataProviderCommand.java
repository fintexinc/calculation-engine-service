package com.fintex.ce.model.dto.command;

import com.fintex.wm.commons.domain.DataProvider;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base command with data provider selection")
public class DataProviderCommand extends CalculationCommand {
  @Schema(description = "Data providers to use for fetching security data", example = "[\"MORNINGSTAR\"]")
  private List<DataProvider> dataProviders;
}
