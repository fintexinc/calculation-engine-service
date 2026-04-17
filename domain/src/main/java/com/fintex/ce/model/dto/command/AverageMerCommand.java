package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Command for fee ratio calculations. Supports metrics: mer, management-fee")
public class AverageMerCommand extends DataProviderCommand implements HoldingsProvider {
  @Schema(description = "Fee parameter types to calculate", example = "[\"scaled\", \"absolute\"]")
  private List<ParameterType> parameterTypes;
  @Schema(description = "Portfolio holdings")
  private List<PortfolioHolding> holdings;
}
