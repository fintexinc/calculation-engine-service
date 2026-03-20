package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.EquitySectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.sm.model.domain.allocation.EquitySectorAllocation;
import com.fintex.sm.model.domain.enumeration.EquitySectorAllocationType;
import com.fintex.sm.model.domain.value.EquitySectorAllocationTypeNameValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EquitySectorAllocationFetcherTest
    extends AbstractSecurityMasterFetcherTest<EquitySector, EquitySectorAllocation> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/equity-sector";

  @Mock
  private EquitySectorAllocationMapper mapper;

  private EquitySectorAllocationFetcher fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new EquitySectorAllocationFetcher(client, mapper, ENDPOINT_PATH);
  }

  @Override
  protected AbstractSecurityMasterFetcher<EquitySector, EquitySectorAllocation> fetcher() {
    return fetcher;
  }

  @Override
  protected String expectedEndpointPath() {
    return ENDPOINT_PATH;
  }

  @Override
  protected EquitySectorAllocation createSmsResponse() {
    var techEntry = new EquitySectorAllocationTypeNameValue();
    techEntry.setType(EquitySectorAllocationType.TECHNOLOGY);
    techEntry.setValue(BigDecimal.valueOf(28.5));

    var healthEntry = new EquitySectorAllocationTypeNameValue();
    healthEntry.setType(EquitySectorAllocationType.HEALTHCARE);
    healthEntry.setValue(BigDecimal.valueOf(15.3));

    var smsAllocation = new EquitySectorAllocation();
    smsAllocation.setAllocation(List.of(techEntry, healthEntry));
    return smsAllocation;
  }

  @Override
  protected EquitySector createExpectedDomainModel(String holdingId) {
    return new EquitySector()
        .setAllocations(Map.of(
            EquitySectorAllocationType.TECHNOLOGY, BigDecimal.valueOf(28.5),
            EquitySectorAllocationType.HEALTHCARE, BigDecimal.valueOf(15.3)))
        .setHoldingId(holdingId);
  }

  @Override
  protected SecurityMasterResponseMapper<EquitySector, EquitySectorAllocation> mapper() {
    return mapper;
  }
}