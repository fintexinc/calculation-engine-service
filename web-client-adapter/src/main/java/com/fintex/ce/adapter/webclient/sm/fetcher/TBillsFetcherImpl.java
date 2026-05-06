package com.fintex.ce.adapter.webclient.sm.fetcher;

import com.fintex.ce.adapter.webclient.sm.client.SecurityMasterWebClient;
import com.fintex.ce.port.webclient.sm.TBillsFetcher;
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
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Fetches the historical risk-free rate (T-Bill) monthly return series from Security Master. SMS exposes per-date
 * entries with a {@code Map<Currency, BigDecimal>} of rates; this fetcher transposes that into a per-currency
 * {@link NavigableMap}{@code <}{@link LocalDate}{@code , BigDecimal>} so callers pick a currency by lookup. Adding a
 * new currency is purely a producer-side change — this fetcher is currency-agnostic.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "external-services.security-master.api-type", havingValue = "rest", matchIfMissing = true)
public class TBillsFetcherImpl implements TBillsFetcher {

  private final SecurityMasterWebClient client;
  private final String treasuryRatesEndpoint;

  public TBillsFetcherImpl(SecurityMasterWebClient client,
      @Value("${external-services.security-master.rest.endpoints.reference.treasury-rates}") String treasuryRatesEndpoint) {
    this.client = client;
    this.treasuryRatesEndpoint = treasuryRatesEndpoint;
  }

  @Override
  public Map<Currency, NavigableMap<LocalDate, BigDecimal>> fetch() {
    TreasuryRates response = client.get(treasuryRatesEndpoint, TreasuryRates.class);
    List<TreasuryRateReturn> entries = Optional.ofNullable(response)
        .map(TreasuryRates::getReturns)
        .map(TreasuryRateReturnsDatapoint::getReturns)
        .orElse(List.of());

    // Pre-populate every Currency with an empty series so callers can do `.get(currency)` without null-checking.
    // Currencies SMS doesn't supply yield an empty TreeMap (the prior fetcher's short-circuit contract).
    Map<Currency, NavigableMap<LocalDate, BigDecimal>> rates = new EnumMap<>(Currency.class);
    for (Currency currency : Currency.values()) {
      rates.put(currency, new TreeMap<>());
    }

    for (TreasuryRateReturn entry : entries) {
      if (entry.getDate() == null || entry.getRates() == null) {
        continue;
      }
      entry.getRates().forEach((currency, rate) -> {
        if (rate != null) {
          rates.get(currency).put(entry.getDate(), rate);
        }
      });
    }

    if (CollectionUtils.isEmpty(entries)) {
      log.warn("Security Master returned no Treasury rate entries");
    } else {
      log.info("Fetched {} Treasury rate entries from Security Master", entries.size());
    }
    return rates;
  }
}