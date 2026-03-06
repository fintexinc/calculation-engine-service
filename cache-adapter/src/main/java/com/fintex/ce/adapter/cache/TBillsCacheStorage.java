package com.fintex.ce.adapter.cache;

import com.fintex.ce.port.output.cache.TBillsProvider;
import com.fintex.smclient.service.CommonEndpointsComponent;
import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.adapter.cache.entity.RTBills;
import com.fintex.ce.adapter.cache.repository.TBillsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.TreeMap;

import static com.fintex.ce.adapter.cache.config.CacheConfig.LOAD_TBILLS;

@Slf4j
@Service
public class TBillsCacheStorage implements TBillsProvider {

  private final CommonEndpointsComponent commonEndpointsComponent;
  private final TBillsRepository tBillsRepository;

  public TBillsCacheStorage(final CommonEndpointsComponent commonEndpointsComponent,
      final TBillsRepository tBillsRepository) {
    this.commonEndpointsComponent = commonEndpointsComponent;
    this.tBillsRepository = tBillsRepository;
  }

  @Cacheable(value = LOAD_TBILLS, cacheManager = "caffeine1HourCacheManager")
  public TreeMap<LocalDate, BigDecimal> loadTBillsFor(final Currency currency) {
    final Collection<RTBills> tBills = tBillsRepository.findAllByCurrency(currency);
    if (tBills.isEmpty()) {
      final TreeMap<LocalDate, BigDecimal> loadedTBills = (TreeMap<LocalDate, BigDecimal>) commonEndpointsComponent
          .loadTreasuryBillsBy(mapToFdsCurrency(currency));
      tBillsRepository.save(new RTBills(currency, loadedTBills));
      return loadedTBills;
    }

    return tBills.stream().findFirst().map(i -> new TreeMap<>(i.getMonthlyReturns())).orElseThrow();
  }

  public com.fintex.smclient.enumeration.Currency mapToFdsCurrency(Currency currency) {
    return com.fintex.smclient.enumeration.Currency.of(currency.name());
  }

}
