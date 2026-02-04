package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.adapter.cache.entity.RTBills;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.fintex.ce.adapter.cache.config.RedisConfig.PREFIX_ENV;

@Repository
public interface TBillsRepository extends CoreRedisCacheRepository<RTBills> {

  Collection<RTBills> findAllByCurrencyAndPrefixEnv(Currency currency, String prefixEnv);

  default Collection<RTBills> findAllByCurrency(Currency currency) {
    return this.findAllByCurrencyAndPrefixEnv(currency, PREFIX_ENV);
  }

}
