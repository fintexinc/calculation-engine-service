package com.fintex.ce.application.mapping.response;

import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.calculation.CreditQualityRating;
import com.fintex.ce.domain.model.calculation.FixedIncomeCreditQuality;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.model.result.CreditQualityResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreditQualityResponseMapperTest {

  private final CreditQualityResponseMapper mapper = new CreditQualityResponseMapper();

  @Test
  void shouldReturnDefaultMap_whenDomainIsNull() {
    CreditQualityResult result = mapper.toResponse(null);

    assertEquals(FixedIncomeCreditQuality.values().length, result.getCreditQuality().size());
    assertTrue(result.getWarnings().isEmpty());
    assertTrue(result.getCreditQuality().containsKey(FixedIncomeCreditQuality.AAA));
    assertNull(result.getCreditQuality().get(FixedIncomeCreditQuality.AAA));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingDomainWithRatings() {
    CreditQuality domain = new CreditQuality();
    domain.setRatings(Map.of(CreditQualityRating.AAA, BigDecimal.ONE));

    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(domain));
  }

  @Test
  void shouldThrowUnsupportedOperationException_whenMappingPortfolioDomainMap() {
    assertThrows(UnsupportedOperationException.class, () -> mapper.toResponse(Map.of(), List.of()));
  }

  @Test
  void shouldApplyUserScaleAndSetWarnings_whenUsingCalculatedValues() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));
    Map<FixedIncomeCreditQuality, BigDecimal> calculated = Map.of(
        FixedIncomeCreditQuality.AAA, new BigDecimal("0.12345678901"),
        FixedIncomeCreditQuality.HIGH_YIELD, new BigDecimal("0.2")
    );

    CreditQualityResult result = mapper.fromCalculatedValues(calculated, warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(0, result.getCreditQuality().get(FixedIncomeCreditQuality.AAA)
        .compareTo(new BigDecimal("0.1234567890")));
    assertEquals(0, result.getCreditQuality().get(FixedIncomeCreditQuality.HIGH_YIELD)
        .compareTo(new BigDecimal("0.2000000000")));
  }

  @Test
  void shouldReturnDefaultMapAndPassWarnings_whenUsingEmptyResponse() {
    List<Warning> warnings = List.of(new Warning("w1", "warning"));

    CreditQualityResult result = mapper.toEmptyResponse(warnings);

    assertEquals(warnings, result.getWarnings());
    assertEquals(FixedIncomeCreditQuality.values().length, result.getCreditQuality().size());
    assertNull(result.getCreditQuality().get(FixedIncomeCreditQuality.NOT_RATED));
  }
}

