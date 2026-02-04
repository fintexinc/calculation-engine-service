package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.domain.enumeration.calculation.EquitySectorAllocationType;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EquitySectorResDTO extends WarningDTO {

  private Map<EquitySectorAllocationType, BigDecimal> equitySector;

  public EquitySectorResDTO() {

  }

  public EquitySectorResDTO(Map<EquitySectorAllocationType, BigDecimal> equitySector, List<Warning> warnings) {
    super(warnings);
    this.equitySector = equitySector;
  }
}
