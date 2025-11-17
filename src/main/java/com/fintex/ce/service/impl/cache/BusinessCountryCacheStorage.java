package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.Country;
import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.model.redis.RBusinessCountry;
import com.fintex.ce.repository.graphql.query.BusinessCountrySMRepository;
import com.fintex.ce.repository.redis.businesscountry.BusinessCountryRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import com.fintex.ce.util.validation.DataProviderRequestHandlingValidator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.FilterUtils.STOCK_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Service
public class BusinessCountryCacheStorage extends MultipleCacheStorageAbstract<RBusinessCountry, RBusinessCountry, RBusinessCountry, RBusinessCountry> {

    @Autowired
    public BusinessCountryCacheStorage(BusinessCountrySMRepository queryRepository,
                                       BusinessCountryRepository businessCountryRepository,
                                       CacheStatisticService cacheStatisticService) {
        super(queryRepository, businessCountryRepository, businessCountryRepository,
                businessCountryRepository, businessCountryRepository, cacheStatisticService, CacheNameEntity.BUSINESS_COUNTRY);
    }

    @Override
    public Map<Holding, RBusinessCountry> load(final List<Holding> holdings, final List<DataProvider> providers,
                                               final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
        return load(holdings, providers, false);
    }

    Map<Holding, RBusinessCountry> load(final List<Holding> holdings, final List<DataProvider> providers,
                                        final boolean needToCheckDataProvidersFromResponse) {
        final Map<StockHolding, RBusinessCountry> response = loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE), List.of());

        if (needToCheckDataProvidersFromResponse) {
            DataProviderRequestHandlingValidator.dataProviderCheckValidation(providers, response.values(), getRBusinessCountrySetValueFunction());
        }

        return new HashMap<>(response);
    }

    BiFunction<RBusinessCountry, String, RBusinessCountry> getRBusinessCountrySetValueFunction() {
        return RBusinessCountry::setValue;
    }

    public Map<Holding, Country> loadBusinessCountries(final List<Holding> holdings, final List<DataProvider> providers,
                                                       final boolean needToCheckDataProvidersFromResponse,
                                                       final List<Warning> warnings) {
        final Map<Holding, RBusinessCountry> responseMap = load(holdings, providers, needToCheckDataProvidersFromResponse);
        return responseMap.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> getCountryForHolding(e.getKey(), e.getValue(), warnings)));
    }

    Country getCountryForHolding(final Holding holding,
                                 final RBusinessCountry rBusinessCountry,
                                 final List<Warning> warnings) {
        final String countryFromResponse = rBusinessCountry.getValue();
        if (StringUtils.isBlank(countryFromResponse)) {
            warnings.add(new Warning(
                    holding.generateUserIdentifier(),
                    ExceptionCode.WRN_BCC_001.getMessage(),
                    ExceptionCode.WRN_BCC_001.name()
            ));
            return Country.EMPTY;
        }
        if (Country.CAN.name().equalsIgnoreCase(countryFromResponse)) {
            return Country.CAN;
        } else if (Country.USA.name().equalsIgnoreCase(countryFromResponse)) {
            return Country.USA;
        }
        return Country.OTHER;
    }

}
