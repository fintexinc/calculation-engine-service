package com.fintex.ce.adapter.rest.dto.returns;

import com.fintex.ce.adapter.rest.dto.PeriodResDTO;
import com.fintex.ce.adapter.rest.dto.TimeIntervalResDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@Schema(description = "Response for leading-total-return metric. Contains leading total returns per time interval period.")
public class LeadingTotalReturnsResDTO extends PeriodResDTO {

  @Schema(description = "Leading total returns per time interval period")
  private Set<TimeIntervalResDTO> leadingTotalReturn;

}
