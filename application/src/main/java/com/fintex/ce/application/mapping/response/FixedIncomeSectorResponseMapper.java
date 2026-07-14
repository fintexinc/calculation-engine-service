package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class FixedIncomeSectorResponseMapper
    extends
      AbstractSectorResponseMapper<FixedIncomeSectorAllocationType, FixedIncomeSectorResult> {

  public FixedIncomeSectorResponseMapper() {
    super(FixedIncomeSectorAllocationType.class);
  }

  @Override
  protected FixedIncomeSectorResult buildResult(Map<FixedIncomeSectorAllocationType, BigDecimal> sectors,
      List<Notification> warnings) {
    return FixedIncomeSectorResult.builder()
        .fixedIncomeSector(sectors)
        .warnings(warnings)
        .build();
  }
}
