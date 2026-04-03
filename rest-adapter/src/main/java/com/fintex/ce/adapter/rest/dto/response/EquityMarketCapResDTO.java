package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.sm.model.domain.enumeration.EquityMarketCapitalizationType;
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
