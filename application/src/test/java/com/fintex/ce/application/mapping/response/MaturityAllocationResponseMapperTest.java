package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocation;
import com.fintex.ce.model.domain.calculation.allocation.MaturityAllocationType;
import com.fintex.ce.model.domain.result.allocation.MaturityAllocationResult;
import com.fintex.ce.model.error.Warning;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaturityAllocationResponseMapperTest {

  private final MaturityAllocationResponseMapper mapper = new MaturityAllocationResponseMapper();

  @Test
  void shouldReturnDefaultMap_whenDomainIsNull() {
    MaturityAllocationResult result = mapper.toResponse(null);

    assertEquals(MaturityAllocationType.values().length, result.getMaturityAllocation().size());
    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getMaturityAllocation().get(MaturityAllocationType.UNDER_ONE_YEAR));
  }

  @Test
  void shouldMapKnownKeysAndIgnoreUnknownKeys_whenMappingFromDomain() {
    MaturityAllocation domain = new MaturityAllocation();
    domain.setMaturityDurationValues(Map.of(
        "UNDER_ONE_YEAR", new BigDecimal("0.12345678901"),
        "ONE_TO_THREE_YEARS", new BigDecimal("0.2"),
        "UNKNOWN_KEY", new BigDecimal("0.9")));

    MaturityAllocationResult result = mapper.toResponse(domain);

    assertEquals(2, result.getMaturityAllocation().size());
    assertEquals(0, result.getMaturityAllocation().get(MaturityAllocationType.UNDER_ONE_YEAR).compareTo(new BigDecimal(
        "0.1234567890")));
    assertEquals(0, result.getMaturityAllocation().get(MaturityAllocationType.ONE_TO_THREE_YEARS).compareTo(
        new BigDecimal("0.2000000000")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenNetProductsAreEmpty() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));

    MaturityAllocationResult result = mapper.fromNetProducts(Map.of(), warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(MaturityAllocationType.values().length, result.getMaturityAllocation().size());
    assertNull(result.getMaturityAllocation().get(MaturityAllocationType.MORE_THAN_TWENTY_YEARS));
  }

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));
    Map<MaturityAllocationType, BigDecimal> netProducts = Map.of(
        MaturityAllocationType.UNDER_ONE_YEAR, new BigDecimal("0.1"),
        MaturityAllocationType.ONE_TO_THREE_YEARS, new BigDecimal("0.2"));

    MaturityAllocationResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getMaturityAllocation().get(MaturityAllocationType.UNDER_ONE_YEAR).compareTo(new BigDecimal(
        "0.1000000000")));
    assertEquals(0, result.getMaturityAllocation().get(MaturityAllocationType.ONE_TO_THREE_YEARS).compareTo(
        new BigDecimal("0.2000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}
