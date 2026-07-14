package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.result.allocation.EquitySectorResult;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EquitySectorResponseMapperTest {

  private final EquitySectorResponseMapper mapper = new EquitySectorResponseMapper();

  @Test
  void shouldMapAndScaleValues_whenUsingFromNetProducts() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());
    Map<EquitySectorAllocationType, BigDecimal> netProducts = Map.of(
        EquitySectorAllocationType.ENERGY, new BigDecimal("0.3"),
        EquitySectorAllocationType.TECHNOLOGY, new BigDecimal("0.7"));

    EquitySectorResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.ENERGY).compareTo(new BigDecimal(
        "0.3000000000")));
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY).compareTo(new BigDecimal(
        "0.7000000000")));
    assertNull(result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN));
  }

  @Test
  void shouldNormalizeSectorsToSumToOne_whenRawSectorsTotalLessThanOne() {
    Map<EquitySectorAllocationType, BigDecimal> netProducts = Map.of(
        EquitySectorAllocationType.ENERGY, new BigDecimal("0.2"),
        EquitySectorAllocationType.TECHNOLOGY, new BigDecimal("0.2"));

    EquitySectorResult result = mapper.fromNetProducts(netProducts, List.of());

    BigDecimal sum = result.getEquitySector().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, sum.compareTo(BigDecimal.ONE));
  }

  @Test
  void shouldIncludeUnknownBucketInNormalization_whenSomeHoldingsAreUnclassified() {
    Map<EquitySectorAllocationType, BigDecimal> netProducts = Map.of(
        EquitySectorAllocationType.ENERGY, new BigDecimal("0.2"),
        EquitySectorAllocationType.TECHNOLOGY, new BigDecimal("0.2"),
        EquitySectorAllocationType.UNKNOWN, new BigDecimal("0.4"));

    EquitySectorResult result = mapper.fromNetProducts(netProducts, List.of());

    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.ENERGY).compareTo(new BigDecimal("0.25")));
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.TECHNOLOGY)
        .compareTo(new BigDecimal("0.25")));
    assertEquals(0, result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN).compareTo(new BigDecimal("0.5")));
  }

  @Test
  void toEmptyResponse_shouldReturnDefaultMapWithNullsAndPassWarnings() {
    List<Notification> warnings = List.of(Notification.builder().uuid("w1").message("warning").build());

    EquitySectorResult result = mapper.toEmptyResponse(warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(EquitySectorAllocationType.values().length, result.getEquitySector().size());
    assertNull(result.getEquitySector().get(EquitySectorAllocationType.HEALTHCARE));
    assertNull(result.getEquitySector().get(EquitySectorAllocationType.UNKNOWN));
  }
}
