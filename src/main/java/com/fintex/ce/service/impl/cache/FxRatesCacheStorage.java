package com.fintex.ce.service.impl.cache;

import com.fintex.ce.model.redis.RFxRates;
import com.fintex.ce.repository.redis.FxRatesRepository;
import com.fintex.smclient.dto.FxRatesDTO;
import com.fintex.smclient.service.CommonEndpointsComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.CacheConfig.GET_FX_RATES;

@Slf4j
@Service
public class FxRatesCacheStorage {

    private final CommonEndpointsComponent commonEndpointsComponent;
    private final FxRatesRepository fxRatesRepository;

    public FxRatesCacheStorage(final CommonEndpointsComponent commonEndpointsComponent, final FxRatesRepository fxRatesRepository) {
        this.commonEndpointsComponent = commonEndpointsComponent;
        this.fxRatesRepository = fxRatesRepository;
    }

    @Cacheable(value = GET_FX_RATES, cacheManager = "caffeine1HourCacheManager")
    public Map<LocalDate, FxRatesDTO> loadFxRates() {
        final List<RFxRates> fxRatesFromCache = fxRatesRepository.findAllByPrefixEnv();
        if (fxRatesFromCache.isEmpty()) {
            final Map<LocalDate, FxRatesDTO> fxRatesFromFds = commonEndpointsComponent.loadFxRates();
            fxRatesRepository.save(new RFxRates(fxRatesFromFds));
            return fxRatesFromFds;
        }

        return fxRatesFromCache.stream().findFirst().orElseThrow().getFxRates();
    }
}
