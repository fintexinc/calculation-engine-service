package com.fintex.ce.repository.redis.core;

import com.fintex.ce.model.redis.core.RedisId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.fintex.ce.config.RedisConfig.PREFIX_ENV;

@Repository
@NoRepositoryBean
public interface CoreRedisCacheRepository<T extends RedisId> extends CrudRepository<T, String> {

    default List<T> findAllByHoldingId(String holdingId) {
        return this.findAllByHoldingIdAndPrefixEnv(holdingId, PREFIX_ENV);
    }

    default Optional<T> findOneByHoldingIdAndProvider(String holdingId, String provider) {
        return this.findOneByHoldingIdAndProviderAndPrefixEnv(holdingId, provider, PREFIX_ENV);
    }

    default Optional<T> findOneByHoldingIdAndProviders(String holdingId, String providers) {
        return this.findOneByHoldingIdAndProvidersAndPrefixEnv(holdingId, providers, PREFIX_ENV);
    }

    default List<T> findAllByPrefixEnv() {
        return this.findAllByPrefixEnv(PREFIX_ENV);
    }

    List<T> findAllByHoldingIdAndPrefixEnv(String holdingId, String prefixEnv);

    List<T> findAllByPrefixEnv(String prefixEnv);

    Optional<T> findOneByHoldingIdAndProviderAndPrefixEnv(String holdingId, String provider, String prefixEnv);

    Optional<T> findOneByHoldingIdAndProvidersAndPrefixEnv(String holdingId, String providers, String prefixEnv);

}
