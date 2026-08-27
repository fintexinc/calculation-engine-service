package ca.tangerine.pce.webclient.boc.mapper;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static ca.tangerine.pce.util.DateTimeUtils.toLastDayOfMonth;

import ca.tangerine.pce.webclient.boc.client.FxRateSource.Frequency;
import ca.tangerine.pce.webclient.boc.dto.BankOfCanadaFxRateResponse;
import ca.tangerine.pce.webclient.boc.dto.BankOfCanadaFxRateResponse.Observation;
import ca.tangerine.pce.webclient.boc.dto.BankOfCanadaFxRateResponse.SeriesValue;

@Component
public class BankOfCanadaFxRateMapper {

  public Map<LocalDate, BigDecimal> map(BankOfCanadaFxRateResponse response,
      List<String> seriesNames, Frequency frequency) {
    if (response == null || response.getObservations() == null || seriesNames == null || seriesNames.isEmpty()) {
      return Map.of();
    }

    return frequency == Frequency.MONTHLY
        ? mapMonthly(response.getObservations(), seriesNames)
        : mapDaily(response.getObservations(), seriesNames);
  }

  /**
   * Daily: keeps every observation keyed by its actual date.
   */
  private Map<LocalDate, BigDecimal> mapDaily(List<Observation> observations, List<String> seriesNames) {
    Map<LocalDate, BigDecimal> result = new TreeMap<>();

    for (Observation obs : observations) {
      if (obs.getDate() == null) {
        continue;
      }
      extractFirstAvailableRate(obs, seriesNames)
          .ifPresent(rate -> result.putIfAbsent(LocalDate.parse(obs.getDate()), rate));
    }

    return result;
  }

  /**
   * Monthly: groups by month, takes the last available observation, and keys by last day of month.
   */
  private Map<LocalDate, BigDecimal> mapMonthly(List<Observation> observations, List<String> seriesNames) {
    Map<YearMonth, Observation> lastPerMonth = groupByMonthTakingLast(observations, seriesNames);

    Map<LocalDate, BigDecimal> result = new TreeMap<>();
    lastPerMonth.forEach((yearMonth, obs) -> extractFirstAvailableRate(obs, seriesNames)
        .ifPresent(rate -> result.put(toLastDayOfMonth(yearMonth.atDay(1)), rate)));

    return result;
  }

  private Map<YearMonth, Observation> groupByMonthTakingLast(
      List<Observation> observations, List<String> seriesNames) {
    Map<YearMonth, Observation> result = new LinkedHashMap<>();

    for (Observation observation : observations) {
      if (observation.getDate() == null || !hasAnyValidRate(observation, seriesNames)) {
        continue;
      }
      LocalDate date = LocalDate.parse(observation.getDate());
      YearMonth yearMonth = YearMonth.from(date);
      result.merge(yearMonth, observation, (existing, incoming) -> {
        LocalDate existingDate = LocalDate.parse(existing.getDate());
        LocalDate incomingDate = LocalDate.parse(incoming.getDate());
        return incomingDate.isAfter(existingDate) ? incoming : existing;
      });
    }

    return result;
  }

  private boolean hasAnyValidRate(Observation observation, List<String> seriesNames) {
    return seriesNames.stream().anyMatch(name -> extractRate(observation, name).isPresent());
  }

  private Optional<BigDecimal> extractFirstAvailableRate(Observation observation, List<String> seriesNames) {
    return seriesNames.stream()
        .map(name -> extractRate(observation, name))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  private Optional<BigDecimal> extractRate(Observation observation, String seriesName) {
    return Optional.ofNullable(observation.getSeriesValues().get(seriesName))
        .map(SeriesValue::getValue)
        .filter(v -> !v.isBlank())
        .map(BigDecimal::new);
  }
}