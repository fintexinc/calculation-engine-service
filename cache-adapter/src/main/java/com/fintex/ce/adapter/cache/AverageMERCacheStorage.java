package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.cache.entity.averagemer.RAverageMer;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.averagemer.AverageMerRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.fintex.ce.constant.CacheNameEntity.MER;
import static com.fintex.ce.util.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;

@Service
public class AverageMERCacheStorage
    extends
      ManagementExpenseAbstractCacheStorage<AverageMer, AverageMer, AverageMer, AverageMer, RAverageMer> {

  public AverageMERCacheStorage(final MultipleSMRepository<AverageMer, AverageMer, AverageMer, AverageMer> smRepo,
      final CacheEntityMapper<AverageMer, RAverageMer> mapper,
      final AverageMerRepository fundCanadaRepo,
      final AverageMerRepository etfCanadaRepo,
      final AverageMerRepository etfUsRepo,
      final CacheStatisticService cacheStatisticService) {
    super(smRepo, mapper, mapper, mapper, null, fundCanadaRepo, etfCanadaRepo, etfUsRepo, cacheStatisticService, MER);
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForEtfCanada(final Holding etfHolding,
      final AverageMer averageMerEtfCanada) {
    final var result = preBuildAverageMerDto(etfHolding);
    result.setManagementExpenseRatio(averageMerEtfCanada.getMer());
    result.setActualManagementFee(averageMerEtfCanada.getActualManagementFee());
    return result;
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForEtfUs(final Holding etfHolding,
      final AverageMer averageMerEtfUs) {
    final var result = preBuildAverageMerDto(etfHolding);
    result.setNetExpenseRatio(averageMerEtfUs.getNetExpenseRatio());
    result.setGrossExpenseRatio(averageMerEtfUs.getGrossExpenseRatio());
    return result;
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForCanadaMutualFund(final Holding fundSeriesHolding,
      final AverageMer fundSeries) {
    final var result = preBuildAverageMerDto(fundSeriesHolding);
    result.setActualManagementFee(fundSeries.getActualManagementFee());
    result.setManagementExpenseRatio(fundSeries.getMer());
    return result;
  }

  @Override
  public void dataProviderCheckerForCanadaMutualFund(final List<DataProvider> providers,
      final Collection<AverageMer> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getMerProvider,
        AverageMer::setMer);
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getActualManagementFeeProvider,
        AverageMer::setActualManagementFee);
  }

  @Override
  public void dataProviderCheckerForEtfCanada(final List<DataProvider> providers,
      final Collection<AverageMer> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getMerProvider,
        AverageMer::setMer);
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getActualManagementFeeProvider,
        AverageMer::setActualManagementFee);
  }

  @Override
  public void dataProviderCheckerForEtfUs(final List<DataProvider> providers,
      final Collection<AverageMer> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getMerProvider,
        AverageMer::setMer);
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getActualManagementFeeProvider,
        AverageMer::setActualManagementFee);
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForUsMutualFund(final Holding holding,
      final AverageMer averageMerUsMutualFund) {
    final var result = preBuildAverageMerDto(holding);
    result.setNetExpenseRatio(averageMerUsMutualFund.getNetExpenseRatio());
    result.setGrossExpenseRatio(averageMerUsMutualFund.getGrossExpenseRatio());
    return result;
  }

  @Override
  public void dataProviderCheckerForUsMutualFund(final List<DataProvider> providers,
      final Collection<AverageMer> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getMerProvider,
        AverageMer::setMer);
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getActualManagementFeeProvider,
        AverageMer::setActualManagementFee);
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForCanadaHedgeFund(final Holding holding,
      final AverageMer averageMerCanadaHedgeFund) {
    final var result = preBuildAverageMerDto(holding);
    result.setActualManagementFee(averageMerCanadaHedgeFund.getActualManagementFee());
    result.setManagementExpenseRatio(averageMerCanadaHedgeFund.getMer());
    return result;
  }

  @Override
  public void dataProviderCheckerForCanadaHedgeFund(final List<DataProvider> providers,
      final Collection<AverageMer> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getMerProvider,
        AverageMer::setMer);
    dataProviderCheckValidation(providers, responseFromFds, AverageMer::getActualManagementFeeProvider,
        AverageMer::setActualManagementFee);
  }
}
