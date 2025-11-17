package com.fintex.ce.service.impl.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.dto.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.model.redis.core.RedisId;
import com.fintex.ce.repository.graphql.query.AverageMERSMRepository;
import com.fintex.ce.repository.redis.averagemer.AverageMerRepository;
import com.fintex.ce.service.interfaces.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.fintex.ce.config.enumeration.cache.CacheNameEntity.MER;
import static com.fintex.ce.util.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;

@Service
public class AverageMERCacheStorage extends ManagementExpenseAbstractCacheStorage<RAverageMer, RAverageMer, RAverageMer, RedisId> {

    public AverageMERCacheStorage(final AverageMERSMRepository queryRepository,
                                  final AverageMerRepository fundCanadaRepo,
                                  final AverageMerRepository etfCanadaRepo,
                                  final AverageMerRepository etfUsRepo,
                                  final CacheStatisticService cacheStatisticService) {
        super(queryRepository, fundCanadaRepo, etfCanadaRepo, etfUsRepo, cacheStatisticService, MER);
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForEtfCanada(final Holding etfHolding,
                                                                     final RAverageMer averageMerEtfCanada) {
        final var result = preBuildAverageMerDto(etfHolding);
        result.setManagementExpenseRatio(averageMerEtfCanada.getMer());
        result.setActualManagementFee(averageMerEtfCanada.getActualManagementFee());
        return result;
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForEtfUs(final Holding etfHolding,
                                                                 final RAverageMer averageMerEtfUs) {
        final var result = preBuildAverageMerDto(etfHolding);
        result.setNetExpenseRatio(averageMerEtfUs.getNetExpenseRatio());
        result.setGrossExpenseRatio(averageMerEtfUs.getGrossExpenseRatio());
        return result;
    }

    @Override    
    public AverageManagementExpenseCalculationDTO mapperForCanadaMutualFund(final Holding fundSeriesHolding,
                                                                            final RAverageMer fundSeries) {
        final var result = preBuildAverageMerDto(fundSeriesHolding);
        result.setActualManagementFee(fundSeries.getActualManagementFee());
        result.setManagementExpenseRatio(fundSeries.getMer());
        return result;
    }

    @Override
    public void dataProviderCheckerForCanadaMutualFund(final List<DataProvider> providers,
                                                       final Collection<RAverageMer> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getMerProvider,
                RAverageMer::setMer);
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getActualManagementFeeProvider,
                RAverageMer::setActualManagementFee);
    }

    @Override
    public void dataProviderCheckerForEtfCanada(final List<DataProvider> providers,
                                                final Collection<RAverageMer> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getMerProvider,
                RAverageMer::setMer);
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getActualManagementFeeProvider,
                RAverageMer::setActualManagementFee);
    }

    @Override
    public void dataProviderCheckerForEtfUs(final List<DataProvider> providers,
                                            final Collection<RAverageMer> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getNetExpenseRatioProvider,
                RAverageMer::setNetExpenseRatio);
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getGrossExpenseRatioProvider,
                RAverageMer::setGrossExpenseRatio);
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForUsMutualFund(final Holding holding, final RAverageMer averageMerUsMutualFund) {
        final var result = preBuildAverageMerDto(holding);
        result.setNetExpenseRatio(averageMerUsMutualFund.getNetExpenseRatio());
        result.setGrossExpenseRatio(averageMerUsMutualFund.getGrossExpenseRatio());
        return result;
    }

    @Override
    public void dataProviderCheckerForUsMutualFund(final List<DataProvider> providers, final Collection<RAverageMer> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getNetExpenseRatioProvider,
                RAverageMer::setNetExpenseRatio);
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getGrossExpenseRatioProvider,
                RAverageMer::setGrossExpenseRatio);
    }

    @Override
    public AverageManagementExpenseCalculationDTO mapperForCanadaHedgeFund(final Holding holding, final RAverageMer averageMerCanadaHedgeFund) {
        final var result = preBuildAverageMerDto(holding);
        result.setActualManagementFee(averageMerCanadaHedgeFund.getActualManagementFee());
        result.setManagementExpenseRatio(averageMerCanadaHedgeFund.getMer());
        return result;
    }

    @Override
    public void dataProviderCheckerForCanadaHedgeFund(final List<DataProvider> providers, final Collection<RAverageMer> responseFromFds) {
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getMerProvider,
                RAverageMer::setMer);
        dataProviderCheckValidation(providers, responseFromFds, RAverageMer::getActualManagementFeeProvider,
                RAverageMer::setActualManagementFee);
    }
}
