package com.fintex.ce.application.mapper.response;

import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegion;
import com.fintex.ce.domain.enumeration.calculation.AssetAllocationRegionType;
import com.fintex.ce.domain.model.AssetAllocation;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.port.input.result.AssetAllocationResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssetAllocationResponseMapperTest {

  private final AssetAllocationResponseMapper mapper = new AssetAllocationResponseMapper();

  @Test
  void shouldReturnEmptyResult_whenDomainIsNull() {
    AssetAllocationResult result = mapper.toResponse(null);

    assertTrue(result.getWarnings().isEmpty());
    assertNull(result.getAssetAllocation());
  }

  @Test
  void shouldAggregateByRegionTypeAndIgnoreUnknownKeys_whenMappingFromDomain() {
    AssetAllocation domain = new AssetAllocation();
    domain.setAssetAllocation(Map.of(
        "CANADIAN_EQUITIES", new BigDecimal("0.12345678901"),
        "US_EQUITIES", new BigDecimal("0.2"),
        "EUROPEAN_EQUITIES", new BigDecimal("0.3"),
        "ASIA_PACIFIC_EQUITIES", new BigDecimal("0.4"),
        "UNKNOWN_KEY", new BigDecimal("0.9999")
    ));

    AssetAllocationResult result = mapper.toResponse(domain);
    Map<AssetAllocationRegionType, BigDecimal> actual = result.getAssetAllocation();

    assertEquals(3, actual.size());
    assertEquals(0, actual.get(AssetAllocationRegionType.CANADIAN_EQUITY)
        .compareTo(new BigDecimal("0.1234567890")));
    assertEquals(0, actual.get(AssetAllocationRegionType.US_EQUITY)
        .compareTo(new BigDecimal("0.2000000000")));
    assertEquals(0, actual.get(AssetAllocationRegionType.INTERNATIONAL_EQUITY)
        .compareTo(new BigDecimal("0.7000000000")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldMapNetProductsAndPreserveWarnings_whenBuildingResponse() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));
    Map<AssetAllocationRegion, BigDecimal> netProducts = Map.of(
        AssetAllocationRegion.CANADIAN_EQUITIES, new BigDecimal("0.1"),
        AssetAllocationRegion.US_EQUITIES, new BigDecimal("0.2"),
        AssetAllocationRegion.ASIA_PACIFIC_EQUITIES, new BigDecimal("0.3")
    );

    AssetAllocationResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getAssetAllocation().get(AssetAllocationRegionType.CANADIAN_EQUITY)
        .compareTo(new BigDecimal("0.1000000000")));
    assertEquals(0, result.getAssetAllocation().get(AssetAllocationRegionType.US_EQUITY)
        .compareTo(new BigDecimal("0.2000000000")));
    assertEquals(0, result.getAssetAllocation().get(AssetAllocationRegionType.INTERNATIONAL_EQUITY)
        .compareTo(new BigDecimal("0.3000000000")));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }
}

