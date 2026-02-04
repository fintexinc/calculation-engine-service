package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.enumeration.calculation.FixedIncomeCreditQuality;
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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
public class CreditQualityResDTO extends WarningDTO {

  private Map<FixedIncomeCreditQuality, BigDecimal> creditQuality;

  public CreditQualityResDTO(final Map<FixedIncomeCreditQuality, BigDecimal> creditQuality,
      final List<Warning> warnings) {
    super(warnings);
    this.creditQuality = creditQuality;
  }
}
