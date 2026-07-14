package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeSectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeValue;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class FixedIncomeBondSectorFetcherTest
    extends
      AbstractSecurityMasterFetcherTest<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/fixed-income-sector";

  @Mock
  private FixedIncomeSectorAllocationMapper mapper;

  private AbstractSecurityMasterFetcher<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeSectorAllocationWithCurrency>>>() {}) {};
  }

  @Override
  protected AbstractSecurityMasterFetcher<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected FixedIncomeSectorAllocationWithCurrency createSmsResponse() {
    var smsAllocation = new FixedIncomeSectorAllocation();
    smsAllocation.setValue(List.of(
        new FixedIncomeSectorAllocationTypeValue(
            FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(45.2), null, null),
        new FixedIncomeSectorAllocationTypeValue(
            FixedIncomeSectorAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(35.5), null, null)));
    var wrapper = new FixedIncomeSectorAllocationWithCurrency();
    wrapper.setFixedIncomeSectorAllocation(smsAllocation);
    return wrapper;
  }

  @Override
  protected FixedIncomeBondSector createExpectedDomainModel() {
    final FixedIncomeBondSector tmpFixedIncomeBondSector = new FixedIncomeBondSector();
    tmpFixedIncomeBondSector.setFixedIncomeBondSectors(Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(45.2),
        FixedIncomeSectorAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(35.5)));
    tmpFixedIncomeBondSector.setHoldingType(FinancialInstrumentType.ETF_CANADA);
    return tmpFixedIncomeBondSector;
  }

  @Override
  protected SecurityMasterResponseMapper<FixedIncomeBondSector, FixedIncomeSectorAllocationWithCurrency> mapper() {
    return mapper;
  }
}
