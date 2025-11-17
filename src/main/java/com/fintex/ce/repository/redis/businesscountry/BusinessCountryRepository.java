package com.fintex.ce.repository.redis.businesscountry;

import com.fintex.ce.model.redis.RBusinessCountry;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessCountryRepository extends CoreRedisCacheRepository<RBusinessCountry> {
}
