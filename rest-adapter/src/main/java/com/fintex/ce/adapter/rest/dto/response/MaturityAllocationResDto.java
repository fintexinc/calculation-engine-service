package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.model.calculation.MaturityAllocationType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class MaturityAllocationResDto extends WarningDTO {

  private Map<MaturityAllocationType, BigDecimal> maturityAllocation;

  public MaturityAllocationResDto(Map<MaturityAllocationType, BigDecimal> maturityAllocation, List<Warning> warnings) {
    super(warnings);
    this.maturityAllocation = maturityAllocation;
  }
}
