package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.ce.model.error.Warning;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquitySectorResponseMapperTest {

  private final EquitySectorResponseMapper mapper = new EquitySectorResponseMapper();

  @Test
  void shouldReturnDefaultMapWithZeros_whenDomainIsNull() {
    EquitySectorResult result = mapper.toResponse(null);

    assertEquals(EquitySectorAllocationType.values().length, result.getEquitySector().size());
    assertTrue(result.getWarnings().isEmpty());
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.ENERGY).compareTo(BigDecimal.ZERO));
  }

  @Test
  void shouldMapAndScaleAllocations_whenMappingFromDomain() {
    EquitySector domain = EquitySector.builder()
        .allocations(Map.of(
            EquitySectorAllocationType.ENERGY, new BigDecimal("0.12345678901"),
            EquitySectorAllocationType.TECHNOLOGY, new BigDecimal("0.2")))
        .build();
    EquitySectorResult result = mapper.toResponse(domain);

    assertEquals(2, result.getEquitySector().size());
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenNetProductsAreEmpty() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));

    EquitySectorResult result = mapper.fromNetProducts(Map.of(), warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(EquitySectorAllocationType.values().length, result.getEquitySector().size());
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.HEALTHCARE).compareTo(BigDecimal.ZERO));
  }

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));
    Map<EquitySectorAllocationType, BigDecimal> netProducts = Map.of(
        EquitySectorAllocationType.ENERGY, new BigDecimal("0.1"),
        EquitySectorAllocationType.TECHNOLOGY, new BigDecimal("0.2"));

    EquitySectorResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.ENERGY).compareTo(new BigDecimal(
        "0.1000000000")));
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY).compareTo(new BigDecimal(
        "0.2000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}
