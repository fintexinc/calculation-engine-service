package com.fintex.ce.application.calculation;

import com.fintex.ce.application.result.AnnualReturnResult;
import com.fintex.ce.application.result.core.KeyValueResult;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.util.DecimalUtils;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.fintex.ce.util.CalculationUtils.product;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static java.math.BigDecimal.ONE;

@Log4j2
public class AnnualReturnCalculation {

  private final NavigableMap<LocalDate, BigDecimal> portfolioTotalReturns;
  private final List<Warning> warnings;

  public AnnualReturnCalculation(final NavigableMap<LocalDate, BigDecimal> weightedAveragePortfolioReturns,
      final List<Warning> warnings) {
    this.portfolioTotalReturns = weightedAveragePortfolioReturns;
    this.warnings = warnings;
  }

  public AnnualReturnResult<Integer> calculate() {
    final var portfolioReturns = new TreeMap<>(portfolioTotalReturns);
    final Set<Integer> years = portfolioReturns.keySet().stream().map(LocalDate::getYear).collect(Collectors.toSet());
    final NavigableMap<Integer, BigDecimal> result = calculateAnnualReturns(portfolioReturns, years);
    final AnnualReturnResult<Integer> responseDTO = new AnnualReturnResult<>();
    responseDTO.setAnnualReturns(result.entrySet().stream().map(e -> new KeyValueResult<>(e.getKey(), e.getValue()))
        .toList());
    populateBasicDetails(responseDTO, portfolioReturns);
    responseDTO.setWarnings(warnings);
    return responseDTO;
  }

  /**
   * Calculates annual returns of the passing portfolio returns
   *
   * @param portfolioReturns
   *          calculated portfolio returns
   * @param years
   *          set of years
   * @return annual returns map
   */
  public TreeMap<Integer, BigDecimal> calculateAnnualReturns(final TreeMap<LocalDate, BigDecimal> portfolioReturns,
      final Set<Integer> years) {
    final TreeMap<Integer, BigDecimal> map = new TreeMap<>();
    for (final Integer year : years) {
      final LocalDate startDate = toLastDayOfMonth(LocalDate.of(year, Month.JANUARY, 1));
      final LocalDate endDate = toLastDayOfMonth(LocalDate.of(year, Month.DECEMBER, 1));
      if (!portfolioReturns.containsKey(startDate) || !portfolioReturns.containsKey(endDate)) {
        continue;
      }
      final NavigableMap<LocalDate, BigDecimal> subMap = portfolioReturns.subMap(startDate, true, endDate, true);
      if (subMap.size() < 12) {
        log.warn(String.format("Portfolio Returns are missing a few months between period: %s - %s", startDate,
            endDate));
        continue;
      }
      final BigDecimal product = product(subMap).subtract(ONE);
      map.put(year, DecimalUtils.toUserScale(product));
    }
    return map;
  }

  /**
   * Populates PSD and PED for the response
   *
   * @param responseDTO
   *          response DTO
   * @param portfolioReturns
   *          portfolio returns
   */
  public void populateBasicDetails(final AnnualReturnResult<Integer> responseDTO,
      final TreeMap<LocalDate, BigDecimal> portfolioReturns) {
    responseDTO.setPsd(portfolioReturns.firstKey());
    responseDTO.setPed(portfolioReturns.lastKey());
  }

}
