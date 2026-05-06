package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.port.webclient.sm.TreasuryBillsFetcher;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.domain.rates.TreasuryRates;
import com.fintex.wm.commons.domain.reference.TreasuryRateReturn;
import com.fintex.wm.commons.domain.reference.TreasuryRateReturnsDatapoint;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches the historical risk-free rate (T-Bill) monthly return series from Security Master and returns the slice for a
 * single {@link Currency}. SMS exposes per-date entries with a {@code Map<Currency, BigDecimal>} of rates; this fetcher
 * pulls the full payload and projects the requested currency's rates into a per-date
 * {@link NavigableMap}{@code <}{@link LocalDate}{@code , BigDecimal>}.
 *
 * <p>
 * Each call invokes the SMS endpoint once. When multiple metric services run for different currencies in the same
 * request, the {@code CachingTreasuryBillsFetcher} decorator (in {@code cache-adapter}) collapses repeat lookups to a
 * single upstream call per currency per refresh window.
 * </p>
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class SmsTreasuryBillsFetcherImpl implements TreasuryBillsFetcher {

  private final SecurityMasterWebClient client;
  private final String treasuryRatesEndpoint;

  public SmsTreasuryBillsFetcherImpl(SecurityMasterWebClient client,
      @Value("${external-services.security-master.rest.endpoints.reference.treasury-rates}") String treasuryRatesEndpoint) {
    this.client = client;
    this.treasuryRatesEndpoint = treasuryRatesEndpoint;
  }

  @Override
  public NavigableMap<LocalDate, BigDecimal> fetch(Currency currency) {
    TreasuryRates response = client.get(treasuryRatesEndpoint, TreasuryRates.class);
    List<TreasuryRateReturn> entries = Optional.ofNullable(response)
        .map(TreasuryRates::getReturns)
        .map(TreasuryRateReturnsDatapoint::getReturns)
        .orElse(List.of());

    NavigableMap<LocalDate, BigDecimal> series = new TreeMap<>();
    for (TreasuryRateReturn entry : entries) {
      if (entry.getDate() == null || entry.getRates() == null) {
        continue;
      }
      BigDecimal rate = entry.getRates().get(currency);
      if (rate != null) {
        series.put(entry.getDate(), rate);
      }
    }

    if (CollectionUtils.isEmpty(entries)) {
      log.warn("Security Master returned no Treasury rate entries");
    } else {
      log.debug("Fetched {} Treasury rate entries from Security Master for {}", series.size(), currency);
    }
    return series;
  }
}
