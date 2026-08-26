package com.fintex.ce.adapter.webclient.mic.mapper;

import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSector;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeValue;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationWithCurrency;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.currency.CurrencyDatapoint;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.fintex.ce.test.PortfolioHoldingBuildHelper.holdingWithoutCountry;
import static org.assertj.core.api.Assertions.assertThat;

class FixedIncomeSectorAllocationMapperTest {

  private final FixedIncomeSectorAllocationMapper mapper = new FixedIncomeSectorAllocationMapper();

  @Test
  void shouldMapEverySectorBucketAndProvider_whenResponseCoversAllTypes() {
    Map<FixedIncomeSectorAllocationType, String> expectedByType = new EnumMap<>(FixedIncomeSectorAllocationType.class);
    expectedByType.put(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, "20.00");
    expectedByType.put(FixedIncomeSectorAllocationType.CORPORATE_BONDS, "18.00");
    expectedByType.put(FixedIncomeSectorAllocationType.MORTGAGE_BACKED_SECURITIES, "14.00");
    expectedByType.put(FixedIncomeSectorAllocationType.ASSET_BACKED_SECURITIES, "12.00");
    expectedByType.put(FixedIncomeSectorAllocationType.SECURITIZED_DEBT, "10.00");
    expectedByType.put(FixedIncomeSectorAllocationType.DIRECT_MORTGAGES, "9.00");
    expectedByType.put(FixedIncomeSectorAllocationType.OTHER_BONDS, "8.00");
    expectedByType.put(FixedIncomeSectorAllocationType.ST_INVESTMENTS, "6.00");
    expectedByType.put(FixedIncomeSectorAllocationType.UNKNOWN, "3.00");

    List<FixedIncomeSectorAllocationTypeValue> values = expectedByType.entrySet().stream()
        .map(typeValue -> entry(typeValue.getKey(), typeValue.getValue()))
        .toList();
    FixedIncomeSectorAllocationWithCurrency micResponse = response(values, DataProvider.MORNINGSTAR);

    PortfolioHolding holding = holdingWithoutCountry("AGG", null, FinancialInstrumentType.ETF, (BigDecimal) null);

    FixedIncomeBondSector result = mapper.map(micResponse, holding);

    assertThat(result.getFixedIncomeBondSectors()).hasSize(expectedByType.size());
    expectedByType.forEach((type, value) -> assertThat(result.getFixedIncomeBondSectors().get(type))
        .isEqualByComparingTo(value));
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF);
    assertThat(result.getProviders()).containsExactly(DataProvider.MORNINGSTAR);
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyAllocations_whenResponseIsNullOrHasNoAllocation(
      FixedIncomeSectorAllocationWithCurrency micResponse) {
    PortfolioHolding holding = holdingWithoutCountry("TEST.ID", null, FinancialInstrumentType.FUND, (BigDecimal) null);

    FixedIncomeBondSector result = mapper.map(micResponse, holding);

    assertThat(result.getFixedIncomeBondSectors()).isEmpty();
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.FUND);
    assertThat(result.getProviders()).isEmpty();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullInner = new FixedIncomeSectorAllocationWithCurrency();
    nullInner.setFixedIncomeSectorAllocation(null);

    return Stream.of(
        Arguments.of((FixedIncomeSectorAllocationWithCurrency) null),
        Arguments.of(nullInner),
        Arguments.of(response(null)),
        Arguments.of(response(List.of())));
  }

  @Test
  void shouldFilterOutEntriesWithNullTypeOrValue() {
    FixedIncomeSectorAllocationWithCurrency micResponse = response(List.of(
        entry(FixedIncomeSectorAllocationType.OTHER_BONDS, "12.0"),
        new FixedIncomeSectorAllocationTypeValue(null, BigDecimal.valueOf(5.0), null, null),
        new FixedIncomeSectorAllocationTypeValue(FixedIncomeSectorAllocationType.ST_INVESTMENTS, null, null, null)));

    FixedIncomeBondSector result = mapper.map(micResponse, holdingWithoutCountry("TEST.ID", null,
        FinancialInstrumentType.ETF, (BigDecimal) null));

    assertThat(result.getFixedIncomeBondSectors()).hasSize(1);
    assertThat(result.getFixedIncomeBondSectors()).containsKey(FixedIncomeSectorAllocationType.OTHER_BONDS);
  }

  @Test
  void shouldSumDuplicateBuckets() {
    FixedIncomeSectorAllocationWithCurrency micResponse = response(List.of(
        entry(FixedIncomeSectorAllocationType.CORPORATE_BONDS, "30.0"),
        entry(FixedIncomeSectorAllocationType.CORPORATE_BONDS, "10.0")));

    FixedIncomeBondSector result = mapper.map(micResponse, holdingWithoutCountry("TEST.ID", null,
        FinancialInstrumentType.ETF, (BigDecimal) null));

    assertThat(result.getFixedIncomeBondSectors()).hasSize(1);
    assertThat(result.getFixedIncomeBondSectors().get(FixedIncomeSectorAllocationType.CORPORATE_BONDS))
        .isEqualByComparingTo("40.0");
  }

  @Test
  void shouldMapCurrency_whenResponseHasCurrency() {
    FixedIncomeSectorAllocationWithCurrency micResponse = response(List.of(
        entry(FixedIncomeSectorAllocationType.CORPORATE_BONDS, "30.0")));
    CurrencyDatapoint currency = new CurrencyDatapoint();
    currency.setValue(Currency.USD);
    micResponse.setCurrency(currency);

    FixedIncomeBondSector result = mapper.map(micResponse, holdingWithoutCountry("AGG", null,
        FinancialInstrumentType.ETF, (BigDecimal) null));

    assertThat(result.getCurrency()).isEqualTo(Currency.USD);
  }

  @Test
  void shouldMapNullCurrency_whenResponseHasNoCurrency() {
    FixedIncomeBondSector result = mapper.map(response(List.of(
        entry(FixedIncomeSectorAllocationType.CORPORATE_BONDS, "30.0"))),
        holdingWithoutCountry("AGG", null, FinancialInstrumentType.ETF, (BigDecimal) null));

    assertThat(result.getCurrency()).isNull();
  }

  private static FixedIncomeSectorAllocationWithCurrency response(List<FixedIncomeSectorAllocationTypeValue> values,
      DataProvider... providers) {
    var inner = new FixedIncomeSectorAllocation();
    inner.setValue(values);
    if (providers.length > 0) {
      inner.setDataProviders(List.of(providers));
    }
    var wrapper = new FixedIncomeSectorAllocationWithCurrency();
    wrapper.setFixedIncomeSectorAllocation(inner);
    return wrapper;
  }

  private FixedIncomeSectorAllocationTypeValue entry(FixedIncomeSectorAllocationType type, String value) {
    return new FixedIncomeSectorAllocationTypeValue(type, new BigDecimal(value), null, null);
  }
}
