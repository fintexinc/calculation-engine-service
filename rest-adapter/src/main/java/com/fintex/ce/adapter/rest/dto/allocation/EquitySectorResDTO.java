package com.fintex.ce.adapter.rest.dto.allocation;

import com.fintex.ce.adapter.rest.dto.WarningDTO;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;

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
@Schema(description = "Response for equity-sector metric. Contains equity sector allocation breakdown.")
public class EquitySectorResDTO extends WarningDTO {

  @Schema(description = "Equity allocation percentages by sector")
  private Map<EquitySectorAllocationType, BigDecimal> equitySector;

  public EquitySectorResDTO() {

  }

  public EquitySectorResDTO(Map<EquitySectorAllocationType, BigDecimal> equitySector, List<Warning> warnings) {
    super(warnings);
    this.equitySector = equitySector;
  }
}
