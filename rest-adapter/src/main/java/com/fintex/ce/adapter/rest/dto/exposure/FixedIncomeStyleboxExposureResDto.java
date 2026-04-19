package com.fintex.ce.adapter.rest.dto.exposure;

import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;

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
@Schema(description = "Response for fixed-income-stylebox-exposure metric. Contains fixed income style box exposure breakdown.")
public class FixedIncomeStyleboxExposureResDto extends WarningDTO {

  @Schema(description = "Fixed income exposure percentages by style box")
  private Map<FixedIncomeStyleBoxType, BigDecimal> fixedIncomeStyleboxExposure;

  public FixedIncomeStyleboxExposureResDto(Map<FixedIncomeStyleBoxType, BigDecimal> fixedIncomeStyleboxExposure,
      List<Warning> warnings) {
    super(warnings);
    this.fixedIncomeStyleboxExposure = fixedIncomeStyleboxExposure;
  }
}
