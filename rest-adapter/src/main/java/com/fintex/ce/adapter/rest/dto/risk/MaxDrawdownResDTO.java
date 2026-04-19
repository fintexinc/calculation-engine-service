package com.fintex.ce.adapter.rest.dto.risk;

import com.fintex.ce.adapter.rest.dto.MaxDrawdownDTO;
import com.fintex.ce.adapter.rest.dto.PeriodResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for max-drawdown metric. Contains maximum peak-to-trough drawdown per time interval period.")
public class MaxDrawdownResDTO extends PeriodResDTO {

  @Schema(description = "Maximum drawdown entries per time interval period")
  private List<MaxDrawdownDTO> maxDrawdown;

}
