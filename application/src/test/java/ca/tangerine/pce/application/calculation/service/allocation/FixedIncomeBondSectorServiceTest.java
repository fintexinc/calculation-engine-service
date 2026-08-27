package ca.tangerine.pce.application.calculation.service.allocation;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.model.error.ErrorCode.FX_RATES_UNAVAILABLE;
import static ca.tangerine.pce.model.error.ErrorCode.SECURITY_NOT_FOUND_FOR_METRIC;
import static java.math.BigDecimal.ONE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.tangerine.pce.application.calculation.service.FxRateService;
import ca.tangerine.pce.application.calculation.service.HoldingCurrencyConverter;
import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.application.config.FxProperties;
import ca.tangerine.pce.model.domain.calculation.allocation.FixedIncomeBondSector;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import ca.tangerine.wm.commons.domain.currency.Currency;

class FixedIncomeBondSectorServiceTest {

  private final FxRateService fxRateService = mock(FxRateService.class);
  private final FixedIncomeBondSectorService service = new FixedIncomeBondSectorService(
      new PortfolioWeightCalculator(new HoldingCurrencyConverter(fxRateService, new FxProperties())));

  private PortfolioHolding bond(BigDecimal value) {
    var holding = mock(PortfolioHolding.class);
    when(holding.getValue()).thenReturn(value);
    return holding;
  }

  private FixedIncomeBondSector sector(Currency currency, Map<FixedIncomeSectorAllocationType, BigDecimal> sectors) {
    return FixedIncomeBondSector.builder().fixedIncomeBondSectors(sectors).currency(currency).build();
  }

  private PortfolioHoldingsCommand commandOf(List<PortfolioHolding> holdings) {
    var command = mock(PortfolioHoldingsCommand.class);
    when(command.getHoldings()).thenReturn(holdings);
    return command;
  }

  @Test
  void shouldEmitWarningAndPopulateUnknownBucket_whenSecurityDataIsMissing() {
    var holding = bond(BigDecimal.valueOf(100));
    when(holding.getIdsString()).thenReturn("HOLDING-1");

    var result = service.perform(commandOf(List.of(holding)), Map.of());

    assertEquals(1, result.getWarnings().size());
    assertEquals(SECURITY_NOT_FOUND_FOR_METRIC.getCode(), result.getWarnings().get(0).getCode());
    assertEquals(0,
        result.getFixedIncomeSector().get(FixedIncomeSectorAllocationType.UNKNOWN).compareTo(ONE));
  }

  @Test
  void shouldWeightByRawValueAndRescale_whenComputingMultiHoldingNetProducts() {
    var aom = bond(BigDecimal.valueOf(70));
    var rbf605 = bond(BigDecimal.valueOf(30));
    var data = Map.of(
        aom, sector(null, Map.of(
            FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.3"),
            FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.5"))),
        rbf605, sector(null, Map.of(
            FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, new BigDecimal("0.7"),
            FixedIncomeSectorAllocationType.CORPORATE_BONDS, new BigDecimal("0.1"))));

    var result = service.perform(commandOf(List.of(aom, rbf605)), data);

    // weight(aom)=0.7, weight(rbf605)=0.3
    // netProduct(GOVERNMENT_BONDS) = 0.7*0.3 + 0.3*0.7 = 0.42
    // netProduct(CORPORATE_BONDS) = 0.7*0.5 + 0.3*0.1 = 0.38 -> sum=0.80
    // after rescale: GOVERNMENT_BONDS=0.525, CORPORATE_BONDS=0.475
    var sectors = result.getFixedIncomeSector();
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS).compareTo(new BigDecimal("0.525")));
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.CORPORATE_BONDS).compareTo(new BigDecimal("0.475")));
  }

  @Test
  void shouldWeightBondsByFxAdjustedValue_whenHoldingsSpanCurrencies() {
    var cadBond = bond(BigDecimal.valueOf(100));
    var usdBond = bond(BigDecimal.valueOf(100));
    var data = Map.of(
        cadBond, sector(Currency.CAD, Map.of(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, ONE)),
        usdBond, sector(Currency.USD, Map.of(FixedIncomeSectorAllocationType.CORPORATE_BONDS, ONE)));
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(Map.of(Currency.USD, new BigDecimal("1.5")));

    var result = service.perform(commandOf(List.of(cadBond, usdBond)), data);

    // cadBond=100 CAD, usdBond=100 USD * 1.5 = 150 CAD -> weight(cad)=0.4, weight(usd)=0.6
    var sectors = result.getFixedIncomeSector();
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS).compareTo(new BigDecimal("0.4")));
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.CORPORATE_BONDS).compareTo(new BigDecimal("0.6")));
    assertTrue(result.getWarnings().isEmpty());
  }

  @Test
  void shouldFallBackToRawValues_whenFxRateIsMissing() {
    var cadBond = bond(BigDecimal.valueOf(100));
    var usdBond = bond(BigDecimal.valueOf(100));
    var data = Map.of(
        cadBond, sector(Currency.CAD, Map.of(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS, ONE)),
        usdBond, sector(Currency.USD, Map.of(FixedIncomeSectorAllocationType.CORPORATE_BONDS, ONE)));
    Map<Currency, BigDecimal> noRate = new EnumMap<>(Currency.class);
    noRate.put(Currency.USD, null);
    when(fxRateService.spotRates(anySet(), any(), any())).thenReturn(noRate);

    var result = service.perform(commandOf(List.of(cadBond, usdBond)), data);

    // no FX rate for USD -> both bonds weighted by raw value: 100/200 = 0.5 each
    var sectors = result.getFixedIncomeSector();
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.GOVERNMENT_BONDS).compareTo(new BigDecimal("0.5")));
    assertEquals(0, sectors.get(FixedIncomeSectorAllocationType.CORPORATE_BONDS).compareTo(new BigDecimal("0.5")));
    assertEquals(1, result.getWarnings().size());
    assertEquals(FX_RATES_UNAVAILABLE.getCode(), result.getWarnings().get(0).getCode());
  }
}
