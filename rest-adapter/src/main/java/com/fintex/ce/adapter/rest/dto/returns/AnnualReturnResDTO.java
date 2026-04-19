package com.fintex.ce.adapter.rest.dto.returns;

import com.fintex.ce.adapter.rest.dto.DatesResDTO;
import com.fintex.ce.adapter.rest.dto.KeyValueDTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@Schema(description = "Response for annual-return metric. Contains calendar-year annual returns.")
public class AnnualReturnResDTO<T> extends DatesResDTO {

  @Schema(description = "Annual returns by calendar year")
  private List<KeyValueDTO<T>> annualReturns;

  public AnnualReturnResDTO(List<KeyValueDTO<T>> annualReturns) {
    this.annualReturns = annualReturns;
  }
}
