package com.fintex.ce.application.mapping.response;

import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.error.Notification;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FixedIncomeSectorResponseMapperTest {

  private final FixedIncomeSectorResponseMapper mapper = new FixedIncomeSectorResponseMapper();

  @Test
  void toEmptyResponse_shouldReturnDefaultMapWithNulls() {
    var result = mapper.toEmptyResponse(List.of());

    assertEquals(FixedIncomeSectorAllocationType.values().length, result.getFixedIncomeSector().size());
    assertTrue(result.getWarnings().isEmpty());
    assertTrue(result.getFixedIncomeSector().values().stream().allMatch(Objects::isNull));
  }

  @Test
  void toEmptyResponse_shouldPassWarnings() {
    var warnings = List.of(Notification.builder().uuid("w1").message("warning").build());

    var result = mapper.toEmptyResponse(warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(FixedIncomeSectorAllocationType.values().length, result.getFixedIncomeSector().size());
    assertTrue(result.getFixedIncomeSector().values().stream().allMatch(Objects::isNull));
  }

  @Test
  void fromNetProducts_shouldNormalizeAndScaleValues() {
    var warnings = List.of(Notification.builder().uuid("w1").message("warning").build());
    var netProducts = Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.3"),
        FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.7"));

    FixedIncomeSectorResult result = mapper.fromNetProducts(netProducts, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS)
        .compareTo(new BigDecimal("0.3000000000")));
    assertEquals(0, result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.CORPORATE_BONDS)
        .compareTo(new BigDecimal("0.7000000000")));
  }

  @Test
  void shouldNormalizeSectorsToSumToOne_whenRawSectorsTotalLessThanOne() {
    var netProducts = Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.4"),
        FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.6"));

    var result = mapper.fromNetProducts(netProducts, List.of());

    var sum = result.getFixedIncomeSector().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, sum.compareTo(BigDecimal.ONE));
  }

  @Test
  void shouldNormalizeSectorsToNetTotal_whenNetProductsContainLongAndShortExposure() {
    var netProducts = Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.100"),
        FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("-0.099"));

    var result = mapper.fromNetProducts(netProducts, List.of());

    assertTrue(result.getWarnings().isEmpty());
    assertEquals(0, result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS)
        .compareTo(new BigDecimal("100.0000000000")));
    assertEquals(0, result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.CORPORATE_BONDS)
        .compareTo(new BigDecimal("-99.0000000000")));
    var sum = result.getFixedIncomeSector().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, sum.compareTo(BigDecimal.ONE));
  }

  @Test
  void shouldNormalizePartialSectorDataToSumToOne_whenNotAllSectorsPresent() {
    var netProducts = Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.3"),
        FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.6"),
        FixedIncomeSectorAllocationType.MORTGAGE_BACKED_SECURITIES, new BigDecimal("0.1"));

    var result = mapper.fromNetProducts(netProducts, List.of());

    var sum = result.getFixedIncomeSector().values().stream()
        .filter(Objects::nonNull)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    assertEquals(0, sum.compareTo(BigDecimal.ONE));
  }

  @Test
  void shouldIncludeUnknownBucketInNormalization_whenSomeHoldingsAreUnclassified() {
    var netProducts = Map.of(
        FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.3"),
        FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.3"),
        FixedIncomeSectorAllocationType.UNKNOWN, new BigDecimal("0.4"));

    var result = mapper.fromNetProducts(netProducts, List.of());

    assertEquals(0, result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS)
        .compareTo(new BigDecimal("0.3000000000")));
    assertEquals(0, result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.CORPORATE_BONDS)
        .compareTo(new BigDecimal("0.3000000000")));
    assertEquals(0, result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.UNKNOWN)
        .compareTo(new BigDecimal("0.4000000000")));
  }
}
