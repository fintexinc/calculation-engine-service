package com.fintex.ce.adapter.rest.dto.response;

import com.fintex.ce.adapter.rest.dto.response.core.WarningDTO;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
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
public class EquitySectorResDTO extends WarningDTO {

  private Map<EquitySectorAllocationType, BigDecimal> equitySector;

  public EquitySectorResDTO() {

  }

  public EquitySectorResDTO(Map<EquitySectorAllocationType, BigDecimal> equitySector, List<Warning> warnings) {
    super(warnings);
    this.equitySector = equitySector;
  }
}
