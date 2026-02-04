package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.RFxRates;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FxRatesRepository extends CoreRedisCacheRepository<RFxRates> {

}
