package ca.tangerine.pce.model.dto.command;

import ca.tangerine.pce.model.dto.command.contract.CustomPedProvider;
import ca.tangerine.pce.model.dto.command.contract.CustomPsdProvider;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "Command for return-based calculations with custom date range. Supports metrics: annual-return, growth-of-10k")
public class ReturnCommand extends PortfolioBenchmarkCommand
    implements
      CustomPsdProvider,
      CustomPedProvider {
  @Schema(description = "Custom performance start date")
  private LocalDate customPsd;
  @Schema(description = "Custom performance end date")
  private LocalDate customPed;
}
