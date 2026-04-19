package com.fintex.ce.model.dto.command;

import com.fintex.ce.model.domain.enumeration.ParameterType;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;

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
  @NotEmpty(message = ErrorCode.Codes.FIELD_NOT_EMPTY)
  private List<PortfolioHolding> holdings;
}
