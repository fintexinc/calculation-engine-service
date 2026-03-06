package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.enumeration.ExceptionCode;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.ValidationError;
import com.fintex.ce.domain.model.ParamHolderDTO;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.core.Warning;
import com.fintex.ce.domain.exception.DataErrorException;
import com.fintex.ce.domain.exception.notification.pattern.Notification;
import com.fintex.ce.adapter.cache.entity.RSalesCharge;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.sm.SecurityDataPort;
import com.fintex.ce.adapter.cache.repository.SalesChargeRepository;
import com.fintex.ce.adapter.cache.core.CacheStorageAbstract;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.constant.CacheNameEntity.BUSINESS_COUNTRY;
import static com.fintex.ce.constant.CacheNameEntity.SALES_CHARGE;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Service
public class SalesChargeCacheStorage
    extends CacheStorageAbstract<SalesCharge, RSalesCharge, Map<Holding, SalesCharge>> {

  public SalesChargeCacheStorage(SecurityDataPort<SalesCharge> securityDataPort,
      CacheEntityMapper<SalesCharge, RSalesCharge> mapper,
      SalesChargeRepository salesChargeRepository,
      CacheStatisticService cacheStatisticService) {
    super(securityDataPort, mapper, salesChargeRepository, cacheStatisticService, SALES_CHARGE);
  }

  @Override
  public Map<Holding, SalesCharge> load(List<? extends Holding> holdings,
      List<DataProvider> providers,
      List<Warning> warnings,
      ParamHolderDTO paramHolderDTO) {
    Map<FundSeriesHolding, SalesCharge> response = loadBenchOfFundCanada(filterHoldings(holdings,
        CANADA_MUTUAL_PREDICATE), List.of());

    var notification = new Notification();
    response.values()
        .stream()
        .filter(salesCharge -> salesCharge != null && hasErrors(salesCharge))
        .forEach(salesCharge -> notification.addErrors(getErrors(salesCharge)));
    notification.ifAnyErrorThrowException();

    return new HashMap<>(response);
  }

  private boolean hasErrors(SalesCharge salesCharge) {
    return salesCharge.hasErrors();
  }

  private List<DataErrorException> getErrors(SalesCharge salesCharge) {
    return salesCharge.getErrors().stream()
        .map(this::toDataErrorException)
        .toList();
  }

  private DataErrorException toDataErrorException(ValidationError error) {
    ExceptionCode code = error.getCode() != null ? ExceptionCode.valueOf(error.getCode()) : null;
    return new DataErrorException(error.getMessage(), error.getId(), code);
  }

}
