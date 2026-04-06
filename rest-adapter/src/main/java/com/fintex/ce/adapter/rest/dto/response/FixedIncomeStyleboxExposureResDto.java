package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.sm.model.domain.enumeration.FixedIncomeStyleBoxType;
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
public class FixedIncomeStyleboxExposureResDto extends WarningDTO {

  private Map<FixedIncomeStyleBoxType, BigDecimal> fixedIncomeStyleboxExposure;

  public FixedIncomeStyleboxExposureResDto(Map<FixedIncomeStyleBoxType, BigDecimal> fixedIncomeStyleboxExposure,
      List<Warning> warnings) {
    super(warnings);
    this.fixedIncomeStyleboxExposure = fixedIncomeStyleboxExposure;
  }
}
