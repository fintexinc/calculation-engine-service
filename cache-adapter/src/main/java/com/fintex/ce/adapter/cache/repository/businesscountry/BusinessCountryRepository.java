package com.fintex.ce.adapter.cache.repository.businesscountry;

import com.fintex.ce.adapter.cache.entity.RBusinessCountry;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BusinessCountryRepository extends CoreRedisCacheRepository<RBusinessCountry> {
}
