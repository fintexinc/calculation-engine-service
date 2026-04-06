package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.FixedIncomeSectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.sm.model.domain.enumeration.FixedIncomeSecuritiesAllocationType;
import com.fintex.sm.model.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.enumeration.FixedIncomeSectorAllocationType;
import com.fintex.sm.model.domain.value.FixedIncomeSectorAllocationTypeNameValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class FixedIncomeBondSecuritiesFetcherTest
    extends AbstractSecurityMasterFetcherTest<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/fixed-income-sector";

  @Mock
  private FixedIncomeSectorAllocationMapper mapper;

  private FixedIncomeBondSecuritiesFetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new FixedIncomeBondSecuritiesFetcher(client, mapper, ENDPOINT_PATH);
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
    var governmentEntry = new FixedIncomeSectorAllocationTypeNameValue();
    governmentEntry.setType(FixedIncomeSectorAllocationType.GOVERNMENT);
    governmentEntry.setValue(BigDecimal.valueOf(45.2));

    var corporateEntry = new FixedIncomeSectorAllocationTypeNameValue();
    corporateEntry.setType(FixedIncomeSectorAllocationType.CORPORATE);
    corporateEntry.setValue(BigDecimal.valueOf(35.5));

    var smsAllocation = new FixedIncomeSectorAllocation();
    smsAllocation.setAllocation(List.of(governmentEntry, corporateEntry));
    return smsAllocation;
  }

  @Override
  protected FixedIncomeBondSecurities createExpectedDomainModel(String holdingId) {
    return new FixedIncomeBondSecurities()
        .setFixedIncomeBondSectors(Map.of(
            FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(45.2),
            FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(35.5)))
        .setHoldingType(FinancialInstrumentType.ETF_CANADA)
        .setHoldingId(holdingId);
  }

  @Override
  protected SecurityMasterResponseMapper<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> mapper() {
    return mapper;
  }
}