package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.EquityStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.sm.model.domain.enumeration.StyleBoxType;
import com.fintex.sm.model.domain.rating.StyleBoxes;
import com.fintex.sm.model.domain.value.StyleBoxValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class EquityStyleboxExposureFetcherTest
    extends AbstractSecurityMasterFetcherTest<EquityStyleboxExposure, StyleBoxes> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/equity-stylebox";

  @Mock
  private EquityStyleboxExposureMapper mapper;

  private EquityStyleboxExposureFetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new EquityStyleboxExposureFetcher(client, mapper, ENDPOINT_PATH);
  }

  @Override
  protected AbstractSecurityMasterFetcher<EquityStyleboxExposure, StyleBoxes> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected StyleBoxes createSmsResponse() {
    var styleBoxes = new StyleBoxes();
    styleBoxes.setBoxValues(List.of(
        new StyleBoxValue(StyleBoxType.LARGE_CORE, new BigDecimal("42.3")),
        new StyleBoxValue(StyleBoxType.MID_VALUE, new BigDecimal("5.5"))));
    return styleBoxes;
  }

  @Override
  protected EquityStyleboxExposure createExpectedDomainModel(String holdingId) {
    return new EquityStyleboxExposure()
        .setBoxValues(Map.of(
            StyleBoxType.LARGE_CORE, new BigDecimal("42.3"),
            StyleBoxType.MID_VALUE, new BigDecimal("5.5")))
        .setHoldingId(holdingId);
  }

  @Override
  protected SecurityMasterResponseMapper<EquityStyleboxExposure, StyleBoxes> mapper() {
    return mapper;
  }
}
