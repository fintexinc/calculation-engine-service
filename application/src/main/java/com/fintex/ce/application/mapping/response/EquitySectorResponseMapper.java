package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class EquitySectorResponseMapper
    extends
      AbstractSectorResponseMapper<EquitySectorAllocationType, EquitySectorResult> {

  public EquitySectorResponseMapper() {
    super(EquitySectorAllocationType.class);
  }

  @Override
  protected EquitySectorResult buildResult(Map<EquitySectorAllocationType, BigDecimal> sectors,
      List<Notification> warnings) {
    return EquitySectorResult.builder()
        .equitySector(sectors)
        .warnings(warnings)
        .build();
  }
}
