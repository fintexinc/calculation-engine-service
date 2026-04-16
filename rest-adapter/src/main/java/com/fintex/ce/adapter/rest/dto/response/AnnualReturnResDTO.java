package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.DatesResDTO;
import com.fintex.ce.adapter.rest.dto.response.core.KeyValueDTO;
import com.fintex.ce.model.error.Warning;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response for annual-return metric. Contains calendar-year annual returns.")
public class AnnualReturnResDTO<T> extends DatesResDTO {

  @Schema(description = "Annual returns by calendar year")
  private List<KeyValueDTO<T>> annualReturns;
  @Schema(description = "Calculation warnings")
  private List<Warning> warnings;
}
