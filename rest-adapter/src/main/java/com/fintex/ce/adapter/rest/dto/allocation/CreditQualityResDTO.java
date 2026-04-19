package com.fintex.ce.adapter.rest.dto.allocation;

import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeCreditQuality;
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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@Schema(description = "Response for fixed-income-credit-quality metric. Contains fixed income credit quality breakdown.")
public class CreditQualityResDTO extends WarningDTO {

  @Schema(description = "Fixed income credit quality breakdown percentages")
  private Map<FixedIncomeCreditQuality, BigDecimal> creditQuality;

  public CreditQualityResDTO(final Map<FixedIncomeCreditQuality, BigDecimal> creditQuality,
      final List<Warning> warnings) {
    super(warnings);
    this.creditQuality = creditQuality;
  }
}
