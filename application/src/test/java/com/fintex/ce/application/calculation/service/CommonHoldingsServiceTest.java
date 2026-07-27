package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.config.TopHoldingsProperties;
import com.fintex.ce.application.constant.AccumulateHoldingType;
import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CommonHoldingsServiceTest {

  private FxRateService fxRateService;
  private TopHoldingsProperties properties;
  private CommonHoldingsService service;

  @BeforeEach
  void setup() {
    fxRateService = mock(FxRateService.class);
    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> {
      Set<Currency> src = inv.getArgument(0);
      return src.stream().collect(Collectors.toMap(c -> c, c -> BigDecimal.ONE));
    });
    HoldingCurrencyConverter converter = new HoldingCurrencyConverter(fxRateService, new FxProperties());
    properties = new TopHoldingsProperties();
    properties.setAccumulateTypes(EnumSet.of(AccumulateHoldingType.E));
    service = new CommonHoldingsService(converter, properties);
  }

  @Test
  void shouldComputeWeightedTopHoldings_whenTwoEtfsShareNames() {
    PortfolioHolding parentA = etfHolding("AAA", 60_000);
    PortfolioHolding parentB = etfHolding("BBB", 40_000);

    Map<PortfolioHolding, CommonTopHoldings> sms = new LinkedHashMap<>();
    sms.put(parentA, topHoldings(Currency.CAD,
        equityHolding("Alpha Corp", "0.20"),
        equityHolding("Bravo Corp", "0.18"),
        equityHolding("Charlie Corp", "0.16"),
        equityHolding("Delta Corp", "0.14"),
        equityHolding("Echo Corp", "0.12")));
    sms.put(parentB, topHoldings(Currency.CAD,
        equityHolding("Alpha Corp", "0.25"),
        equityHolding("Bravo Corp", "0.20"),
        equityHolding("Golf Corp", "0.15"),
        equityHolding("Hotel Corp", "0.12"),
        equityHolding("India Corp", "0.10")));

    TopCommonHoldingsCommand command = command(List.of(parentA, parentB), 5);

    TopCommonHoldingsResult result = service.perform(command, sms);

    assertThat(result.getCommonHoldings()).hasSize(5);
    List<String> names = result.getCommonHoldings().stream().map(TopCommonHoldingData::getName).toList();
    assertThat(names).containsExactly("Alpha Corp", "Bravo Corp", "Charlie Corp", "Delta Corp", "Echo Corp");

    BigDecimal alpha = result.getCommonHoldings().get(0).getAllocation();
    assertThat(alpha).isEqualByComparingTo("0.22");
  }

  @Test
  void shouldFxConvertUsdHoldingValue_whenComputingPortfolioWeight() {
    PortfolioHolding cad = etfHolding("CAD-ETF", 50_000);
    PortfolioHolding usd = etfHolding("USD-ETF", 50_000);

    Map<PortfolioHolding, CommonTopHoldings> sms = new LinkedHashMap<>();
    sms.put(cad, topHoldings(Currency.CAD, equityHolding("Alpha Corp", "0.50")));
    sms.put(usd, topHoldings(Currency.USD, equityHolding("Alpha Corp", "0.50")));

    when(fxRateService.spotRates(anySet(), any(), any())).thenAnswer(inv -> Map.of(
        Currency.CAD, BigDecimal.ONE,
        Currency.USD, new BigDecimal("2")));

    TopCommonHoldingsCommand command = command(List.of(cad, usd), 5);

    TopCommonHoldingsResult result = service.perform(command, sms);

    // CAD: 50000 -> 50000 CAD weight 1/3
    // USD: 50000 -> 100000 CAD weight 2/3
    // Alpha weight = 1/3 * 0.5 + 2/3 * 0.5 = 0.5
    assertThat(result.getCommonHoldings()).hasSize(1);
    assertThat(result.getCommonHoldings().get(0).getAllocation()).isEqualByComparingTo("0.5");
  }

  @Test
  void shouldThrowMissingCurrencyFromFds_whenSmsOmitsCurrency() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent,
        topHoldings(null, equityHolding("Alpha Corp", "0.50")));

    TopCommonHoldingsCommand command = command(List.of(parent), 10);

    assertCalculationFails(command, sms, "HOLDING_MISSING_CURRENCY_FROM_FDS");
  }

  @Test
  void shouldThrowTch001_whenMandatoryHoldingHasNoUnderlyings() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD));

    TopCommonHoldingsCommand command = command(List.of(parent), 10);

    assertCalculationFails(command, sms, "HOLDING_MISSING_UNDERLYING_HOLDINGS");
  }

  @Test
  void shouldThrowSmsNoData_whenSmsReturnsNothingForMandatoryHolding() {
    PortfolioHolding parent = etfHolding("AAA", 1000);

    TopCommonHoldingsCommand command = command(List.of(parent), 10);

    assertCalculationFails(command, Map.of(), "NO_SECURITY_DATA_FOR_HOLDING");
  }

  @Test
  void shouldFallbackToDefaults_whenCommandValuesAreNullOrEmpty() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent,
        topHoldings(Currency.CAD, equityHolding("Alpha Corp", "1.0")));

    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(List.of(parent));
    // numOfTopCommonHoldings = null -> default 10
    // accumulateHoldingTypes = null -> default Set.of(E)

    TopCommonHoldingsResult result = service.perform(command, sms);

    assertThat(result.getCommonHoldings()).hasSize(1);
    assertThat(result.getCommonHoldings().get(0).getName()).isEqualTo("Alpha Corp");
  }

  @Test
  void shouldThrowMissingWeightingFromFds_whenChildWeightIsNull() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding child = equityHolding("Alpha Corp", "0.5");
    child.setWeight(null);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, child));

    TopCommonHoldingsCommand command = command(List.of(parent), 10);

    assertCalculationFails(command, sms, "HOLDING_MISSING_WEIGHTING_FROM_FDS");
  }

  @ParameterizedTest(name = "[{index}] {0} -> isOfType STOCK = leaf short-circuit applies")
  @EnumSource(value = FinancialInstrumentType.class, names = {"STOCK_US", "STOCK_CANADA", "STOCK"})
  void shouldShortCircuit_whenRequestHoldingIsAnyStockVariant(FinancialInstrumentType stockType) {
    PortfolioHolding stock = portfolioHolding("STK", 1000, stockType);
    CommonHolding selfEquity = equityHolding("Alpha Corp", "0.42");
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(stock, topHoldings(Currency.CAD, selfEquity));

    TopCommonHoldingsResult result = service.perform(command(List.of(stock), 10), sms);

    // Leaf-stock short-circuit: child weight 0.42 is ignored — inherited weight (100%) is used directly.
    assertThat(result.getCommonHoldings()).hasSize(1);
    assertThat(result.getCommonHoldings().get(0).getAllocation()).isEqualByComparingTo("1.0");
  }

  @ParameterizedTest(name = "[{index}] type={0} accumulated={1}")
  @CsvSource({
      "E,  true",
      "B,  true",
      "BC, true",
      "ER, true",
      "X,  false"
  })
  void shouldHonourAccumulateTypesFromYaml(String type, boolean accumulated) {
    properties.setAccumulateTypes(EnumSet.allOf(AccumulateHoldingType.class));
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding leaf = equityHolding("Alpha Corp", "1.0");
    leaf.setType(type);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, leaf));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    assertThat(result.getCommonHoldings()).hasSize(accumulated ? 1 : 0);
  }

  @ParameterizedTest(name = "[{index}] type={0} descend={1}")
  @CsvSource({
      "FO, true",
      "FE, true",
      "FS, true",
      "FX, true",
      "EX, true",
      "E,  false",
      "B,  false",
      "X,  false"
  })
  void shouldDescend_onlyForFundLikeAndExchangeTypes(String childType, boolean shouldDescend) {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding deepEquity = equityHolding("Deep Equity", "1.0");
    CommonHolding middle = equityHolding("Middle", "0.5");
    middle.setType(childType);
    middle.setUnderlyingHoldings(List.of(deepEquity));
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, middle));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    if (shouldDescend) {
      assertThat(result.getCommonHoldings()).extracting(TopCommonHoldingData::getName)
          .containsExactly("Deep Equity");
    } else if ("E".equals(childType)) {
      // Middle node IS an equity and accumulated as a leaf itself.
      assertThat(result.getCommonHoldings()).extracting(TopCommonHoldingData::getName)
          .containsExactly("Middle");
    } else {
      // Non-fund, non-equity → no leaves accumulate (default Set.of(E)).
      assertThat(result.getCommonHoldings()).isEmpty();
    }
  }

  @Test
  void shouldRespectMaxRecursionDepth_stoppingDescentBelowDeepestLeaf() {
    properties.setMaxRecursionDepth(1);
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding deepEquity = equityHolding("Deep Equity", "0.9");
    CommonHolding level1Fund = equityHolding("Level1 Fund", "1.0");
    level1Fund.setType("FO");
    level1Fund.setUnderlyingHoldings(List.of(deepEquity));
    CommonHolding level0Fund = equityHolding("Level0 Fund", "1.0");
    level0Fund.setType("FO");
    level0Fund.setUnderlyingHoldings(List.of(level1Fund));
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, level0Fund));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    // depth=0 descends into level0; depth=1 NOT descended → level1Fund itself is emitted as a leaf.
    // level1Fund type "FO" is not in accumulate set (only "E") so result is empty — proves no deeper descent happened.
    assertThat(result.getCommonHoldings()).isEmpty();
  }

  @Test
  void shouldDetectCycle_whenChildIdentifierMatchesAncestor() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding cycleChild = equityHolding("Cycle Child", "1.0");
    cycleChild.setType("FO");
    cycleChild.setPrimaryIdentifier(new SecurityIdentifier("CYCLE", FiIdentifierType.MORNINGSTAR_ID));
    CommonHolding ancestor = equityHolding("Cycle Ancestor", "1.0");
    ancestor.setType("FO");
    ancestor.setPrimaryIdentifier(new SecurityIdentifier("CYCLE", FiIdentifierType.MORNINGSTAR_ID));
    ancestor.setUnderlyingHoldings(List.of(cycleChild));
    cycleChild.setUnderlyingHoldings(List.of(ancestor));
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, ancestor));

    // No StackOverflowError; cycle guard short-circuits at the repeated identity.
    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    assertThat(result.getCommonHoldings()).isEmpty();
  }

  @Test
  void shouldSortByAllocationDescThenByIdentifier_pickingCanonicalRepresentative() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding alphaA = equityHolding("Alpha", "0.5");
    alphaA.setPrimaryIdentifier(new SecurityIdentifier("Z-LAST", FiIdentifierType.TICKER));
    CommonHolding alphaB = equityHolding("Alpha", "0.5");
    alphaB.setPrimaryIdentifier(new SecurityIdentifier("A-FIRST", FiIdentifierType.TICKER));
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, alphaA, alphaB));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    assertThat(result.getCommonHoldings()).hasSize(1);
    // Lexicographically smallest identifier wins — A-FIRST is the chosen representative.
    assertThat(result.getCommonHoldings().get(0).getIdentifier().getId()).isEqualTo("A-FIRST");
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("missingNameAndCompanyNameCases")
  void shouldSkipChildren_withoutNameOrCompanyName(String desc, CommonHolding child) {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, child,
        equityHolding("Valid Equity", "0.5")));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    assertThat(result.getCommonHoldings()).extracting(TopCommonHoldingData::getName).containsExactly("Valid Equity");
  }

  private static Stream<Arguments> missingNameAndCompanyNameCases() {
    CommonHolding nullNameAndCompany = new CommonHolding();
    nullNameAndCompany.setType("E");
    nullNameAndCompany.setWeight(new BigDecimal("0.5"));

    CommonHolding emptyNameAndCompany = new CommonHolding();
    emptyNameAndCompany.setName("");
    emptyNameAndCompany.setCompanyName("");
    emptyNameAndCompany.setType("E");
    emptyNameAndCompany.setWeight(new BigDecimal("0.5"));

    return Stream.of(
        Arguments.of("null name and companyName", nullNameAndCompany),
        Arguments.of("empty name and companyName", emptyNameAndCompany));
  }

  @ParameterizedTest(name = "[{index}] numOfTop={0}, expectedSize={1}")
  @CsvSource({"1, 1", "2, 2", "10, 3"})
  void shouldLimitTopCommonHoldings_byNumOfTop(int numOfTop, int expectedSize) {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD,
        equityHolding("Alpha", "0.5"),
        equityHolding("Bravo", "0.3"),
        equityHolding("Charlie", "0.2")));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), numOfTop), sms);

    assertThat(result.getCommonHoldings()).hasSize(expectedSize);
  }

  @ParameterizedTest
  @ValueSource(strings = {"CASH", "GIC"})
  void shouldNotRequireSmsCurrency_forLocallySourcedHoldingTypes(String typeName) {
    // The fetcher won't return these (sent-to-SMS predicate excludes them), so currency must come from elsewhere.
    // Currently the service still calls currencyFor() which falls into the default branch and returns null —
    // and since these are NOT sent to SMS, the missing-currency throw is skipped. The conversion just omits them.
    PortfolioHolding nonSmsHolding = portfolioHolding("X", 1000, FinancialInstrumentType.valueOf(typeName));
    PortfolioHolding etf = etfHolding("AAA", 1000);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(etf,
        topHoldings(Currency.CAD, equityHolding("Alpha", "1.0")));

    // Should not throw.
    TopCommonHoldingsResult result = service.perform(command(List.of(etf, nonSmsHolding), 10), sms);

    assertThat(result.getCommonHoldings()).hasSize(1);
  }

  private void assertCalculationFails(TopCommonHoldingsCommand command,
      Map<PortfolioHolding, CommonTopHoldings> securityData, String expectedErrorCode) {
    assertThatThrownBy(() -> service.perform(command, securityData))
        .isInstanceOf(CalculationException.class)
        .satisfies(ex -> assertThat(((CalculationException) ex).getErrorCode().name()).isEqualTo(expectedErrorCode));
  }

  private TopCommonHoldingsCommand command(List<PortfolioHolding> holdings, int numOfTop) {
    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(holdings);
    command.setNumOfTopCommonHoldings(numOfTop);
    return command;
  }

  private static PortfolioHolding etfHolding(String id, int value) {
    return portfolioHolding(id, value, FinancialInstrumentType.ETF_CANADA);
  }

  private static PortfolioHolding portfolioHolding(String id, int value, FinancialInstrumentType type) {
    return new PortfolioHolding(
        BigDecimal.valueOf(value),
        type,
        new SecurityIdentifier(id, FiIdentifierType.TICKER));
  }

  private static CommonTopHoldings topHoldings(Currency currency, CommonHolding... children) {
    return CommonTopHoldings.builder()
        .currency(currency)
        .holdings(List.of(children))
        .providers(List.of())
        .build();
  }

  private static CommonHolding equityHolding(String name, String weight) {
    CommonHolding h = new CommonHolding();
    h.setName(name);
    h.setCompanyName(name);
    h.setType("E");
    h.setWeight(new BigDecimal(weight));
    return h;
  }
}
