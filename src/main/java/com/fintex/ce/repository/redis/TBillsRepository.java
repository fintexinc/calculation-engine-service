package com.fintex.ce.repository.redis;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.model.redis.RTBills;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;

import static com.fintex.ce.config.RedisConfig.PREFIX_ENV;

@Repository
public interface TBillsRepository extends CoreRedisCacheRepository<RTBills> {

    Collection<RTBills> findAllByCurrencyAndPrefixEnv(Currency currency, String prefixEnv);

    default Collection<RTBills> findAllByCurrency(Currency currency) {
        return this.findAllByCurrencyAndPrefixEnv(currency, PREFIX_ENV);
    }

}
