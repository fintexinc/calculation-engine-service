package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.calculation.DistributionData;
import com.fintex.ce.dto.calculation.HoldingForDailyCalculationDTO;
import com.fintex.ce.dto.calculation.NumOfUnitsAndPacValue;
import com.fintex.ce.dto.calculation.NumOfUnitsAndWithdrawalValue;
import com.fintex.ce.dto.calculation.ReinvestPacWithdrawalDTO;
import com.fintex.ce.dto.calculation.ReturnsAnsDistributionReceived;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.RHistoricalDistributions;
import com.fintex.ce.util.CollectorUtils;
import com.fintex.ce.util.DecimalUtils;
import com.fintex.ce.dto.calculation.InvestmentDataDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

import static com.fintex.ce.config.constant.BigDecimalConstants.HUNDRED;
import static com.fintex.ce.util.DateTimeUtils.isLastDayOfMonth;


public class DailyPerformanceCalculation {

    private final Map<Holding, RHistoricalDistributions> distributionData;
    private final Map<Holding, HoldingForDailyCalculationDTO> holdingAndDailyHolding;
    private Map<Holding, TreeMap<LocalDate, BigDecimal>> holdingNavPrices;

    public DailyPerformanceCalculation(Map<Holding, TreeMap<LocalDate, BigDecimal>> holdingNavPrices,
                                       Map<Holding, RHistoricalDistributions> distributionsData,
                                       Map<Holding, HoldingForDailyCalculationDTO> holdingAndDailyHolding) {
        this.holdingNavPrices = holdingNavPrices;
        this.distributionData = distributionsData;
        this.holdingAndDailyHolding = holdingAndDailyHolding;
    }

    public Map<String, ReturnsAnsDistributionReceived> calculate(boolean reinvest, boolean usePacAndWithdrawal) {
        var returns = new LinkedHashMap<String, ReturnsAnsDistributionReceived>();
        holdingNavPrices.forEach((h, navPrices) -> {
            var dailyHolding = holdingAndDailyHolding.get(h);
            var pacAndWithdrawalValues = getPacAndWithdrawalDto(dailyHolding, reinvest, usePacAndWithdrawal);
            returns.put(h.generateUserIdentifier(), calculate(h, navPrices, pacAndWithdrawalValues, dailyHolding.getPurchaseAmount()));
        });

        return returns;
    }

    public Map<Holding, TreeMap<LocalDate, BigDecimal>> calculateForGrowthOf10K() {
        var returns = new LinkedHashMap<Holding, TreeMap<LocalDate, BigDecimal>>();
        holdingNavPrices.forEach((h, navPrices) -> {
            var dailyHolding = holdingAndDailyHolding.get(h);
            var pacAndWithdrawalValues = getPacAndWithdrawalDto(dailyHolding, true, false);
            var dailyReturns = calculate(h, navPrices, pacAndWithdrawalValues, dailyHolding.getPurchaseAmount()).getReturns().entrySet().stream()
                    .filter(e -> isLastDayOfMonth(e.getKey()))
                    .collect(CollectorUtils.toTreeMap());
            returns.put(h, dailyReturns);
        });

        return returns;
    }

    public Map<String, TreeMap<LocalDate, DistributionData>> calculateDistribution(boolean reinvest, boolean usePacAndWithdrawal) {
        var returns = new LinkedHashMap<String, TreeMap<LocalDate, DistributionData>>();
        holdingNavPrices.forEach((h, navPrices) -> {
            var dailyHolding = holdingAndDailyHolding.get(h);
            var pacAndWithdrawalValues = getPacAndWithdrawalDto(dailyHolding, reinvest, usePacAndWithdrawal);
            returns.put(h.generateUserIdentifier(), calculateDistribution(h, navPrices, pacAndWithdrawalValues, dailyHolding.getPurchaseAmount()));
        });

        return returns;
    }

    private ReinvestPacWithdrawalDTO getPacAndWithdrawalDto(HoldingForDailyCalculationDTO h,
                                                            boolean reinvest, boolean usePacAndWithdrawal) {
        if (reinvest && usePacAndWithdrawal) {
            return new ReinvestPacWithdrawalDTO(h.getPac(), h.getPacFrequency(), h.getPacIndex(), h.getWithdrawal(), h.getWithdrawalFrequency(), reinvest); // get values directly from request.
        } else if (reinvest) { // && !usePacAndWithdrawal
            return new ReinvestPacWithdrawalDTO(InvestmentDataDTO.DEFAULT_PAC, InvestmentDataDTO.DEFAULT_PAC_FREQUENCY, BigDecimal.ZERO, InvestmentDataDTO.DEFAULT_WITHDRAWAL, InvestmentDataDTO.DEFAULT_WITHDRAWAL_FREQUENCY, reinvest); // get values directly from request.
        } else if (usePacAndWithdrawal) { // && !reinvest
            return new ReinvestPacWithdrawalDTO(h.getPac(), h.getPacFrequency(), h.getPacIndex(), h.getWithdrawal(), h.getWithdrawalFrequency(), reinvest);
        } else { //!reinvest && !usePacAndWithdrawal
            return new ReinvestPacWithdrawalDTO(InvestmentDataDTO.DEFAULT_PAC, InvestmentDataDTO.DEFAULT_PAC_FREQUENCY, BigDecimal.ZERO, InvestmentDataDTO.DEFAULT_WITHDRAWAL, InvestmentDataDTO.DEFAULT_WITHDRAWAL_FREQUENCY, reinvest);
        }
    }

    private ReturnsAnsDistributionReceived calculate(Holding h, TreeMap<LocalDate, BigDecimal> returns, ReinvestPacWithdrawalDTO rulesDto, BigDecimal purchaseAmount) {
        var distributions = distributionData.get(h).getDistributions();
        var capitalGains = distributionData.get(h).getCapitalGains();
        var navPriceAtStartDate = returns.firstEntry().getValue();
        var startDate = returns.firstEntry().getKey();
        var endDate = returns.lastKey();

        BigDecimal numberOfUnits = DecimalUtils.divide(purchaseAmount, navPriceAtStartDate);

        TreeMap<LocalDate, BigDecimal> result = new TreeMap<>();

        BigDecimal distributionReceived = BigDecimal.ZERO;
        BigDecimal totalContribution = BigDecimal.ZERO;
        BigDecimal totalWithdrawal = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, BigDecimal> entry : returns.entrySet()) {
            LocalDate date = entry.getKey();
            BigDecimal nav = entry.getValue();

            if (!date.isEqual(startDate)) {
                distributionReceived = distributionReceived.add(distributionReceived(distributions, capitalGains, numberOfUnits, date));
                numberOfUnits = addNewUnitsFromDistribution(rulesDto, distributions, capitalGains, numberOfUnits, date, nav);

                var numOfUnitsAndPacValue = addNewUnitsFromContribution(rulesDto, startDate, numberOfUnits, date, nav, endDate);
                numberOfUnits = numOfUnitsAndPacValue.getNumbUnits();
                totalContribution = totalContribution.add(numOfUnitsAndPacValue.getPacValue());

                var numOfUnitsAndWithdrawalValue = substractUnitsBecauseOfWithdrawal(rulesDto, startDate, numberOfUnits, date, nav, endDate);
                numberOfUnits = numOfUnitsAndWithdrawalValue.getNumbUnits();
                totalWithdrawal = totalWithdrawal.add(numOfUnitsAndWithdrawalValue.getWithdrawalValue());
            }

            result.put(date, DecimalUtils.toUserScale(nav.multiply(numberOfUnits)));
        }

        return mapResponseObject(result, distributionReceived, totalContribution, totalWithdrawal);
    }

    private TreeMap<LocalDate, DistributionData> calculateDistribution(Holding h, TreeMap<LocalDate, BigDecimal> returns, ReinvestPacWithdrawalDTO rulesDto, BigDecimal purchaseAmount) {
        var distributions = distributionData.get(h).getDistributions();
        var capitalGains = distributionData.get(h).getCapitalGains();
        var navPriceAtStartDate = returns.firstEntry().getValue();
        var startDate = returns.firstEntry().getKey();
        var endDate = returns.lastKey();

        BigDecimal numberOfUnits = DecimalUtils.divide(purchaseAmount, navPriceAtStartDate);

        TreeMap<LocalDate, DistributionData> result = new TreeMap<>();

        BigDecimal distributionReceived = BigDecimal.ZERO;
        BigDecimal totalContribution = BigDecimal.ZERO;
        BigDecimal totalWithdrawal = BigDecimal.ZERO;

        for (Map.Entry<LocalDate, BigDecimal> entry : returns.entrySet()) {
            final DistributionData distribution = new DistributionData();
            LocalDate date = entry.getKey();
            BigDecimal nav = entry.getValue();

            if (!date.isEqual(startDate)) {
                distributionReceived = distributionReceived.add(distributionReceivedAndPopulateDistributionData(distributions, capitalGains, numberOfUnits, date, distribution));
                numberOfUnits = addNewUnitsFromDistribution(rulesDto, distributions, capitalGains, numberOfUnits, date, nav);

                var numOfUnitsAndPacValue = addNewUnitsFromContribution(rulesDto, startDate, numberOfUnits, date, nav, endDate);
                numberOfUnits = numOfUnitsAndPacValue.getNumbUnits();
                totalContribution = totalContribution.add(numOfUnitsAndPacValue.getPacValue());

                var numOfUnitsAndWithdrawalValue = substractUnitsBecauseOfWithdrawal(rulesDto, startDate, numberOfUnits, date, nav, endDate);
                numberOfUnits = numOfUnitsAndWithdrawalValue.getNumbUnits();
                totalWithdrawal = totalWithdrawal.add(numOfUnitsAndWithdrawalValue.getWithdrawalValue());
            }
            distribution.setFundValue(DecimalUtils.toUserScale(nav.multiply(numberOfUnits)));
            result.put(date, distribution);
        }
        return result;
    }

    private ReturnsAnsDistributionReceived mapResponseObject(TreeMap<LocalDate, BigDecimal> result, BigDecimal distributionReceived, BigDecimal totalContribution, BigDecimal totalWithdrawal) {
        var response = new ReturnsAnsDistributionReceived();
        response.setReturns(result);
        response.setDistributionReceived(distributionReceived);
        response.setTotalContribution(totalContribution);
        response.setTotalWithdrawal(totalWithdrawal);
        response.setSubsequentContribution(totalContribution.subtract(totalWithdrawal));
        return response;
    }

    private NumOfUnitsAndWithdrawalValue substractUnitsBecauseOfWithdrawal(ReinvestPacWithdrawalDTO rulesDto, LocalDate startDate, BigDecimal numberOfUnits, LocalDate date, BigDecimal nav, LocalDate endDate) {
        if (((date.plusDays(1).getMonthValue() - date.getMonthValue()) != 0 || date.equals(endDate))  // verify that it's the end of the month. We add pac to the end date of the month.
                && (date.getMonthValue() - (startDate.getMonthValue() - 1)) % rulesDto.getWithdrawalFrequency().getFrequency() == 0) { // verify that month % getWithdrawalFrequency == 0; Which means that withdrawal has happened and it should be subtracted.
            var withdrawalDividedByNav = DecimalUtils.divide(rulesDto.getWithdrawal(), nav);
            var newNumberOfUnits = numberOfUnits.subtract(withdrawalDividedByNav);
            if (newNumberOfUnits.compareTo(BigDecimal.ZERO) < 0) {
                return new NumOfUnitsAndWithdrawalValue(BigDecimal.ZERO, DecimalUtils.toUserScale(nav.multiply(numberOfUnits)));
            }
            return new NumOfUnitsAndWithdrawalValue(newNumberOfUnits, rulesDto.getWithdrawal());
        }
        return new NumOfUnitsAndWithdrawalValue(numberOfUnits, BigDecimal.ZERO);
    }

    private NumOfUnitsAndPacValue addNewUnitsFromContribution(ReinvestPacWithdrawalDTO rulesDto, LocalDate startDate, BigDecimal numberOfUnits, LocalDate date, BigDecimal nav, LocalDate endDate) {
        if (((date.plusDays(1).getMonthValue() - date.getMonthValue()) != 0 || date.equals(endDate)) // verify that it's the end of the month. We add pac to the end date of the month.
                && (date.getMonthValue() - (startDate.getMonthValue() - 1)) % rulesDto.getPacFreq().getFrequency() == 0) { // verify that month % pacFreq == 0; Which means that pacFreq has happened and it should be added.
            rulesDto.setPac(getPacWithAnnualIndexing(rulesDto, startDate, date));
            var pacDividedByNav = DecimalUtils.divide(rulesDto.getPac(), nav);
            numberOfUnits = numberOfUnits.add(pacDividedByNav);
            return new NumOfUnitsAndPacValue(numberOfUnits, rulesDto.getPac());
        }
        return new NumOfUnitsAndPacValue(numberOfUnits, BigDecimal.ZERO);
    }

    private BigDecimal getPacWithAnnualIndexing(ReinvestPacWithdrawalDTO rulesDto, LocalDate startDate, LocalDate date) {
        if (date.getYear() > startDate.getYear() && date.getMonth() == startDate.getMonth()) {
            final BigDecimal index = BigDecimal.ONE.add(DecimalUtils.divide(rulesDto.getPacIndex(), HUNDRED));
            return DecimalUtils.toUserScale(rulesDto.getPac().multiply(index));
        }
        return rulesDto.getPac();
    }

    private BigDecimal addNewUnitsFromDistribution(ReinvestPacWithdrawalDTO rulesDto, TreeMap<LocalDate, RHistoricalDistributions.DistributionsDto> distributions, TreeMap<LocalDate, RHistoricalDistributions.CapitalGainsDto> capitalGains, BigDecimal numberOfUnits, LocalDate date, BigDecimal nav) {
        if (rulesDto.isReinvest()) {
            var rDistribution = Optional.ofNullable(distributions).map(distribution -> distribution.get(date)).orElse(null);
            var rCapitalGains = Optional.ofNullable(capitalGains).map(capitalGain -> capitalGain.get(date)).orElse(null);
            BigDecimal sum = BigDecimal.ZERO;
            if (Objects.nonNull(rDistribution)) {
                sum = sum.add(rDistribution.sum());
            }
            if (Objects.nonNull(rCapitalGains)) {
                sum = sum.add(rCapitalGains.sum());
            }

            var multiplyDistributionByPreviousNumberOfUnits = sum.multiply(numberOfUnits);
            var newUnits = DecimalUtils.divide(multiplyDistributionByPreviousNumberOfUnits, nav);

            numberOfUnits = numberOfUnits.add(newUnits);
        }
        return numberOfUnits;
    }

    private BigDecimal distributionReceived(TreeMap<LocalDate, RHistoricalDistributions.DistributionsDto> distributions, TreeMap<LocalDate, RHistoricalDistributions.CapitalGainsDto> capitalGains, BigDecimal numberOfUnits, LocalDate date) {
        var d = Optional.ofNullable(distributions).map(distribution -> distribution.get(date)).orElse(null);
        var c = Optional.ofNullable(capitalGains).map(capitalGain -> capitalGain.get(date)).orElse(null);
        BigDecimal sum = BigDecimal.ZERO;
        if (Objects.nonNull(d)) {
            sum = sum.add(d.sum());
        }
        if (Objects.nonNull(c)) {
            sum = sum.add(c.sum());
        }
        return DecimalUtils.toUserScale(sum.multiply(numberOfUnits));
    }

    private BigDecimal distributionReceivedAndPopulateDistributionData(TreeMap<LocalDate, RHistoricalDistributions.DistributionsDto> distributions,
                                                                       TreeMap<LocalDate, RHistoricalDistributions.CapitalGainsDto> capitalGains,
                                                                       BigDecimal numberOfUnits, LocalDate date, DistributionData distributionData) {
        var d = Optional.ofNullable(distributions).map(distribution -> distribution.get(date)).orElse(null);
        var c = Optional.ofNullable(capitalGains).map(capitalGain -> capitalGain.get(date)).orElse(null);
        BigDecimal sum = BigDecimal.ZERO;
        if (Objects.nonNull(d)) {
            sum = sum.add(d.sum());
        }
        if (Objects.nonNull(c)) {
            sum = sum.add(c.sum());
        }
        var multiplyDistributionByPreviousNumberOfUnits = DecimalUtils.toUserScale(sum.multiply(numberOfUnits));

        if (!BigDecimal.ZERO.equals(multiplyDistributionByPreviousNumberOfUnits)) {
            distributionData.setFundDistribution(multiplyDistributionByPreviousNumberOfUnits);
            populateDistributionData(d, distributionData, numberOfUnits);
            populateCapitalGainsData(c, distributionData, numberOfUnits);
        }
        return multiplyDistributionByPreviousNumberOfUnits;
    }

    private void populateDistributionData(RHistoricalDistributions.DistributionsDto d, DistributionData distributionData, BigDecimal numberOfUnits) {
        if (Objects.nonNull(d)) {
            if (Objects.nonNull(d.getDomesticDividend())) {
                distributionData.setCanadianDividend(DecimalUtils.toUserScale(d.getDomesticDividend().multiply(numberOfUnits)));
            }
            if (Objects.nonNull(d.getForeignDividend())) {
                distributionData.setForeignDividend(DecimalUtils.toUserScale(d.getForeignDividend().multiply(numberOfUnits)));
            }
            if (Objects.nonNull(d.getInterestIncome())) {
                distributionData.setInterest(DecimalUtils.toUserScale(d.getInterestIncome().multiply(numberOfUnits)));
            }
        }
    }

    private void populateCapitalGainsData(RHistoricalDistributions.CapitalGainsDto c, DistributionData distributionData, BigDecimal numberOfUnits) {
        if (Objects.nonNull(c)) {
            if (Objects.nonNull(c.getCapitalGains())) {
                distributionData.setCapitalGains(DecimalUtils.toUserScale(c.getCapitalGains().multiply(numberOfUnits)));
            }
            if (Objects.nonNull(c.getReturnOfCapital())) {
                distributionData.setReturnOfCapital(DecimalUtils.toUserScale(c.getReturnOfCapital().multiply(numberOfUnits)));
            }
        }
    }

}
