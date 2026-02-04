package com.fintex.ce.adapter.cache.repository;

import com.fintex.ce.adapter.cache.entity.RCountryExposure;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CountryExposureRepository extends CoreRedisCacheRepository<RCountryExposure> {
}
