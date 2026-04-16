package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
import com.fintex.ce.model.error.Warning;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Response for maturity-allocation metric. Contains bond maturity allocation breakdown.")
public class MaturityAllocationResDto extends WarningDTO {

  @Schema(description = "Bond maturity allocation percentages")
  private Map<MaturityAllocationType, BigDecimal> maturityAllocation;

  public MaturityAllocationResDto(Map<MaturityAllocationType, BigDecimal> maturityAllocation, List<Warning> warnings) {
    super(warnings);
    this.maturityAllocation = maturityAllocation;
  }
}
