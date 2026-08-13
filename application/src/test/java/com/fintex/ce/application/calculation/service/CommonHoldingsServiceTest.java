package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.config.TopHoldingsProperties;
import com.fintex.ce.model.domain.calculation.holding.CommonHolding;
import com.fintex.ce.model.domain.calculation.holding.CommonTopHoldings;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingData;
import com.fintex.ce.model.domain.result.holding.TopCommonHoldingsResult;
import com.fintex.ce.model.dto.command.TopCommonHoldingsCommand;
import com.fintex.ce.model.error.ErrorCode;
import com.fintex.ce.model.error.ErrorParams;
import com.fintex.ce.model.error.exceptions.CalculationException;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.holding.HoldingType;
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
    properties.setAccumulateTypes(EnumSet.of(HoldingType.E));
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

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("fundOrEtfWithoutUnderlyingHoldings")
  void shouldThrowTch001_whenDescendedFundOrEtfHasNoUnderlyingHoldings(String description,
      CommonHolding fundOrEtf) {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, fundOrEtf));
    TopCommonHoldingsCommand command = command(List.of(parent), 10);

    assertMissingUnderlyingHoldings(command, sms, parent);
  }

  private static Stream<Arguments> fundOrEtfWithoutUnderlyingHoldings() {
    CommonHolding fund = equityHolding("Underlying Fund", "1.0");
    fund.setType(HoldingType.FO);

    CommonHolding etf = equityHolding("Underlying ETF", "1.0");
    etf.setType(HoldingType.EX);
    etf.setUnderlyingHoldings(List.of());

    CommonHolding namelessFund = new CommonHolding();
    namelessFund.setType(HoldingType.FO);
    namelessFund.setWeight(BigDecimal.ONE);

    return Stream.of(
        Arguments.of("fund has null holdings", fund),
        Arguments.of("ETF has empty holdings", etf),
        Arguments.of("nameless fund has null holdings", namelessFund));
  }

  @Test
  void shouldValidateEveryNestedFund_whenLeafLimitIsReached() {
    properties.setMaxLeavesPerHolding(1);
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding fund = equityHolding("Underlying Fund", "0.5");
    fund.setType(HoldingType.FO);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD,
        equityHolding("First Equity", "0.5"), fund));
    TopCommonHoldingsCommand command = command(List.of(parent), 10);

    assertMissingUnderlyingHoldings(command, sms, parent);
  }

  @Test
  void shouldTreatNestedEquityAsLeaf_whenItHasNoUnderlyingHoldings() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding equity = equityHolding("Nested Equity", "1.0");
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, equity));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getCommonHoldings()).hasSize(1);
    assertThat(result.getCommonHoldings().getFirst().getName()).isEqualTo("Nested Equity");
    assertThat(result.getCommonHoldings().getFirst().getHoldingType()).isEqualTo(HoldingType.E);
    assertThat(result.getCommonHoldings().getFirst().getAllocation()).isEqualByComparingTo(BigDecimal.ONE);
  }

  @Test
  void shouldTreatFundAsLeaf_whenItIsAtMaxRecursionDepthAndHasNoUnderlyingHoldings() {
    properties.setMaxRecursionDepth(1);
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding level1Fund = equityHolding("Level1 Fund", "1.0");
    level1Fund.setType(HoldingType.FO);
    CommonHolding level0Fund = equityHolding("Level0 Fund", "1.0");
    level0Fund.setType(HoldingType.FO);
    level0Fund.setUnderlyingHoldings(List.of(level1Fund));
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, level0Fund));
    TopCommonHoldingsCommand command = command(List.of(parent), 10);
    command.setAccumulateHoldingTypes(Set.of(HoldingType.FO));

    TopCommonHoldingsResult result = service.perform(command, sms);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getCommonHoldings()).hasSize(1);
    assertThat(result.getCommonHoldings().getFirst().getName()).isEqualTo("Level1 Fund");
    assertThat(result.getCommonHoldings().getFirst().getAllocation()).isEqualByComparingTo(BigDecimal.ONE);
  }

  /**
   * Guards the predicate against the regex it replaced: <code>(FO|FE|FS|EX|[F].*$)</code> made every {@code F*} code a
   * wrapper, so {@code FD} — an {@code F*} code the shared vocabulary does not mark as nesting, and one SM therefore
   * never resolves underlying holdings for — failed the whole portfolio with TCH-001 instead of counting as a leaf.
   */
  @Test
  void shouldTreatFundLikeCodeAsLeaf_whenTheVocabularyDoesNotMarkItAsNesting() {
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding nonNestingFundLikeCode = equityHolding("Non-nesting FD", "0.5");
    nonNestingFundLikeCode.setType(HoldingType.FD);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent,
        topHoldings(Currency.CAD, nonNestingFundLikeCode, equityHolding("Nested Equity", "0.5")));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    assertThat(result.getWarnings()).isEmpty();
    assertThat(result.getCommonHoldings()).hasSize(1);
    assertThat(result.getCommonHoldings().getFirst().getName()).isEqualTo("Nested Equity");
    assertThat(result.getCommonHoldings().getFirst().getAllocation()).isEqualByComparingTo(new BigDecimal("0.5"));
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
  @EnumSource(value = FinancialInstrumentType.class, names = {"STOCK"})
  void shouldShortCircuit_whenRequestHoldingIsAnyStockVariant(FinancialInstrumentType stockType) {
    PortfolioHolding stock = portfolioHolding("STK", 1000, stockType);
    CommonHolding selfEquity = equityHolding("Alpha Corp", "0.42");
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(stock, topHoldings(Currency.CAD, selfEquity));

    TopCommonHoldingsResult result = service.perform(command(List.of(stock), 10), sms);

    // Leaf-stock short-circuit: child weight 0.42 is ignored — inherited weight (100%) is used directly.
    assertThat(result.getCommonHoldings()).hasSize(1);
    assertThat(result.getCommonHoldings().get(0).getAllocation()).isEqualByComparingTo("1.0");
  }

  /**
   * Configured with the same six codes production ships, rather than with every {@link HoldingType}: the point is that
   * the configured subset decides. {@code BG} is agency MBS — a real code the dictionary knows and this subset excludes
   * — and {@code null} is how a code outside the vocabulary arrives, so the two false cases separate "not configured"
   * from "not a holding type".
   */
  @ParameterizedTest(name = "[{index}] type={0} accumulated={1}")
  @CsvSource(nullValues = "null", value = {
      "E,    true",
      "B,    true",
      "BC,   true",
      "ER,   true",
      "BG,   false",
      "null, false"
  })
  void shouldHonourAccumulateTypesFromYaml(HoldingType type, boolean accumulated) {
    properties.setAccumulateTypes(EnumSet.of(HoldingType.E, HoldingType.ER, HoldingType.B, HoldingType.BC,
        HoldingType.BD, HoldingType.BT));
    PortfolioHolding parent = etfHolding("AAA", 1000);
    CommonHolding leaf = equityHolding("Alpha Corp", "1.0");
    leaf.setType(type);
    Map<PortfolioHolding, CommonTopHoldings> sms = Map.of(parent, topHoldings(Currency.CAD, leaf));

    TopCommonHoldingsResult result = service.perform(command(List.of(parent), 10), sms);

    assertThat(result.getCommonHoldings()).hasSize(accumulated ? 1 : 0);
  }

  /**
   * The truth table of the descent predicate, and it is the shared vocabulary's, not the shape of the code: {@code FD}
   * is an {@code F*} code the dictionary knows and flags as non-nesting, {@code null} is how a code outside the
   * dictionary arrives — and the regex this replaced descended into both.
   */
  @ParameterizedTest(name = "[{index}] type={0} descend={1}")
  @CsvSource(nullValues = "null", value = {
      "FC,   true",
      "FE,   true",
      "FH,   true",
      "FM,   true",
      "FO,   true",
      "FS,   true",
      "FV,   true",
      "EX,   true",
      "FD,   false",
      "E,    false",
      "B,    false",
      "null, false"
  })
  void shouldDescend_onlyForTypesTheVocabularyMarksAsNesting(HoldingType childType, boolean shouldDescend) {
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
    } else if (childType == HoldingType.E) {
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
    level1Fund.setType(HoldingType.FO);
    level1Fund.setUnderlyingHoldings(List.of(deepEquity));
    CommonHolding level0Fund = equityHolding("Level0 Fund", "1.0");
    level0Fund.setType(HoldingType.FO);
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
    cycleChild.setType(HoldingType.FO);
    cycleChild.setPrimaryIdentifier(new SecurityIdentifier("CYCLE", FiIdentifierType.MORNINGSTAR_ID));
    CommonHolding ancestor = equityHolding("Cycle Ancestor", "1.0");
    ancestor.setType(HoldingType.FO);
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
    nullNameAndCompany.setType(HoldingType.E);
    nullNameAndCompany.setWeight(new BigDecimal("0.5"));

    CommonHolding emptyNameAndCompany = new CommonHolding();
    emptyNameAndCompany.setName("");
    emptyNameAndCompany.setCompanyName("");
    emptyNameAndCompany.setType(HoldingType.E);
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

  private void assertMissingUnderlyingHoldings(TopCommonHoldingsCommand command,
      Map<PortfolioHolding, CommonTopHoldings> securityData, PortfolioHolding parent) {
    assertThatThrownBy(() -> service.perform(command, securityData))
        .isInstanceOfSatisfying(CalculationException.class, exception -> {
          assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.HOLDING_MISSING_UNDERLYING_HOLDINGS);
          assertThat(exception.getMessage()).isEqualTo(ErrorCode.HOLDING_MISSING_UNDERLYING_HOLDINGS
              .getFormattedMessage());
          assertThat(exception.getId()).isEqualTo(parent.getIdsString());
          assertThat(exception.getMetadata())
              .containsOnlyKeys(ErrorParams.HOLDING_ID)
              .containsEntry(ErrorParams.HOLDING_ID, parent.getIdsString());
        });
  }

  private TopCommonHoldingsCommand command(List<PortfolioHolding> holdings, int numOfTop) {
    TopCommonHoldingsCommand command = new TopCommonHoldingsCommand();
    command.setHoldings(holdings);
    command.setNumOfTopCommonHoldings(numOfTop);
    return command;
  }

  private static PortfolioHolding etfHolding(String id, int value) {
    return portfolioHolding(id, value, FinancialInstrumentType.ETF);
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
    h.setType(HoldingType.E);
    h.setWeight(new BigDecimal(weight));
    return h;
  }
}
