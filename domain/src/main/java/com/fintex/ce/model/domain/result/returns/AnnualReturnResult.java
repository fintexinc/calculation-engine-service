package com.fintex.ce.model.domain.result.returns;

import com.fintex.ce.model.domain.result.DatesResult;
import com.fintex.ce.model.domain.result.KeyValueResult;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@SuperBuilder
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response for annual-return metric. Contains calendar-year annual returns.")
public class AnnualReturnResult<T> extends DatesResult {

  @Schema(description = "Annual returns by calendar year")
  private List<KeyValueResult<T>> annualReturns;
}
