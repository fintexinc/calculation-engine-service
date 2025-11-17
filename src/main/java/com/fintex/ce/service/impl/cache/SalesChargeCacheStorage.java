package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.ParamHolderDTO;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.exception.notification.pattern.Notification;
import com.fintex.ce.model.redis.RSalesCharge;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.SalesChargeSMRepository;
import com.fintex.ce.repository.redis.SalesChargeRepository;
import com.fintex.ce.service.impl.cache.core.MultipleCacheStorageAbstract;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.BUSINESS_COUNTRY;
import static com.fintex.ce.util.FilterUtils.CANADA_MUTUAL_PREDICATE;
import static com.fintex.ce.util.FilterUtils.filterHoldings;

@Service
public class SalesChargeCacheStorage extends MultipleCacheStorageAbstract<RSalesCharge, RSalesCharge, RSalesCharge, RSalesCharge> {

    @Autowired
    public SalesChargeCacheStorage(SalesChargeSMRepository queryRepository,
                                   SalesChargeRepository salesChargeRepository,
                                   CacheStatisticService cacheStatisticService) {
        super(queryRepository, salesChargeRepository, salesChargeRepository,
                salesChargeRepository, salesChargeRepository, cacheStatisticService, BUSINESS_COUNTRY);
    }

    @Override
    public Map<Holding, RSalesCharge> load(List<Holding> holdings,
                                           List<DataProvider> providers,
                                           List<Warning> warnings,
                                           ParamHolderDTO paramHolderDTO) {
        Map<FundSeriesHolding, RSalesCharge> response = loadBenchOfFundCanada(filterHoldings(holdings, CANADA_MUTUAL_PREDICATE), List.of());

        var notification = new Notification();
        response.values()
                .stream()
                .filter(RedisId::hasErrors)
                .forEach(rSalesCharge -> notification.addErrors(rSalesCharge.getErrors()));
        notification.ifAnyErrorThrowException();

        return new HashMap<>(response);
    }

}
