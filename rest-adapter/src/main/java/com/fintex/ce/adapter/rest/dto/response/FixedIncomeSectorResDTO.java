package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(description = "Response for fixed-income-bond-sector metric. Contains fixed income bond sector allocation breakdown.")
public class FixedIncomeSectorResDTO extends WarningDTO {

  @Schema(description = "Fixed income allocation percentages by bond sector")
  private Map<FixedIncomeSecuritiesAllocationType, BigDecimal> fixedIncomeSector;

  public FixedIncomeSectorResDTO() {

  }

  public FixedIncomeSectorResDTO(final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> fixedIncomeSector,
      final List<Warning> warnings) {
    super(warnings);
    this.fixedIncomeSector = fixedIncomeSector;
  }

}
