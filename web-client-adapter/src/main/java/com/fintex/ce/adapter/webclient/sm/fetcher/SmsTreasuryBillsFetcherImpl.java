package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.rates.DateRateValue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches the historical risk-free rate (T-Bill) monthly return series from Security Master for a single
 * {@link Currency}. Calls {@code GET /treasury-rates/{currency}} so SMS dispatches to that currency's upstream resolver
 * and returns the series directly as a flat {@code List<DateRateValue>} — no envelope, no map lookup. The list is
 * rebuilt into a {@code NavigableMap<LocalDate, BigDecimal>} at this seam so the rest of the calculation layer keeps
 * its date-keyed lookups. Unsupported currencies yield an empty map.
 *
 * <p>
 * Each call invokes the SMS endpoint once. The {@code CachingTreasuryBillsFetcher} decorator (in {@code cache-adapter})
 * collapses repeat invocations for the same currency to one upstream call per refresh window.
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class SmsTreasuryBillsFetcherImpl implements TreasuryBillsFetcher {

  private static final ParameterizedTypeReference<List<DateRateValue>> RATES_TYPE = new ParameterizedTypeReference<>() {};

  private final SecurityMasterWebClient client;
  private final String treasuryRatesEndpoint;

  public SmsTreasuryBillsFetcherImpl(SecurityMasterWebClient client,
      @Value("${external-services.security-master.rest.endpoints.reference.treasury-rates}") String treasuryRatesEndpoint) {
    this.client = client;
    this.treasuryRatesEndpoint = treasuryRatesEndpoint;
  }

  @Override
  public NavigableMap<LocalDate, BigDecimal> fetch(Currency currency) {
    List<DateRateValue> rates = client.get(treasuryRatesEndpoint + "/" + currency.name(), RATES_TYPE);
    if (CollectionUtils.isEmpty(rates)) {
      log.warn("Security Master returned no Treasury rate entries for {}", currency);
      return new TreeMap<>();
    }
    NavigableMap<LocalDate, BigDecimal> series = rates.stream()
        .collect(Collectors.toMap(DateRateValue::date, DateRateValue::rate, (a, b) -> b, TreeMap::new));
    log.debug("Fetched {} Treasury rate entries from Security Master for {}", series.size(), currency);
    return series;
  }
}