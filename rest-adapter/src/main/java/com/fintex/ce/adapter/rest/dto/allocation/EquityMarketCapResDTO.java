package com.fintex.ce.adapter.rest.dto.allocation;

import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.allocation.EquityMarketCapitalizationType;

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
@Schema(description = "Response for equity-market-capitalization metric. Contains equity market capitalization breakdown.")
public class EquityMarketCapResDTO extends WarningDTO {

  @Schema(description = "Equity allocation percentages by market capitalization")
  private Map<EquityMarketCapitalizationType, BigDecimal> equityMarketCapitalization;

  public EquityMarketCapResDTO() {

  }

  public EquityMarketCapResDTO(Map<EquityMarketCapitalizationType, BigDecimal> equityMarketCapitalization,
      List<Warning> warnings) {
    super(warnings);
    this.equityMarketCapitalization = equityMarketCapitalization;
  }
}
