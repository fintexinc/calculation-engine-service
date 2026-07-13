package com.fintex.ce.application.calculation.service;

import com.fintex.ce.application.util.DecimalUtils;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.constant.GeneralConstants;
import com.fintex.ce.model.domain.calculation.distribution.Income;
import com.fintex.ce.model.domain.calculation.yield.HoldingIncomeForecast;
import com.fintex.ce.model.domain.calculation.yield.IncomeForecast;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.enumeration.InterestFreq;
import com.fintex.ce.model.domain.holding.GicHolding;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.income.IncomeForecastResult;
import com.fintex.ce.model.dto.command.IncomeForecastCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.util.CollectionUtils;

import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @deprecated metric is broken and not supported for now
 */
@Deprecated
public class IncomeForecastCalculationServiceImpl
    implements
      CalculationService<IncomeForecastCommand, IncomeForecastResult> {

  private static final int ONE_MONTH = 1;
  private static final int TWO_SYMBOLS = 2;
  private static final int TWELVE_MONTH = 12;
  private static final int ONE_HUNDRED = 100;
  private static final String AT_MATURITY_PAYMENT_FREQUENCY = "AT_MATURITY";
  private static final Set<InterestFreq> MONTHLY_FREQUENCY = Set.of(InterestFreq.DAILY, InterestFreq.WEEKLY,
      InterestFreq.BI_WEEKLY);

  private final SecurityDataFetcher<IncomeForecast> incomeForecastSecurityDataFetcher;
  private final Clock clock;

  public IncomeForecastCalculationServiceImpl(
      final SecurityDataFetcher<IncomeForecast> incomeForecastSecurityDataFetcher) {
    this(incomeForecastSecurityDataFetcher, Clock.systemDefaultZone());
  }

  public IncomeForecastCalculationServiceImpl(
      final SecurityDataFetcher<IncomeForecast> incomeForecastSecurityDataFetcher,
      final Clock clock) {
    this.incomeForecastSecurityDataFetcher = incomeForecastSecurityDataFetcher;
    this.clock = clock == null ? Clock.systemDefaultZone() : clock;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.INCOME_FORECAST;
  }

  @Override
  public IncomeForecastResult perform(final IncomeForecastCommand command) {
    ArrayList<Notification> warnings = new ArrayList<>();
    Map<PortfolioHolding, IncomeForecast> holdingIncomeForecast = incomeForecastSecurityDataFetcher.fetch(
        command.getHoldings(), command.getDataProviders());

    Integer period = Optional.ofNullable(command.getTimeIntervalPeriods()).orElse(TWELVE_MONTH);
    IncomeForecastResult result = calculate(holdingIncomeForecast, period);
    result.setWarnings(warnings);
    return result;
  }

  private IncomeForecastResult calculate(final Map<PortfolioHolding, IncomeForecast> holdingIncomeForecastMap,
      final Integer terms) {

    List<HoldingIncomeForecast> incomeForecasts = holdingIncomeForecastMap.entrySet()
        .stream()
        .map(entry -> getHoldingIncomeForecast(terms, entry))
        .filter(dto -> !CollectionUtils.isEmpty(dto.getIncome()))
        .toList();

    return new IncomeForecastResult(incomeForecasts);

  }

  private HoldingIncomeForecast getHoldingIncomeForecast(final Integer terms,
      final Map.Entry<PortfolioHolding, IncomeForecast> entry) {
    PortfolioHolding holding = entry.getKey();
    return Objects.equals(holding.getHoldingType(), FinancialInstrumentType.GIC)
        ? getGicIncomeForecast(holding)
        : getIncomeForecast(terms, entry.getValue(), holding);
  }

  private HoldingIncomeForecast getIncomeForecast(final Integer terms,
      final IncomeForecast rIncomeForecast,
      final PortfolioHolding holding) {
    HoldingIncomeForecast holdingIncomeForecast = getHoldingIncomeForecast(holding);

    if (isFixedIncomeAtMaturityType(rIncomeForecast, holding)) {
      LocalDate maturityDate = LocalDate.parse(rIncomeForecast.getMaturityDate());
      BigDecimal income = calculateAtMaturityIncome(holding.getValue(), rIncomeForecast);
      holdingIncomeForecast.setIncome(List.of(
          getIncome(maturityDate, income)));
    } else if (ObjectUtils.allNotNull(rIncomeForecast.getSchedule(), rIncomeForecast.getDividendYield())) {
      List<Income> incomes = calculateIncome(
          rIncomeForecast.getDividendYield(),
          rIncomeForecast.getSchedule(),
          holding.getValue(),
          terms,
          currentCalendar());

      holdingIncomeForecast.setIncome(incomes);
    }

    return holdingIncomeForecast;
  }

  private Income getIncome(final LocalDate date,
      final BigDecimal amount) {
    String formattedDate = date.format(DateTimeFormatter.ofPattern(GeneralConstants.YEAR_MONTH_DATE_FORMAT));
    return new Income(formattedDate, DecimalUtils.toUserScale(amount));
  }

  private BigDecimal calculateAtMaturityIncome(final BigDecimal amount,
      final IncomeForecast rIncomeForecast) {
    LocalDate maturityDate = LocalDate.parse(rIncomeForecast.getMaturityDate());
    LocalDate issueDate = LocalDate.parse(rIncomeForecast.getIssueDate());
    Period period = Period.between(issueDate, maturityDate);
    int monthsBetween = (period.getYears() * TWELVE_MONTH) + period.getMonths();
    BigDecimal income = DecimalUtils.divide(amount.multiply(rIncomeForecast.getDividendYield()), new BigDecimal(
        TWELVE_MONTH))
        .multiply(new BigDecimal(monthsBetween));
    return DecimalUtils.toUserScale(income);
  }

  private boolean isFixedIncomeAtMaturityType(final IncomeForecast rIncomeForecast,
      final PortfolioHolding holding) {
    return Objects.equals(holding.getHoldingType(), FinancialInstrumentType.FIXED_INCOME) &&
        Objects.isNull(rIncomeForecast.getSchedule()) &&
        ObjectUtils.allNotNull(rIncomeForecast.getDividendYield(), rIncomeForecast.getPaymentFrequencyType(),
            rIncomeForecast.getMaturityDate(), rIncomeForecast.getIssueDate()) &&
        Objects.equals(AT_MATURITY_PAYMENT_FREQUENCY, rIncomeForecast.getPaymentFrequencyType());
  }

  private static HoldingIncomeForecast getHoldingIncomeForecast(final PortfolioHolding holding) {
    String holdingType = holding.getHoldingType().name();
    String securityIdentifierType = holding.getSecurityIdentifier().getIdType().name();

    HoldingIncomeForecast holdingIncomeForecast = HoldingIncomeForecast.builder()
        .type(holdingType)
        .holdingIdentifier(securityIdentifierType)
        .build();

    PortfolioUtils.setHoldingResponseDetails(holding, holdingIncomeForecast);
    return holdingIncomeForecast;
  }

  private HoldingIncomeForecast getGicIncomeForecast(final PortfolioHolding holding) {
    GicHolding gicHolding = (GicHolding) holding;
    LocalDate investmentDate = gicHolding.getInvestmentDate();
    List<Income> incomes = getGicIncomes(investmentDate, gicHolding);

    return HoldingIncomeForecast.builder()
        .type(gicHolding.getHoldingType().toString())
        .holdingIdentifier(gicHolding.getName())
        .income(incomes)
        .build();
  }

  private List<Income> getGicIncomes(final LocalDate investmentDate,
      final GicHolding gicHolding) {
    Calendar investmentDateCalendar = getInvestmentDateCalendar(investmentDate);
    BigDecimal annualInterestRate = DecimalUtils.divide(gicHolding.getClientIntRate(), new BigDecimal(
        ONE_HUNDRED));
    InterestFreq interestFreq = getFrequency(gicHolding.getInterestFreq());

    List<String> distributionDates = getDistributionDates(interestFreq, investmentDate);

    return calculateIncome(
        annualInterestRate,
        distributionDates,
        gicHolding.getValue(),
        gicHolding.getTerm().intValue(),
        investmentDateCalendar);
  }

  private InterestFreq getFrequency(final InterestFreq frequencyType) {
    if (MONTHLY_FREQUENCY.contains(frequencyType)) {
      return InterestFreq.MONTHLY;
    }
    return frequencyType;
  }

  private static Calendar getInvestmentDateCalendar(LocalDate investmentDate) {
    Calendar investmentDateCalendar = Calendar.getInstance();
    investmentDateCalendar.clear();
    investmentDateCalendar.set(investmentDate.getYear(), investmentDate.getMonthValue() - 1, investmentDate
        .getDayOfMonth());
    return investmentDateCalendar;
  }

  private List<String> getDistributionDates(final InterestFreq interestFreq,
      final LocalDate couponFirstDate) {
    return IntStream.rangeClosed(1, interestFreq.getFrequency().intValue())
        .map(index -> Math.multiplyExact(index, interestFreq.getMonthsInPeriod().intValue()))
        .mapToObj(daysToAdd -> getDate(daysToAdd, couponFirstDate))
        .sorted(MonthDay::compareTo)
        .map(MonthDay::toString)
        .map(date -> date.substring(TWO_SYMBOLS))
        .toList();
  }

  private static MonthDay getDate(final int monthsToAdd,
      final LocalDate couponFirstDate) {
    LocalDate paymentDate = couponFirstDate
        .plusMonths(monthsToAdd)
        .with(TemporalAdjusters.lastDayOfMonth());

    return MonthDay.of(
        paymentDate.getMonth(),
        paymentDate.getDayOfMonth());
  }

  public List<Income> calculateIncome(final BigDecimal dividendYield,
      final List<String> payoutDates,
      final BigDecimal amount,
      final int terms,
      final Calendar startingDate) {
    Map<Integer, BigDecimal> monthPayouts = getMonthPayouts(dividendYield, payoutDates, amount);

    getNextMonth(startingDate);

    return IntStream.range(0, terms)
        .mapToObj(v -> getPayoutSchedule(startingDate, monthPayouts))
        .filter(Objects::nonNull)
        .filter(dto -> !(YearMonth.parse(dto.getDate(), DateTimeFormatter.ofPattern(
            GeneralConstants.YEAR_MONTH_DATE_FORMAT)).atEndOfMonth()).isBefore(LocalDate.now(clock)))
        .toList();
  }

  private Calendar currentCalendar() {
    return GregorianCalendar.from(ZonedDateTime.now(clock));
  }

  private Income getPayoutSchedule(final Calendar calendar,
      final Map<Integer, BigDecimal> monthPayouts) {
    int month = calendar.get(Calendar.MONTH) + ONE_MONTH;
    int year = calendar.get(Calendar.YEAR);
    String formattedDate = year + GeneralConstants.DELIMITER + String.format("%02d", month);
    BigDecimal payout = monthPayouts.get(month);

    Income income = Optional.ofNullable(payout)
        .map(value -> new Income(formattedDate, DecimalUtils.toUserScale(value)))
        .orElse(null);

    getNextMonth(calendar);
    return income;
  }

  private void getNextMonth(final Calendar calendar) {
    calendar.add(Calendar.MONTH, ONE_MONTH);
  }

  private Map<Integer, BigDecimal> getMonthPayouts(final BigDecimal dividendYield,
      final List<String> payoutDates,
      final BigDecimal amount) {

    BigDecimal payoutAmount = !payoutDates.isEmpty()
        ? calculatePayoutAmount(dividendYield, payoutDates, amount)
        : BigDecimal.ZERO;

    return payoutDates.stream()
        .collect(Collectors.toMap(
            this::getMonthFromShortDate,
            v -> payoutAmount));
  }

  private static BigDecimal calculatePayoutAmount(final BigDecimal dividendYield,
      final List<String> payoutDates,
      final BigDecimal amount) {
    return DecimalUtils.toUserScale(
        DecimalUtils.divide(
            amount.multiply(dividendYield),
            new BigDecimal(payoutDates.size())));
  }

  private int getMonthFromShortDate(final String shortDate) {
    return Integer.parseInt(shortDate.split(GeneralConstants.DELIMITER)[0].trim());
  }

}
