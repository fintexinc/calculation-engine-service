package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeNameValue;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FixedIncomeSectorAllocationMapperTest {

  private final FixedIncomeSectorAllocationMapper mapper = new FixedIncomeSectorAllocationMapper();

  @Test
  void shouldMapAllFieldsCorrectly_whenResponseHasMultipleSectorsAndProvider() {
    var governmentEntry = createEntry(FixedIncomeSectorAllocationType.GOVERNMENT, "45.20");
    var corporateEntry = createEntry(FixedIncomeSectorAllocationType.CORPORATE, "35.50");
    var securitizedEntry = createEntry(FixedIncomeSectorAllocationType.SECURITIZED, "19.30");

    var smsResponse = new FixedIncomeSectorAllocation();
    smsResponse.setAllocation(List.of(governmentEntry, corporateEntry, securitizedEntry));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    PortfolioHolding holding = createHolding("AGG", FinancialInstrumentType.ETF);

    FixedIncomeBondSecurities result = mapper.map(smsResponse, holding);

    assertThat(result.getFixedIncomeBondSectors()).hasSize(3);
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS))
        .isEqualByComparingTo("45.20");
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS))
        .isEqualByComparingTo("35.50");
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.MORTGAGE_BACKED_SECURITIES))
        .isEqualByComparingTo("19.30");
    assertThat(result.getHoldingId()).isEqualTo("AGG");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyAllocations_whenResponseIsNullOrHasNoAllocation(
      FixedIncomeSectorAllocation smsResponse) {
    PortfolioHolding holding = createHolding("TEST.ID", FinancialInstrumentType.FUND);

    FixedIncomeBondSecurities result = mapper.map(smsResponse, holding);

    assertThat(result.getFixedIncomeBondSectors()).isEmpty();
    assertThat(result.getHoldingId()).isEqualTo("TEST.ID");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.FUND);
    assertThat(result.getProviders()).isEmpty();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullAllocationResponse = new FixedIncomeSectorAllocation();
    nullAllocationResponse.setAllocation(null);

    var emptyAllocationResponse = new FixedIncomeSectorAllocation();
    emptyAllocationResponse.setAllocation(List.of());

    return Stream.of(
        Arguments.of((FixedIncomeSectorAllocation) null),
        Arguments.of(nullAllocationResponse),
        Arguments.of(emptyAllocationResponse));
  }

  @Test
  void shouldFilterOutEntriesWithNullTypeOrValue() {
    var validEntry = createEntry(FixedIncomeSectorAllocationType.MUNICIPAL, "12.0");
    var nullTypeEntry = new FixedIncomeSectorAllocationTypeNameValue();
    nullTypeEntry.setType(null);
    nullTypeEntry.setValue(BigDecimal.valueOf(5.0));
    var nullValueEntry = new FixedIncomeSectorAllocationTypeNameValue();
    nullValueEntry.setType(FixedIncomeSectorAllocationType.CASH);
    nullValueEntry.setValue(null);

    var smsResponse = new FixedIncomeSectorAllocation();
    smsResponse.setAllocation(List.of(validEntry, nullTypeEntry, nullValueEntry));

    FixedIncomeBondSecurities result = mapper.map(smsResponse, createHolding("TEST.ID",
        FinancialInstrumentType.ETF_CANADA));

    assertThat(result.getFixedIncomeBondSectors()).hasSize(1);
    assertThat(result.getFixedIncomeBondSectors()).containsKey(FixedIncomeSecuritiesAllocationType.OTHER_BONDS);
  }

  @Test
  void shouldMapAllSectorTypesToCeEquivalents() {
    List<FixedIncomeSectorAllocationTypeNameValue> entries = List.of(
        createEntry(FixedIncomeSectorAllocationType.GOVERNMENT, "25.0"),
        createEntry(FixedIncomeSectorAllocationType.CORPORATE, "30.0"),
        createEntry(FixedIncomeSectorAllocationType.CASH, "10.0"),
        createEntry(FixedIncomeSectorAllocationType.SECURITIZED, "15.0"),
        createEntry(FixedIncomeSectorAllocationType.MUNICIPAL, "15.0"),
        createEntry(FixedIncomeSectorAllocationType.DERIVATIVE, "5.0"));

    var smsResponse = new FixedIncomeSectorAllocation();
    smsResponse.setAllocation(entries);

    FixedIncomeBondSecurities result = mapper.map(smsResponse, createHolding("FULL.TEST", FinancialInstrumentType.ETF));

    assertThat(result.getFixedIncomeBondSectors()).hasSize(6);
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS))
        .isEqualByComparingTo("25.0");
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS))
        .isEqualByComparingTo("30.0");
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.ST_INVESTMENTS))
        .isEqualByComparingTo("10.0");
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.MORTGAGE_BACKED_SECURITIES))
        .isEqualByComparingTo("15.0");
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.OTHER_BONDS))
        .isEqualByComparingTo("15.0");
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSecuritiesAllocationType.ASSET_BACKED_SECURITIES))
        .isEqualByComparingTo("5.0");
  }

  private FixedIncomeSectorAllocationTypeNameValue createEntry(
      FixedIncomeSectorAllocationType type, String value) {
    var entry = new FixedIncomeSectorAllocationTypeNameValue();
    entry.setType(type);
    entry.setValue(new BigDecimal(value));
    return entry;
  }

  private PortfolioHolding createHolding(String securityId, FinancialInstrumentType holdingType) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new PortfolioHolding(null, holdingType, identifier);
  }
}