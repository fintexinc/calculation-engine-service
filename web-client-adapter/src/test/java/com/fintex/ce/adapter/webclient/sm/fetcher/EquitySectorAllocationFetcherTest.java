package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.mapper.EquitySectorAllocationMapper;
import com.fintex.ce.adapter.webclient.sm.mapper.SecurityMasterResponseMapper;
import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocation;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationTypeNameValue;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;

import org.springframework.core.ParameterizedTypeReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class EquitySectorAllocationFetcherTest
    extends
      AbstractSecurityMasterFetcherTest<EquitySector, EquitySectorAllocation> {

  private static final String ENDPOINT_PATH = "/api/v1/wealth/securities/allocations/equity-sector";

  @Mock
  private EquitySectorAllocationMapper mapper;

  private AbstractSecurityMasterFetcher<EquitySector, EquitySectorAllocation> fetcher;

  @BeforeEach
  void setUp() {
    fetcher = new AbstractSecurityMasterFetcher<>(securityMasterWebClient, ENDPOINT_PATH, mapper,
        new ParameterizedTypeReference<List<SecurityAttributeResult<EquitySectorAllocation>>>() {}) {};
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
    return EquitySector.builder()
        .allocations(Map.of(
            EquitySectorAllocationType.TECHNOLOGY, BigDecimal.valueOf(28.5),
            EquitySectorAllocationType.HEALTHCARE, BigDecimal.valueOf(15.3)))
        .holdingId(holdingId)
        .build();
  }

  @Override
  protected SecurityMasterResponseMapper<EquitySector, EquitySectorAllocation> mapper() {
    return mapper;
  }
}