package com.fintex.ce.adapter.webclient.boc.fetcher;

import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties.CurrencyPairConfig;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaWebClient;
import com.fintex.ce.adapter.webclient.boc.client.FxRateSource;
import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse;
import com.fintex.ce.adapter.webclient.boc.mapper.BankOfCanadaFxRateMapper;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.port.webclient.FxRatesFetcher;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches historical FX rates from the Bank of Canada Valet API. Implements the {@link FxRatesFetcher} port — the
 * common interface for both web-client and cache-based fetcher implementations.
 * <p>
 * Rate sources are configured per currency pair in application.yml (e.g., USD_CAD). When the exact pair is not
 * configured, the fetcher tries the inverse pair and inverts the rates. Adding a new currency pair is just a YAML
 * config change.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FxRatesFetcherImpl implements FxRatesFetcher {

  private static final int INVERSE_SCALE = 10;
  private static final String PAIR_SEPARATOR = "_";

  private final BankOfCanadaWebClient client;
  private final BankOfCanadaFxRateMapper mapper;
  private final BankOfCanadaProperties properties;

  @Override
  public NavigableMap<LocalDate, BigDecimal> fetch(CurrencyExchangePair currencyPair, DateRange dateRange) {
    if (currencyPair.from().equals(currencyPair.to())) {
      return new TreeMap<>();
    }

    String directKey = currencyPair.from().name() + PAIR_SEPARATOR + currencyPair.to().name();
    String inverseKey = currencyPair.to().name() + PAIR_SEPARATOR + currencyPair.from().name();

    LocalDate startDate = dateRange != null ? dateRange.start() : null;
    LocalDate endDate = dateRange != null ? dateRange.end() : null;

    CurrencyPairConfig directConfig = properties.getCurrencyPairs().get(directKey);
    if (directConfig != null) {
      NavigableMap<LocalDate, BigDecimal> rates = fetchFromSources(directConfig.getRateSources(), startDate, endDate);
      log.info("Fetched {} FX rates for {}", rates.size(), currencyPair);
      return rates;
    }

    CurrencyPairConfig inverseConfig = properties.getCurrencyPairs().get(inverseKey);
    if (inverseConfig != null) {
      NavigableMap<LocalDate, BigDecimal> rates = fetchFromSources(inverseConfig.getRateSources(), startDate, endDate);
      log.info("Fetched {} FX rates for {} (inverted from {})", rates.size(), currencyPair, inverseKey);
      return invert(rates);
    }

    log.warn("No FX rate configuration for pair: {} or {}", directKey, inverseKey);
    return new TreeMap<>();
  }

  private NavigableMap<LocalDate, BigDecimal> fetchFromSources(List<FxRateSource> allSources,
      LocalDate startDate, LocalDate endDate) {
    boolean hasDateRange = startDate != null && endDate != null;

    List<FxRateSource> sources = hasDateRange
        ? allSources.stream().filter(source -> overlaps(source, startDate, endDate)).toList()
        : allSources;

    log.debug("Selected {} of {} rate sources for range [{} — {}]",
        sources.size(), allSources.size(), startDate, endDate);

    NavigableMap<LocalDate, BigDecimal> mergedRates = new TreeMap<>();
    for (FxRateSource source : sources) {
      String url = buildUrl(source);
      log.debug("Fetching FX rates from Bank of Canada: {} (series: {})", url, source.getSeriesNames());

      BankOfCanadaFxRateResponse response = client.get(url, BankOfCanadaFxRateResponse.class);
      Map<LocalDate, BigDecimal> rates = mapper.map(response, source.getSeriesNames(), source.getFrequency());

      log.debug("Fetched {} FX rates ({}) from source: {}", rates.size(), source.getFrequency(), source.getPath());
      rates.forEach(mergedRates::putIfAbsent);
    }
    return mergedRates;
  }

  private NavigableMap<LocalDate, BigDecimal> invert(NavigableMap<LocalDate, BigDecimal> rates) {
    return rates.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> BigDecimal.ONE.divide(e.getValue(), INVERSE_SCALE, RoundingMode.HALF_UP),
            (a, b) -> a,
            TreeMap::new));
  }

  private boolean overlaps(FxRateSource source, LocalDate from, LocalDate to) {
    LocalDate sourceStart = parseDate(source.getStartDate(), LocalDate.MIN);
    LocalDate sourceEnd = parseDate(source.getEndDate(), LocalDate.MAX);
    return !sourceStart.isAfter(to) && !sourceEnd.isBefore(from);
  }

  private LocalDate parseDate(String date, LocalDate defaultValue) {
    return Optional.ofNullable(date)
        .filter(d -> !d.isBlank())
        .map(LocalDate::parse)
        .orElse(defaultValue);
  }

  private String buildUrl(FxRateSource source) {
    StringBuilder url = new StringBuilder(source.getPath());
    StringJoiner params = new StringJoiner("&", "?", "");
    params.setEmptyValue("");

    if (source.getStartDate() != null && !source.getStartDate().isBlank()) {
      params.add("start_date=" + source.getStartDate());
    }
    if (source.getEndDate() != null && !source.getEndDate().isBlank()) {
      params.add("end_date=" + source.getEndDate());
    }

    return url.append(params).toString();
  }
}
