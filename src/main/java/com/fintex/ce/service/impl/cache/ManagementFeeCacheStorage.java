package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.graphql.query.ManagementFeeSMRepository;
import com.fintex.ce.repository.redis.managementfee.ManagementFeeRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;

@Service
public class ManagementFeeCacheStorage extends ManagementExpenseAbstractCacheStorage<RManagementFee, RManagementFee, RManagementFee, RedisId> {

    public ManagementFeeCacheStorage(ManagementFeeSMRepository queryRepository,
                                     ManagementFeeRepository fundCanadaRepo,
                                     ManagementFeeRepository etfCanadaRepo,
                                     ManagementFeeRepository etfUsRepo,
                                     CacheStatisticService cacheStatisticService) {
        super(queryRepository, fundCanadaRepo, etfCanadaRepo, etfUsRepo, cacheStatisticService, MANAGEMENT_FEE);
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForCanadaMutualFund(Holding fundSeriesHolding,
                                                                            RManagementFee fundSeries) {
        var result = preBuildAverageMerDto(fundSeriesHolding);
        result.setActualManagementFee(fundSeries.getManagementFee());
        return result;
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForEtfCanada(Holding etfHolding, RManagementFee averageMerEtfCanada) {
        var result = preBuildAverageMerDto(etfHolding);
        result.setActualManagementFee(averageMerEtfCanada.getManagementFee());
        return result;
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForEtfUs(Holding etfHolding, RManagementFee averageMerEtfUs) {
        var result = preBuildAverageMerDto(etfHolding);
        result.setActualManagementFee(averageMerEtfUs.getManagementFee());
        return result;
    }

    @Override
    public void dataProviderCheckerForCanadaMutualFund(List<DataProvider> providers,
                                                       Collection<RManagementFee> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RManagementFee::setManagementFee);
    }

    @Override
    public void dataProviderCheckerForEtfCanada(List<DataProvider> providers,
                                                Collection<RManagementFee> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RManagementFee::setManagementFee);
    }

    @Override
    public void dataProviderCheckerForEtfUs(List<DataProvider> providers,
                                            Collection<RManagementFee> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RManagementFee::setManagementFee);
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForUsMutualFund(final Holding holding,
                                                                        final RManagementFee managementFeeUsFund) {
        var result = preBuildAverageMerDto(holding);
        result.setActualManagementFee(managementFeeUsFund.getManagementFee());
        return result;
    }

    @Override
    public void dataProviderCheckerForUsMutualFund(final List<DataProvider> providers, final Collection<RManagementFee> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RManagementFee::setManagementFee);
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForCanadaHedgeFund(final Holding holding, final RManagementFee managementFeeCanadaHedgeFund) {
        var result = preBuildAverageMerDto(holding);
        result.setActualManagementFee(managementFeeCanadaHedgeFund.getManagementFee());
        return result;
    }

    @Override
    public void dataProviderCheckerForCanadaHedgeFund(final List<DataProvider> providers, final Collection<RManagementFee> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RManagementFee::setManagementFee);
    }
}
