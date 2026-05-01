package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.calculation.exposure.FixedIncomeStyleboxExposure;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxType;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxValue;
import com.fintex.wm.commons.domain.rating.FixedIncomeStyleBoxes;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class FixedIncomeStyleboxExposureFetcherTest
    extends
      AbstractSecurityMasterFetcherTest<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/fixed-income-stylebox";

  @Mock
  private FixedIncomeStyleboxExposureMapper mapper;

  private AbstractSecurityMasterFetcher<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeStyleBoxes>>>() {}) {};
  }

  @Override
  protected AbstractSecurityMasterFetcher<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected FixedIncomeStyleBoxes createSmsResponse() {
    var smsStyleBoxes = new FixedIncomeStyleBoxes();
    smsStyleBoxes.setBoxValues(List.of(
        FixedIncomeStyleBoxValue.builder()
            .styleBoxType(FixedIncomeStyleBoxType.HIGH_LIMITED)
            .value(BigDecimal.valueOf(15.5))
            .build(),
        FixedIncomeStyleBoxValue.builder()
            .styleBoxType(FixedIncomeStyleBoxType.MEDIUM_MODERATE)
            .value(BigDecimal.valueOf(22.3))
            .build()));
    return smsStyleBoxes;
  }

  @Override
  protected FixedIncomeStyleboxExposure createExpectedDomainModel(String holdingId) {
    final FixedIncomeStyleboxExposure tmpFixedIncomeStyleboxExposure = new FixedIncomeStyleboxExposure();
    tmpFixedIncomeStyleboxExposure.setBoxValues(Map.of(
        FixedIncomeStyleBoxType.HIGH_LIMITED, BigDecimal.valueOf(15.5),
        FixedIncomeStyleBoxType.MEDIUM_MODERATE, BigDecimal.valueOf(22.3)));
    tmpFixedIncomeStyleboxExposure.setHoldingType(FinancialInstrumentType.ETF_CANADA);
    tmpFixedIncomeStyleboxExposure.setHoldingId(holdingId);
    return tmpFixedIncomeStyleboxExposure;
  }

  @Override
  protected SecurityMasterResponseMapper<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> mapper() {
    return mapper;
  }
}