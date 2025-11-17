package com.fintex.ce.service.impl.calculation;

import com.fintex.smclient.graphql.PaymentFrequencyType;
import com.fintex.ce.config.constant.GeneralConstants;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.config.enumeration.InterestFreq;
import com.fintex.ce.dto.IncomeDto;
import com.fintex.ce.dto.IncomeForecastDto;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.IncomeForecastReqDTO;
import com.fintex.ce.dto.response.IncomeForecastResDto;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RIncomeForecast;
import com.fintex.ce.service.impl.cache.IncomeForecastCacheStorage;
import com.fintex.ce.service.interfaces.calculation.IncomeForecastService;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.util.PortfolioUtils;
import com.fintex.ce.util.validation.request.IncomeForecastReqValidation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class IncomeForecastCalculationServiceImpl implements IncomeForecastService {

    private static final int ONE_MONTH = 1;
    private static final int TWO_SYMBOLS = 2;
    private static final int TWELVE_MONTH = 12;
    private static final int ONE_HUNDRED = 100;
    private static final Set<InterestFreq> MONTHLY_FREQUENCY = Set.of(InterestFreq.DAILY, InterestFreq.WEEKLY, InterestFreq.BI_WEEKLY);

    private final IncomeForecastCacheStorage incomeForecastCacheStorage;
    private final IncomeForecastReqValidation requestValidator;

    public IncomeForecastCalculationServiceImpl(final IncomeForecastCacheStorage incomeForecastCacheStorage,
                                                final IncomeForecastReqValidation requestValidator) {
        this.incomeForecastCacheStorage = incomeForecastCacheStorage;
        this.requestValidator = requestValidator;
    }

    @Override
    public IncomeForecastResDto perform(final IncomeForecastReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final ArrayList<Warning> warnings = new ArrayList<>();
        final Map<Holding, RIncomeForecast> incomeForecastDto = incomeForecastCacheStorage.load(
                reqDTO.getHoldings(), List.of(), warnings, new ParamHolderDTO());

        final Integer period = Optional.ofNullable(reqDTO.getTimeIntervalPeriods()).orElse(TWELVE_MONTH);
        final IncomeForecastResDto incomeForecastResDto = calculate(incomeForecastDto, period);
        incomeForecastResDto.setWarnings(warnings);
        return incomeForecastResDto;
    }

    private IncomeForecastResDto calculate(final Map<Holding, RIncomeForecast> holdingRIncomeForecastMap,
                                           final Integer terms) {

        final List<IncomeForecastDto> incomeForecasts = holdingRIncomeForecastMap.entrySet()
                .stream()
                .map(entry -> getIncomeForecastDto(terms, entry))
                .filter(dto -> !CollectionUtils.isEmpty(dto.getIncome()))
                .toList();

        final IncomeForecastResDto incomeForecastResDto = new IncomeForecastResDto();
        incomeForecastResDto.setIncomeForecast(incomeForecasts);
        return incomeForecastResDto;

    }

    private IncomeForecastDto getIncomeForecastDto(final Integer terms,
                                                   final Map.Entry<Holding, RIncomeForecast> entry) {
        final Holding holding = entry.getKey();
        return Objects.equals(holding.getType(), HoldingType.GIC) ?
                getGicIncomeForecast(holding) :
                getIncomeForecast(terms, entry.getValue(), holding);
    }

    private IncomeForecastDto getIncomeForecast(final Integer terms,
                                                final RIncomeForecast rIncomeForecast,
                                                final Holding holding) {
        final IncomeForecastDto incomeForecastDto = getIncomeForecastDto(holding);

        if (isFixedIncomeAtMaturityType(rIncomeForecast, holding)) {
            final LocalDate maturityDate = LocalDate.parse(rIncomeForecast.getMaturityDate());
            final BigDecimal income = calculateAtMaturityIncome(holding.getValue(), rIncomeForecast);
            incomeForecastDto.setIncome(List.of(
                    getIncomeDto(maturityDate, income)
            ));
        } else if (ObjectUtils.allNotNull(rIncomeForecast.getSchedule(), rIncomeForecast.getDividendYield())) {
            final List<IncomeDto> incomes = calculateIncome(
                    rIncomeForecast.getDividendYield(),
                    rIncomeForecast.getSchedule(),
                    holding.getValue(),
                    terms,
                    Calendar.getInstance());

            incomeForecastDto.setIncome(incomes);
        }

        return incomeForecastDto;
    }

    private IncomeDto getIncomeDto(final LocalDate date,
                                   final BigDecimal amount) {
        final String formattedDate = date.format(DateTimeFormatter.ofPattern(GeneralConstants.YEAR_MONTH_DATE_FORMAT));
        return new IncomeDto(formattedDate, DecimalUtils.toUserScale(amount));
    }

    private BigDecimal calculateAtMaturityIncome(final BigDecimal amount,
                                                 final RIncomeForecast rIncomeForecast) {
        final LocalDate maturityDate = LocalDate.parse(rIncomeForecast.getMaturityDate());
        final LocalDate issueDate = LocalDate.parse(rIncomeForecast.getIssueDate());
        final Period period = Period.between(issueDate, maturityDate);
        final int monthsBetween = (period.getYears() * TWELVE_MONTH) + period.getMonths();
        final BigDecimal income = DecimalUtils.divide(amount.multiply(rIncomeForecast.getDividendYield()), new BigDecimal(TWELVE_MONTH))
                .multiply(new BigDecimal(monthsBetween));
        return DecimalUtils.toUserScale(income);
    }

    private boolean isFixedIncomeAtMaturityType(final RIncomeForecast rIncomeForecast,
                                                final Holding holding) {
        return Objects.equals(holding.getType(), HoldingType.FIXED_INCOME) &&
                Objects.isNull(rIncomeForecast.getSchedule()) &&
                ObjectUtils.allNotNull(rIncomeForecast.getDividendYield(), rIncomeForecast.getPaymentFrequencyType(),
                        rIncomeForecast.getMaturityDate(), rIncomeForecast.getIssueDate()) &&
                Objects.equals(PaymentFrequencyType.AT_MATURITY, rIncomeForecast.getPaymentFrequencyType());
    }

    private static IncomeForecastDto getIncomeForecastDto(final Holding holding) {
        final String holdingType = holding.getType().name();
        final String holdingIdentifier = holding.getHoldingIdentifier().name();

        final IncomeForecastDto incomeForecastDto = IncomeForecastDto.builder()
                .type(holdingType)
                .holdingIdentifier(holdingIdentifier)
                .build();

        PortfolioUtils.setHoldingResponseDetails(holding, incomeForecastDto);
        return incomeForecastDto;
    }

    private IncomeForecastDto getGicIncomeForecast(final Holding holding) {
        final GicHolding gicHolding = (GicHolding) holding;
        final LocalDate investmentDate = gicHolding.getInvestmentDate();
        final List<IncomeDto> incomeDtos = getGicIncomeDtos(investmentDate, gicHolding);

        return IncomeForecastDto.builder()
                .type(gicHolding.getType().toString())
                .holdingIdentifier(gicHolding.getName())
                .income(incomeDtos)
                .build();
    }

    private List<IncomeDto> getGicIncomeDtos(final LocalDate investmentDate,
                                             final GicHolding gicHolding) {
        final Calendar investmentDateCalendar = getInvestmentDateCalendar(investmentDate);
        final BigDecimal annualInterestRate = DecimalUtils.divide(gicHolding.getClientIntRate(), new BigDecimal(ONE_HUNDRED));
        final InterestFreq interestFreq = getFrequency(gicHolding.getInterestFreq());

        final List<String> distributionDates = getDistributionDates(interestFreq, investmentDate);

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
        final Calendar investmentDateCalendar = Calendar.getInstance();
        investmentDateCalendar.clear();
        investmentDateCalendar.set(investmentDate.getYear(), investmentDate.getMonthValue() - 1, investmentDate.getDayOfMonth());
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
        final LocalDate paymentDate = couponFirstDate
                .plusMonths(monthsToAdd)
                .with(TemporalAdjusters.lastDayOfMonth());

        return MonthDay.of(
                paymentDate.getMonth(),
                paymentDate.getDayOfMonth());
    }

    public List<IncomeDto> calculateIncome(final BigDecimal dividendYield,
                                           final List<String> payoutDates,
                                           final BigDecimal amount,
                                           final int terms,
                                           final Calendar startingDate) {
        final Map<Integer, BigDecimal> monthPayouts = getMonthPayouts(dividendYield, payoutDates, amount);

        getNextMonth(startingDate);

        return IntStream.range(0, terms)
                .mapToObj(v -> getPayoutSchedule(startingDate, monthPayouts))
                .filter(Objects::nonNull)
                .filter(dto -> !(YearMonth.parse(dto.getDate(), DateTimeFormatter.ofPattern(GeneralConstants.YEAR_MONTH_DATE_FORMAT)).atEndOfMonth()).isBefore(LocalDate.now()))
                .toList();
    }

    private IncomeDto getPayoutSchedule(final Calendar calendar,
                                        final Map<Integer, BigDecimal> monthPayouts) {
        final int month = calendar.get(Calendar.MONTH) + ONE_MONTH;
        final int year = calendar.get(Calendar.YEAR);
        final String formattedDate = year + GeneralConstants.DELIMITER + String.format("%02d", month);
        final BigDecimal payout = monthPayouts.get(month);

        final IncomeDto incomeDto = Optional.ofNullable(payout)
                .map(value -> new IncomeDto(formattedDate, DecimalUtils.toUserScale(value)))
                .orElse(null);

        getNextMonth(calendar);
        return incomeDto;
    }

    private void getNextMonth(final Calendar calendar) {
        calendar.add(Calendar.MONTH, ONE_MONTH);
    }

    private Map<Integer, BigDecimal> getMonthPayouts(final BigDecimal dividendYield,
                                                     final List<String> payoutDates,
                                                     final BigDecimal amount) {

        final BigDecimal payoutAmount = !payoutDates.isEmpty() ?
                calculatePayoutAmount(dividendYield, payoutDates, amount) :
                BigDecimal.ZERO;

        return payoutDates.stream()
                .collect(Collectors.toMap(
                        this::getMonthFromShortDate,
                        v -> payoutAmount
                ));
    }

    private static BigDecimal calculatePayoutAmount(final BigDecimal dividendYield,
                                                    final List<String> payoutDates,
                                                    final BigDecimal amount) {
        return DecimalUtils.toUserScale(
                DecimalUtils.divide(
                        amount.multiply(dividendYield),
                        new BigDecimal(payoutDates.size())
                ));
    }

    private int getMonthFromShortDate(final String shortDate) {
        return Integer.parseInt(shortDate.split(GeneralConstants.DELIMITER)[0].trim());
    }

}
