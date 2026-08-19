package com.fintex.ce.application.util;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityDataValidatorTest {

  @Test
  void passes_whenAllMandatoryHoldingsHaveRawDataEntry() {
    final var fund = holdingWithoutCountry(new SecurityIdentifier("CIG-001", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.MUTUAL_FUND, new BigDecimal("100"));
    final var rawData = Map.of(fund, "fee-data");

    assertThatCode(() -> SecurityDataValidator.requireDataForEveryHolding(rawData, List.of(fund), h -> true))
        .doesNotThrowAnyException();
  }

  @Test
  void throws_whenMandatoryHoldingHasNoRawDataEntry() {
    final var present = holdingWithoutCountry(new SecurityIdentifier("CIG-001", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.MUTUAL_FUND, new BigDecimal("100"));
    final var missing = holdingWithoutCountry(new SecurityIdentifier("US-MISSING", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.MUTUAL_FUND, new BigDecimal("100"));
    final var rawData = Map.of(present, "fee-data");

    assertThatThrownBy(
        () -> SecurityDataValidator.requireDataForEveryHolding(rawData, List.of(present, missing), h -> true))
        .isInstanceOf(CalculationException.class)
        .hasMessageContaining("No data returned for holding")
        .hasMessageContaining("US-MISSING");
  }

  @Test
  void exemptHoldings_areSkipped_evenWhenAbsentFromRawData() {
    final var fund = holdingWithoutCountry(new SecurityIdentifier("CIG-001", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.MUTUAL_FUND, new BigDecimal("100"));
    final var stock = holdingWithoutCountry(new SecurityIdentifier("AAPL", FiIdentifierType.MORNINGSTAR_ID),
        FinancialInstrumentType.STOCK, new BigDecimal("100"));
    final var rawData = Map.of(fund, "fee-data");

    // Only fund holdings are mandatory — the stock is exempt and its absence from rawData is fine.
    assertThatCode(() -> SecurityDataValidator.requireDataForEveryHolding(
        rawData,
        List.of(fund, stock),
        h -> h.getHoldingType() == FinancialInstrumentType.MUTUAL_FUND))
        .doesNotThrowAnyException();
  }

  @Test
  void duplicateIds_passCheck_whenRawDataHasOnlyOneEntryForTheSharedId() {
    // The same fund held twice with different market values — equals() distinguishes them, but they share an
    // identifier. The data source dedupes by identifier so the rawData map might only carry one entry for the shared
    // id. The validator must still accept both because the identifier IS represented in the response.
    final var sharedId = new SecurityIdentifier("CIG-DUP", FiIdentifierType.MORNINGSTAR_ID);
    final var fundA = holdingWithoutCountry(sharedId, FinancialInstrumentType.MUTUAL_FUND,
        new BigDecimal("100"));
    final var fundB = holdingWithoutCountry(sharedId, FinancialInstrumentType.MUTUAL_FUND,
        new BigDecimal("300"));
    final var rawData = Map.of(fundA, "fee-data"); // only one entry, despite two holdings requested

    assertThatCode(() -> SecurityDataValidator.requireDataForEveryHolding(
        rawData, List.of(fundA, fundB), h -> true))
        .doesNotThrowAnyException();
  }

  @Test
  void holdingWithNullIdentifier_isTreatedAsMissing_whenMandatory() {
    final var noId = PortfolioHolding.builder()
        .value(new BigDecimal("100"))
        .holdingType(FinancialInstrumentType.MUTUAL_FUND)
        .securityIdentifier(null)
        .build();

    assertThatThrownBy(() -> SecurityDataValidator.requireDataForEveryHolding(Map.of(), List.of(noId), h -> true))
        .isInstanceOf(CalculationException.class);
  }

}