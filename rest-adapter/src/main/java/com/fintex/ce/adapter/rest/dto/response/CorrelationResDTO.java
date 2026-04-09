package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.response.correlation.CorrelationPeriodDTO;
import com.fintex.ce.adapter.rest.dto.response.correlation.HoldingsKeyDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Schema(description = "Response for correlation metric. Contains correlation matrix between portfolio holdings and benchmark.")
public class CorrelationResDTO extends PeriodResDTO {

  @Schema(description = "Legend mapping holdings to correlation matrix keys")
  private List<HoldingsKeyDTO> holdingsKey;
  @Schema(description = "Correlation values per time interval period")
  private List<CorrelationPeriodDTO> correlationPeriods;

}
