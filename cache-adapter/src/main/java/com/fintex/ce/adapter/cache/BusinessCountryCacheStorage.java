package com.fintex.ce.adapter.cache;

import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import com.fintex.ce.domain.enumeration.Country;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.adapter.cache.entity.RBusinessCountry;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.businesscountry.BusinessCountryRepository;
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
public class BusinessCountryCacheStorage
    extends
      CacheStorageAbstract<BusinessCountry, RBusinessCountry, Map<Holding, BusinessCountry>> {

  @Autowired
  public BusinessCountryCacheStorage(
      SecurityDataPort<BusinessCountry> securityDataPort,
      CacheEntityMapper<BusinessCountry, RBusinessCountry> mapper,
      BusinessCountryRepository businessCountryRepository) {
    super(securityDataPort, mapper, businessCountryRepository, CacheNameEntity.BUSINESS_COUNTRY);
  }

  @Override
  public Map<Holding, BusinessCountry> load(final List<? extends Holding> holdings, final List<DataProvider> providers,
      final List<Warning> warnings, final ParamHolderDTO paramHolderDTO) {
    return load(holdings, providers, false);
  }

  public Map<Holding, BusinessCountry> load(final List<? extends Holding> holdings, final List<DataProvider> providers,
      final boolean needToCheckDataProvidersFromResponse) {
    final Map<StockHolding, BusinessCountry> response = loadForBenchOfStock(filterHoldings(holdings, STOCK_PREDICATE),
        List.of());

    if (needToCheckDataProvidersFromResponse) {
      DataProviderRequestHandlingValidator.dataProviderCheckValidation(
          providers,
          response.values(),
          getBusinessCountrySetValueFunction());
    }

    return new HashMap<>(response);
  }

  public BiFunction<BusinessCountry, String, BusinessCountry> getBusinessCountrySetValueFunction() {
    return BusinessCountry::setValue;
  }

  public Map<Holding, Country> loadBusinessCountries(final List<? extends Holding> holdings, final List<DataProvider> providers,
      final boolean needToCheckDataProvidersFromResponse,
      final List<Warning> warnings) {
    final Map<Holding, BusinessCountry> responseMap = load(holdings, providers, needToCheckDataProvidersFromResponse);
    return responseMap.entrySet().stream().collect(toMap(Map.Entry::getKey, e -> getCountryForHolding(e.getKey(), e
        .getValue(), warnings)));
  }

  public Country getCountryForHolding(final Holding holding,
      final BusinessCountry businessCountry,
      final List<Warning> warnings) {
    final String countryFromResponse = businessCountry.getValue();
    if (StringUtils.isBlank(countryFromResponse)) {
      warnings.add(new Warning(
          holding.generateUserIdentifier(),
          ExceptionCode.WRN_BCC_001.getMessage(),
          ExceptionCode.WRN_BCC_001.name()));
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
