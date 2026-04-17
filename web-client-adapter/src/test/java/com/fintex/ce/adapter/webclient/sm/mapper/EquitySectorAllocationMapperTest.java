package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.EquitySector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocation;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationType;
import com.fintex.wm.commons.domain.allocation.EquitySectorAllocationTypeNameValue;
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

class EquitySectorAllocationMapperTest {

  private final EquitySectorAllocationMapper mapper = new EquitySectorAllocationMapper();

  @Test
  void shouldMapAllFieldsCorrectly_whenResponseHasMultipleSectorsAndProvider() {
    var techEntry = createEntry(EquitySectorAllocationType.TECHNOLOGY, "28.5");
    var healthEntry = createEntry(EquitySectorAllocationType.HEALTHCARE, "15.3");
    var energyEntry = createEntry(EquitySectorAllocationType.ENERGY, "8.7");

    var smsResponse = new EquitySectorAllocation();
    smsResponse.setAllocation(List.of(techEntry, healthEntry, energyEntry));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    PortfolioHolding holding = createHolding("XIU.TO");

    EquitySector result = mapper.map(smsResponse, holding);

    assertThat(result.getAllocations()).hasSize(3);
    assertThat(result.getAllocations().get(EquitySectorAllocationType.TECHNOLOGY)).isEqualByComparingTo("28.5");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.HEALTHCARE)).isEqualByComparingTo("15.3");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.ENERGY)).isEqualByComparingTo("8.7");
    assertThat(result.getHoldingId()).isEqualTo("XIU.TO");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyAllocations_whenResponseIsNullOrHasNoAllocation(
      EquitySectorAllocation smsResponse) {
    PortfolioHolding holding = createHolding("TEST.ID");

    EquitySector result = mapper.map(smsResponse, holding);

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getHoldingId()).isEqualTo("TEST.ID");
    assertThat(result.getProviders()).isEmpty();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullAllocationResponse = new EquitySectorAllocation();
    nullAllocationResponse.setAllocation(null);

    var emptyAllocationResponse = new EquitySectorAllocation();
    emptyAllocationResponse.setAllocation(List.of());

    return Stream.of(
        Arguments.of((EquitySectorAllocation) null),
        Arguments.of(nullAllocationResponse),
        Arguments.of(emptyAllocationResponse));
  }

  @Test
  void shouldFilterOutEntriesWithNullType() {
    var validEntry = createEntry(EquitySectorAllocationType.INDUSTRIALS, "12.0");
    var nullTypeEntry = new EquitySectorAllocationTypeNameValue();
    nullTypeEntry.setType(null);
    nullTypeEntry.setValue(BigDecimal.valueOf(5.0));

    var smsResponse = new EquitySectorAllocation();
    smsResponse.setAllocation(List.of(validEntry, nullTypeEntry));

    EquitySector result = mapper.map(smsResponse, createHolding("TEST.ID"));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations()).containsKey(EquitySectorAllocationType.INDUSTRIALS);
    assertThat(result.getAllocations()).doesNotContainKey(null);
  }

  @Test
  void shouldMapAllSectorTypes() {
    List<EquitySectorAllocationTypeNameValue> entries = List.of(
        createEntry(EquitySectorAllocationType.BASIC_MATERIALS, "5.0"),
        createEntry(EquitySectorAllocationType.COMMUNICATION_SERVICES, "8.0"),
        createEntry(EquitySectorAllocationType.CONSUMER_CYCLICAL, "10.0"),
        createEntry(EquitySectorAllocationType.CONSUMER_DEFENSIVE, "7.0"),
        createEntry(EquitySectorAllocationType.ENERGY, "6.0"),
        createEntry(EquitySectorAllocationType.FINANCIAL_SERVICES, "18.0"),
        createEntry(EquitySectorAllocationType.HEALTHCARE, "12.0"),
        createEntry(EquitySectorAllocationType.INDUSTRIALS, "11.0"),
        createEntry(EquitySectorAllocationType.REAL_ESTATE, "4.0"),
        createEntry(EquitySectorAllocationType.TECHNOLOGY, "15.0"),
        createEntry(EquitySectorAllocationType.UTILITIES, "4.0"));

    var smsResponse = new EquitySectorAllocation();
    smsResponse.setAllocation(entries);

    EquitySector result = mapper.map(smsResponse, createHolding("FULL.TEST"));

    assertThat(result.getAllocations()).hasSize(11);
    assertThat(result.getAllocations().get(EquitySectorAllocationType.BASIC_MATERIALS)).isEqualByComparingTo("5.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.COMMUNICATION_SERVICES)).isEqualByComparingTo(
        "8.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.CONSUMER_CYCLICAL)).isEqualByComparingTo("10.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.CONSUMER_DEFENSIVE)).isEqualByComparingTo("7.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.ENERGY)).isEqualByComparingTo("6.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.FINANCIAL_SERVICES)).isEqualByComparingTo("18.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.HEALTHCARE)).isEqualByComparingTo("12.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.INDUSTRIALS)).isEqualByComparingTo("11.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.REAL_ESTATE)).isEqualByComparingTo("4.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.TECHNOLOGY)).isEqualByComparingTo("15.0");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.UTILITIES)).isEqualByComparingTo("4.0");
  }

  private EquitySectorAllocationTypeNameValue createEntry(
      EquitySectorAllocationType type, String value) {
    var entry = new EquitySectorAllocationTypeNameValue();
    entry.setType(type);
    entry.setValue(new BigDecimal(value));
    return entry;
  }

  private PortfolioHolding createHolding(String securityId) {
    var identifier = new SecurityIdentifier();
    identifier.setId(securityId);
    return new PortfolioHolding(null, FinancialInstrumentType.ETF_CANADA, identifier);
  }
}
