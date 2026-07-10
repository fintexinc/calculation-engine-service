package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeSectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeValue;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;
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
class FixedIncomeBondSecuritiesFetcherTest
    extends
      AbstractSecurityMasterFetcherTest<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/fixed-income-sector";

  @Mock
  private FixedIncomeSectorAllocationMapper mapper;

  private AbstractSecurityMasterFetcher<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<FixedIncomeSectorAllocation>>>() {}) {};
  }

  @Override
  protected AbstractSecurityMasterFetcher<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected FixedIncomeSectorAllocation createSmsResponse() {
    var governmentEntry = new FixedIncomeSectorAllocationTypeValue();
    governmentEntry.setType(FixedIncomeSectorAllocationType.GOVERNMENT);
    governmentEntry.setValue(BigDecimal.valueOf(45.2));

    var corporateEntry = new FixedIncomeSectorAllocationTypeValue();
    corporateEntry.setType(FixedIncomeSectorAllocationType.CORPORATE);
    corporateEntry.setValue(BigDecimal.valueOf(35.5));

    var smsAllocation = new FixedIncomeSectorAllocation();
    smsAllocation.setAllocations(List.of(governmentEntry, corporateEntry));
    return smsAllocation;
  }

  @Override
  protected FixedIncomeBondSecurities createExpectedDomainModel() {
    final FixedIncomeBondSecurities tmpFixedIncomeBondSecurities = new FixedIncomeBondSecurities();
    tmpFixedIncomeBondSecurities.setFixedIncomeBondSectors(Map.of(
        FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(45.2),
        FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(35.5)));
    tmpFixedIncomeBondSecurities.setHoldingType(FinancialInstrumentType.ETF_CANADA);
    return tmpFixedIncomeBondSecurities;
  }

  @Override
  protected SecurityMasterResponseMapper<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> mapper() {
    return mapper;
  }
}