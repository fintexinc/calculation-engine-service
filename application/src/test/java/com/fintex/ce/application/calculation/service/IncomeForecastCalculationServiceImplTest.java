package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.calculation.distribution.Income;
import com.fintex.ce.model.domain.calculation.yield.IncomeForecast;
import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.income.IncomeForecastResult;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

class IncomeForecastCalculationServiceImplTest {

  @Mock
  private SecurityDataFetcher incomeForecastFetcher;

  @InjectMocks
  private IncomeForecastCalculationServiceImpl sut;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void shouldTestPerform_whenConditionIsMet() {
    IncomeForecastCommand reqDTO = new IncomeForecastCommand(); // Initialize with some mock data if needed
    when(incomeForecastFetcher.fetch(any(), any())).thenReturn(new HashMap<>());
    IncomeForecastResult response = sut.perform(reqDTO);
    assertNotNull(response);
  }
  @Test
  void shouldTestCalculate_whenIncome() {
    BigDecimal dividendYield = new BigDecimal("0.05");
    List<String> dates = List.of("1-30", "3-15", "6-20", "10-12");
    BigDecimal amount = BigDecimal.TEN;
    int terms = 12;
    List<Income> incomes = sut.calculateIncome(dividendYield, dates, amount, terms, Calendar.getInstance());
    assertEquals(4, incomes.size());
  }

  @Test
  void shouldTestCalculate_whenIncomeWithNoDividendDates() {
    BigDecimal dividendYield = new BigDecimal("0.05");
    List<String> dates = List.of(); // Empty array
    BigDecimal amount = BigDecimal.TEN;
    int terms = 12;
    List<Income> incomes = sut.calculateIncome(dividendYield, dates, amount, terms, Calendar.getInstance());
    assertTrue(incomes.stream().allMatch(income -> income.getAmount().equals(BigDecimal.ZERO)));
  }

  @Test
  void shouldPerformFundSeries_whenVerify() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final PortfolioHolding fundSeriesHolding = Mockito.mock(PortfolioHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal dividendYield = new BigDecimal("0.5");
    final List<String> schedule = List.of("1-30", "3-15", "6-20", "10-12");
    final BigDecimal holdingValue = new BigDecimal(1000);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(fundSeriesHolding, incomeForecast));
    Mockito.when(fundSeriesHolding.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    Mockito.when(fundSeriesHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(incomeForecastReqDTO.getTimeIntervalPeriods()).thenReturn(12);
    Mockito.when(fundSeriesHolding.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("FUNDSERV_CODE",
        FiIdentifierType.FUNDSERV));
    Mockito.when(incomeForecast.getDividendYield()).thenReturn(dividendYield);
    Mockito.when(incomeForecast.getSchedule()).thenReturn(schedule);

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(result.getIncomeForecast().get(0).getType(), FinancialInstrumentType.MUTUAL_FUND_CANADA
        .name());
    Assertions.assertEquals(result.getIncomeForecast().get(0).getHoldingIdentifier(), FiIdentifierType.FUNDSERV
        .name());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(4, income.size());

    final List<String> month = income.stream()
        .map(v -> v.getDate().split("-")[1])
        .toList();
    Assertions.assertEquals(4, month.size());
    Assertions.assertTrue(month.contains("01"));
    Assertions.assertTrue(month.contains("03"));
    Assertions.assertTrue(month.contains("06"));
    Assertions.assertTrue(month.contains("10"));

    final List<BigDecimal> values = income.stream()
        .map(Income::getAmount)
        .toList();

    Assertions.assertEquals(4, values.size());
    Assertions.assertTrue(values.contains(DecimalUtils.toUserScale(new BigDecimal("125"))));
  }

  @Test
  void shouldPerformFundSeries_whenVerify2() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final PortfolioHolding fundSeriesHolding = Mockito.mock(PortfolioHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal dividendYield = new BigDecimal("0.5");
    final List<String> schedule = List.of("6-30", "12-30");
    final BigDecimal holdingValue = new BigDecimal(1000);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(fundSeriesHolding, incomeForecast));
    Mockito.when(fundSeriesHolding.getHoldingType()).thenReturn(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    Mockito.when(fundSeriesHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(incomeForecastReqDTO.getTimeIntervalPeriods()).thenReturn(12);
    Mockito.when(fundSeriesHolding.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("FUNDSERV_CODE",
        FiIdentifierType.FUNDSERV));
    Mockito.when(incomeForecast.getDividendYield()).thenReturn(dividendYield);
    Mockito.when(incomeForecast.getSchedule()).thenReturn(schedule);

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(FinancialInstrumentType.MUTUAL_FUND_CANADA.name(), result.getIncomeForecast().get(0)
        .getType());
    Assertions.assertEquals(FiIdentifierType.FUNDSERV.name(), result.getIncomeForecast().get(0)
        .getHoldingIdentifier());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(2, income.size());

    final List<String> month = income.stream()
        .map(v -> v.getDate().split("-")[1])
        .toList();
    Assertions.assertEquals(2, month.size());
    Assertions.assertTrue(month.contains("06"));
    Assertions.assertTrue(month.contains("12"));

    final List<BigDecimal> values = income.stream()
        .map(Income::getAmount)
        .toList();

    Assertions.assertEquals(2, values.size());
    Assertions.assertTrue(values.contains(DecimalUtils.toUserScale(new BigDecimal("250"))));
  }

  @Test
  void shouldPerformFixedIncome_whenVerify() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final PortfolioHolding fixedIncomeHolding = Mockito.mock(PortfolioHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal dividendYield = new BigDecimal("0.5");
    final List<String> schedule = List.of("6-30", "12-30");
    final BigDecimal holdingValue = new BigDecimal(1000);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(fixedIncomeHolding, incomeForecast));
    Mockito.when(fixedIncomeHolding.getHoldingType()).thenReturn(FinancialInstrumentType.FIXED_INCOME);
    Mockito.when(fixedIncomeHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(incomeForecastReqDTO.getTimeIntervalPeriods()).thenReturn(12);
    Mockito.when(fixedIncomeHolding.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("CUSIP_ID",
        FiIdentifierType.CUSIP));
    Mockito.when(incomeForecast.getDividendYield()).thenReturn(dividendYield);
    Mockito.when(incomeForecast.getSchedule()).thenReturn(schedule);

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(FinancialInstrumentType.FIXED_INCOME.name(), result.getIncomeForecast().get(0).getType());
    Assertions.assertEquals(FiIdentifierType.CUSIP.name(), result.getIncomeForecast().get(0)
        .getHoldingIdentifier());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();

    Assertions.assertEquals(2, income.size());

    final List<String> month = income.stream()
        .map(v -> v.getDate().split("-")[1])
        .toList();
    Assertions.assertEquals(2, month.size());
    Assertions.assertTrue(month.contains("06"));
    Assertions.assertTrue(month.contains("12"));

    final List<BigDecimal> values = income.stream()
        .map(Income::getAmount)
        .toList();

    Assertions.assertEquals(2, values.size());
    Assertions.assertTrue(values.contains(DecimalUtils.toUserScale(new BigDecimal("250"))));
  }

  @Test
  void shouldPerformFixedIncomeAtMaturity_whenVerify() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final PortfolioHolding fixedIncomeHolding = Mockito.mock(PortfolioHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal dividendYield = new BigDecimal("0.5");
    final BigDecimal holdingValue = new BigDecimal(1000);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(fixedIncomeHolding, incomeForecast));
    Mockito.when(fixedIncomeHolding.getHoldingType()).thenReturn(FinancialInstrumentType.FIXED_INCOME);
    Mockito.when(fixedIncomeHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(incomeForecastReqDTO.getTimeIntervalPeriods()).thenReturn(12);
    Mockito.when(fixedIncomeHolding.getSecurityIdentifier()).thenReturn(new SecurityIdentifier("CUSIP_ID",
        FiIdentifierType.CUSIP));
    Mockito.when(incomeForecast.getSchedule()).thenReturn(null);
    Mockito.when(incomeForecast.getDividendYield()).thenReturn(dividendYield);
    Mockito.when(incomeForecast.getIssueDate()).thenReturn("2023-01-01");
    Mockito.when(incomeForecast.getMaturityDate()).thenReturn("2025-12-30");
    Mockito.when(incomeForecast.getPaymentFrequencyType()).thenReturn("AT_MATURITY");

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(FinancialInstrumentType.FIXED_INCOME.name(), result.getIncomeForecast().get(0).getType());
    Assertions.assertEquals(FiIdentifierType.CUSIP.name(), result.getIncomeForecast().get(0)
        .getHoldingIdentifier());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(1, income.size());
    Assertions.assertEquals("2025-12", income.get(0).getDate());
    Assertions.assertEquals(income.get(0).getAmount(), DecimalUtils.toUserScale(new BigDecimal("1458.3333333333")));
  }

  @Test
  void shouldPerformGic_whenVerify() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate currentDate = LocalDate.now();

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(incomeForecastReqDTO.getTimeIntervalPeriods()).thenReturn(12);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(currentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(36));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.ANNUAL);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(result.getIncomeForecast().get(0).getType(), FinancialInstrumentType.GIC.name());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(3, income.size());
    Assertions.assertEquals(format(currentDate.plusYears(1)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("50")), income.get(0).getAmount());
    Assertions.assertEquals(format(currentDate.plusYears(2)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("50")), income.get(1).getAmount());
    Assertions.assertEquals(format(currentDate.plusYears(3)), income.get(2).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("50")), income.get(2).getAmount());
  }

  private String format(final LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
  }

  @Test
  void shouldPerformGic_whenVerify2() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate investmentDate = LocalDate.now().minusMonths(1);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(incomeForecastReqDTO.getTimeIntervalPeriods()).thenReturn(12);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(investmentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(12));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.SEMI_ANNUAL);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(result.getIncomeForecast().get(0).getType(), FinancialInstrumentType.GIC.name());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(2, income.size());
    Assertions.assertEquals(format(investmentDate.plusMonths(6)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("25")), income.get(0).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(12)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("25")), income.get(1).getAmount());
  }

  @Test
  void shouldPerformGic_whenVerify3() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate investmentDate = LocalDate.now().minusMonths(2);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(investmentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(5));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.MONTHLY);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(result.getIncomeForecast().get(0).getType(), FinancialInstrumentType.GIC.name());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(4, income.size());
    Assertions.assertEquals(format(investmentDate.plusMonths(2)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(0).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(3)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(1).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(4)), income.get(2).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(2).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(5)), income.get(3).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(3).getAmount());
  }

  @Test
  void shouldPerformGic_whenVerify4() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate investmentDate = LocalDate.now().minusMonths(2);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(investmentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(5));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.BI_WEEKLY);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(FinancialInstrumentType.GIC.name(), result.getIncomeForecast().get(0).getType());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(4, income.size());
    Assertions.assertEquals(format(investmentDate.plusMonths(2)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(0).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(3)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(1).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(4)), income.get(2).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(2).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(5)), income.get(3).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(3).getAmount());
  }

  @Test
  void shouldPerformGic_whenVerify5() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate investmentDate = LocalDate.now().minusMonths(2);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(investmentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(5));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.DAILY);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(result.getIncomeForecast().get(0).getType(), FinancialInstrumentType.GIC.name());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(4, income.size());
    Assertions.assertEquals(format(investmentDate.plusMonths(2)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(0).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(3)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(1).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(4)), income.get(2).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(2).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(5)), income.get(3).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(3).getAmount());
  }

  @Test
  void shouldPerformGic_whenVerify6() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate investmentDate = LocalDate.now().minusMonths(2);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(investmentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(5));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.BI_WEEKLY);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(FinancialInstrumentType.GIC.name(), result.getIncomeForecast().get(0).getType());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(4, income.size());
    Assertions.assertEquals(format(investmentDate.plusMonths(2)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(0).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(3)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(1).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(4)), income.get(2).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(2).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(5)), income.get(3).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("4.1666666667")), income.get(3).getAmount());
  }

  @Test
  void shouldPerformGic_whenVerify7() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate investmentDate = LocalDate.now().minusMonths(2);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(investmentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(4));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.BI_MONTHLY);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(FinancialInstrumentType.GIC.name(), result.getIncomeForecast().get(0).getType());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(2, income.size());
    Assertions.assertEquals(format(investmentDate.plusMonths(2)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("8.3333333333")), income.get(0).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(4)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("8.3333333333")), income.get(1).getAmount());
  }

  @Test
  void shouldPerformGic_whenVerify8() {
    // SETUP
    final IncomeForecastCommand incomeForecastReqDTO = Mockito.mock(IncomeForecastCommand.class);
    final GicHolding gicHolding = Mockito.mock(GicHolding.class);
    final IncomeForecast incomeForecast = Mockito.mock(IncomeForecast.class);
    final BigDecimal holdingValue = new BigDecimal(1000);
    final LocalDate investmentDate = LocalDate.now().minusMonths(2);

    Mockito.when(incomeForecastFetcher.fetch(Mockito.any(), Mockito.any()))
        .thenReturn(Map.of(gicHolding, incomeForecast));
    Mockito.when(gicHolding.getHoldingType()).thenReturn(FinancialInstrumentType.GIC);
    Mockito.when(gicHolding.getValue()).thenReturn(holdingValue);
    Mockito.when(gicHolding.getInvestmentDate()).thenReturn(investmentDate);
    Mockito.when(gicHolding.getTerm()).thenReturn(new BigDecimal(7));
    Mockito.when(gicHolding.getInterestFreq()).thenReturn(InterestFreq.QUARTERLY);
    Mockito.when(gicHolding.getClientIntRate()).thenReturn(new BigDecimal("5"));

    // ACT
    final IncomeForecastResult result = sut.perform(incomeForecastReqDTO);

    // VERIFY
    Assertions.assertNotNull(result);
    Assertions.assertEquals(1, result.getIncomeForecast().size());
    Assertions.assertEquals(FinancialInstrumentType.GIC.name(), result.getIncomeForecast().get(0).getType());
    final List<Income> income = result.getIncomeForecast().get(0).getIncome();
    Assertions.assertEquals(2, income.size());
    Assertions.assertEquals(format(investmentDate.plusMonths(3)), income.get(0).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("12.5000000000")), income.get(0).getAmount());
    Assertions.assertEquals(format(investmentDate.plusMonths(6)), income.get(1).getDate());
    Assertions.assertEquals(DecimalUtils.toUserScale(new BigDecimal("12.5000000000")), income.get(1).getAmount());

  }

}