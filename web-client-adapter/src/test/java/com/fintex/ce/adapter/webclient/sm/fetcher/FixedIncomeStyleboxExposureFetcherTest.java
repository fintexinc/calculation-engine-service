package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeStyleboxExposureMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.enumeration.FixedIncomeStyleBoxType;
import com.fintex.sm.model.domain.rating.FixedIncomeStyleBoxes;
import com.fintex.sm.model.domain.value.FixedIncomeStyleBoxValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class FixedIncomeStyleboxExposureFetcherTest
    extends AbstractSecurityMasterFetcherTest<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/fixed-income-stylebox";

  @Mock
  private FixedIncomeStyleboxExposureMapper mapper;

  private AbstractSecurityMasterFetcher<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(client, ENDPOINT_PATH, mapper,
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
    return new FixedIncomeStyleboxExposure()
        .setBoxValues(Map.of(
            FixedIncomeStyleBoxType.HIGH_LIMITED, BigDecimal.valueOf(15.5),
            FixedIncomeStyleBoxType.MEDIUM_MODERATE, BigDecimal.valueOf(22.3)))
        .setHoldingType(FinancialInstrumentType.ETF_CANADA)
        .setHoldingId(holdingId);
  }

  @Override
  protected SecurityMasterResponseMapper<FixedIncomeStyleboxExposure, FixedIncomeStyleBoxes> mapper() {
    return mapper;
  }
}