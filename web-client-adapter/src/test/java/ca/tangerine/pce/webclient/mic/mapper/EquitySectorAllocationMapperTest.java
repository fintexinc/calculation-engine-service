package ca.tangerine.pce.webclient.mic.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static ca.tangerine.pce.util.PortfolioHoldingBuildHelper.holding;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.model.domain.calculation.allocation.EquitySector;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocation;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationType;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationTypeValue;
import ca.tangerine.wm.commons.domain.allocation.EquitySectorAllocationWithCurrency;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.currency.CurrencyDatapoint;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

class EquitySectorAllocationMapperTest {

  private final EquitySectorAllocationMapper mapper = new EquitySectorAllocationMapper();

  @Test
  void shouldMapAllFieldsCorrectly_whenResponseHasMultipleSectorsAndProvider() {
    EquitySectorAllocationWithCurrency micResponse = response(List.of(
        createEntry(EquitySectorAllocationType.TECHNOLOGY, "28.5"),
        createEntry(EquitySectorAllocationType.HEALTHCARE, "15.3"),
        createEntry(EquitySectorAllocationType.ENERGY, "8.7")),
        DataProvider.MORNINGSTAR);

    PortfolioHolding holding = holding(new SecurityIdentifier("XIU.TO", null), FinancialInstrumentType.ETF,
        Country.CANADA, (BigDecimal) null);

    EquitySector result = mapper.map(micResponse, holding);

    assertThat(result.getAllocations()).hasSize(3);
    assertThat(result.getAllocations().get(EquitySectorAllocationType.TECHNOLOGY)).isEqualByComparingTo("28.5");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.HEALTHCARE)).isEqualByComparingTo("15.3");
    assertThat(result.getAllocations().get(EquitySectorAllocationType.ENERGY)).isEqualByComparingTo("8.7");
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @Test
  void shouldMapCurrency_whenResponseHasCurrency() {
    EquitySectorAllocationWithCurrency micResponse = response(List.of(
        createEntry(EquitySectorAllocationType.TECHNOLOGY, "28.5")), DataProvider.MORNINGSTAR);
    CurrencyDatapoint currency = new CurrencyDatapoint();
    currency.setValue(Currency.USD);
    micResponse.setCurrency(currency);

    EquitySector result = mapper.map(micResponse, holding(new SecurityIdentifier("XIU.TO", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getCurrency()).isEqualTo(Currency.USD);
  }

  @Test
  void shouldMapNullCurrency_whenResponseHasNoCurrency() {
    EquitySector result = mapper.map(response(List.of(
        createEntry(EquitySectorAllocationType.TECHNOLOGY, "28.5"))), holding(new SecurityIdentifier("XIU.TO", null),
            FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getCurrency()).isNull();
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyAllocations_whenResponseIsNullOrHasNoAllocation(
      EquitySectorAllocationWithCurrency micResponse) {
    PortfolioHolding holding = holding(new SecurityIdentifier("TEST.ID", null), FinancialInstrumentType.ETF,
        Country.CANADA, (BigDecimal) null);

    EquitySector result = mapper.map(micResponse, holding);

    assertThat(result.getAllocations()).isEmpty();
    assertThat(result.getProviders()).isEmpty();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullInner = new EquitySectorAllocationWithCurrency();
    nullInner.setEquitySectorAllocation(null);

    return Stream.of(
        Arguments.of((EquitySectorAllocationWithCurrency) null),
        Arguments.of(nullInner),
        Arguments.of(response(null)),
        Arguments.of(response(List.of())));
  }

  @Test
  void shouldFilterOutEntriesWithNullType() {
    var validEntry = createEntry(EquitySectorAllocationType.INDUSTRIALS, "12.0");
    var nullTypeEntry = new EquitySectorAllocationTypeValue();
    nullTypeEntry.setType(null);
    nullTypeEntry.setValue(BigDecimal.valueOf(5.0));

    EquitySector result = mapper.map(response(List.of(validEntry, nullTypeEntry)), holding(new SecurityIdentifier(
        "TEST.ID", null), FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

    assertThat(result.getAllocations()).hasSize(1);
    assertThat(result.getAllocations()).containsKey(EquitySectorAllocationType.INDUSTRIALS);
    assertThat(result.getAllocations()).doesNotContainKey(null);
  }

  @Test
  void shouldMapAllSectorTypes() {
    List<EquitySectorAllocationTypeValue> entries = List.of(
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

    EquitySector result = mapper.map(response(entries), holding(new SecurityIdentifier("FULL.TEST", null),
        FinancialInstrumentType.ETF, Country.CANADA, (BigDecimal) null));

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

  private static EquitySectorAllocationWithCurrency response(List<EquitySectorAllocationTypeValue> values,
      DataProvider... providers) {
    var inner = new EquitySectorAllocation();
    inner.setAllocations(values);
    if (providers.length > 0) {
      inner.setDataProviders(List.of(providers));
    }
    var wrapper = new EquitySectorAllocationWithCurrency();
    wrapper.setEquitySectorAllocation(inner);
    return wrapper;
  }

  private EquitySectorAllocationTypeValue createEntry(
      EquitySectorAllocationType type, String value) {
    var entry = new EquitySectorAllocationTypeValue();
    entry.setType(type);
    entry.setValue(new BigDecimal(value));
    return entry;
  }

}
