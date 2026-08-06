package com.fintex.ce.adapter.webclient.boc.fetcher;

import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaProperties.CurrencyPairConfig;
import com.fintex.ce.adapter.webclient.boc.client.BankOfCanadaWebClient;
import com.fintex.ce.adapter.webclient.boc.client.FxRateSource;
import com.fintex.ce.adapter.webclient.boc.dto.BankOfCanadaFxRateResponse;
import com.fintex.ce.adapter.webclient.boc.mapper.BankOfCanadaFxRateMapper;
import com.fintex.ce.adapter.webclient.observability.ExternalServiceObservability;
import com.fintex.ce.model.domain.CurrencyExchangePair;
import com.fintex.ce.model.domain.calculation.DateRange;
import com.fintex.ce.port.webclient.boc.FxRatesFetcher;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

import static com.fintex.ce.model.util.BigDecimalUtils.invert;

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
public class BocFxRatesFetcher implements FxRatesFetcher {

  private static final String PAIR_SEPARATOR = "_";

  private final BankOfCanadaWebClient client;
  private final BankOfCanadaFxRateMapper mapper;
  private final BankOfCanadaProperties properties;
  private final ExternalServiceObservability observability;

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
      return invertRates(rates);
    }

    log.warn("No FX rate configuration for pair: {} or {}", directKey, inverseKey);
    return new TreeMap<>();
  }

  @Override
  public CurrencyExchangePair canonicalDirection(CurrencyExchangePair pair) {
    String directKey = pair.from().name() + PAIR_SEPARATOR + pair.to().name();
    if (properties.getCurrencyPairs().containsKey(directKey)) {
      return pair;
    }
    String inverseKey = pair.to().name() + PAIR_SEPARATOR + pair.from().name();
    if (properties.getCurrencyPairs().containsKey(inverseKey)) {
      return pair.inverse();
    }
    return pair;
  }

  private NavigableMap<LocalDate, BigDecimal> fetchFromSources(List<FxRateSource> allSources,
      LocalDate startDate, LocalDate endDate) {
    DateRange requestedRange = new DateRange(startDate, endDate);
    List<FxRateSource> sources = allSources.stream()
        .filter(source -> overlaps(source, startDate, endDate))
        .toList();

    log.debug("Selected {} of {} rate sources for range [{} — {}]",
        sources.size(), allSources.size(), startDate, endDate);

    NavigableMap<LocalDate, BigDecimal> mergedRates = new TreeMap<>();
    for (FxRateSource source : sources) {
      String url = buildUrl(source, startDate, endDate);
      log.debug("Fetching FX rates from Bank of Canada: {} (series: {})", url, source.getSeriesNames());

      BankOfCanadaFxRateResponse response = client.get(url, BankOfCanadaFxRateResponse.class);
      Map<LocalDate, BigDecimal> rates = mapper.map(response, source.getSeriesNames(), source.getFrequency());
      observability.recordResultSize(BankOfCanadaWebClient.SERVICE_TAG_VALUE, source.getPath(), rates.size());

      log.debug("Fetched {} FX rates ({}) from source: {}", rates.size(), source.getFrequency(), source.getPath());
      rates.forEach(mergedRates::putIfAbsent);
    }
    return mergedRates.entrySet().stream()
        .filter(entry -> requestedRange.contains(entry.getKey()))
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            Map.Entry::getValue,
            (first, second) -> first,
            TreeMap::new));
  }

  private NavigableMap<LocalDate, BigDecimal> invertRates(NavigableMap<LocalDate, BigDecimal> rates) {
    return rates.entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            e -> invert(e.getValue()),
            (a, b) -> a,
            TreeMap::new));
  }

  private boolean overlaps(FxRateSource source, LocalDate from, LocalDate to) {
    LocalDate sourceStart = parseDate(source.getStartDate(), LocalDate.MIN);
    LocalDate sourceEnd = parseDate(source.getEndDate(), LocalDate.MAX);
    return (to == null || !sourceStart.isAfter(to))
        && (from == null || !sourceEnd.isBefore(from));
  }

  private LocalDate parseDate(String date, LocalDate defaultValue) {
    return Optional.ofNullable(date)
        .filter(d -> !d.isBlank())
        .map(LocalDate::parse)
        .orElse(defaultValue);
  }

  private String buildUrl(FxRateSource source, LocalDate requestStart, LocalDate requestEnd) {
    StringBuilder url = new StringBuilder(source.getPath());
    StringJoiner params = new StringJoiner("&", "?", "");
    params.setEmptyValue("");

    LocalDate effectiveStart = pickStart(requestStart, source.getStartDate());
    if (effectiveStart != null) {
      params.add("start_date=" + effectiveStart);
    }
    LocalDate effectiveEnd = pickEnd(requestEnd, source.getEndDate());
    if (effectiveEnd != null) {
      params.add("end_date=" + effectiveEnd);
    }

    return url.append(params).toString();
  }

  private LocalDate pickStart(LocalDate requestStart, String sourceStartDate) {
    LocalDate sourceStart = parseDate(sourceStartDate, null);
    if (requestStart == null) {
      return sourceStart;
    }
    if (sourceStart == null || requestStart.isAfter(sourceStart)) {
      return requestStart;
    }
    return sourceStart;
  }

  private LocalDate pickEnd(LocalDate requestEnd, String sourceEndDate) {
    LocalDate sourceEnd = parseDate(sourceEndDate, null);
    if (requestEnd == null) {
      return sourceEnd;
    }
    if (sourceEnd == null || requestEnd.isBefore(sourceEnd)) {
      return requestEnd;
    }
    return sourceEnd;
  }
}
