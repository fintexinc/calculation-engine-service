package com.fintex.ce.adapter.cache;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.domain.model.AverageManagementExpenseCalculationDTO;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.cache.entity.managementfee.RManagementFee;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import com.fintex.ce.port.output.graphql.MultipleSMRepository;
import com.fintex.ce.adapter.cache.repository.managementfee.ManagementFeeRepository;
import com.fintex.ce.adapter.cache.statistic.CacheStatisticService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.fintex.ce.constant.CacheNameEntity.MANAGEMENT_FEE;
import static com.fintex.ce.util.validation.DataProviderRequestHandlingValidator.dataProviderCheckValidation;

@Service
public class ManagementFeeCacheStorage
    extends
      ManagementExpenseAbstractCacheStorage<ManagementFee, ManagementFee, ManagementFee, ManagementFee, RManagementFee> {

  public ManagementFeeCacheStorage(
      final MultipleSMRepository<ManagementFee, ManagementFee, ManagementFee, ManagementFee> smRepo,
      final CacheEntityMapper<ManagementFee, RManagementFee> mapper,
      final ManagementFeeRepository fundCanadaRepo,
      final ManagementFeeRepository etfCanadaRepo,
      final ManagementFeeRepository etfUsRepo,
      final CacheStatisticService cacheStatisticService) {
    super(smRepo, mapper, mapper, mapper, mapper, fundCanadaRepo, etfCanadaRepo, etfUsRepo, cacheStatisticService,
        MANAGEMENT_FEE);
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForCanadaMutualFund(Holding fundSeriesHolding,
      ManagementFee fundSeries) {
    var result = preBuildAverageMerDto(fundSeriesHolding);
    result.setActualManagementFee(fundSeries.getManagementFee());
    return result;
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForEtfCanada(Holding etfHolding,
      ManagementFee averageMerEtfCanada) {
    var result = preBuildAverageMerDto(etfHolding);
    result.setActualManagementFee(averageMerEtfCanada.getManagementFee());
    return result;
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForEtfUs(Holding etfHolding, ManagementFee averageMerEtfUs) {
    var result = preBuildAverageMerDto(etfHolding);
    result.setActualManagementFee(averageMerEtfUs.getManagementFee());
    return result;
  }

  @Override
  public void dataProviderCheckerForCanadaMutualFund(List<DataProvider> providers,
      Collection<ManagementFee> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, ManagementFee::setManagementFee);
  }

  @Override
  public void dataProviderCheckerForEtfCanada(List<DataProvider> providers,
      Collection<ManagementFee> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, ManagementFee::setManagementFee);
  }

  @Override
  public void dataProviderCheckerForEtfUs(List<DataProvider> providers,
      Collection<ManagementFee> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, ManagementFee::setManagementFee);
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForUsMutualFund(final Holding holding,
      final ManagementFee managementFeeUsFund) {
    var result = preBuildAverageMerDto(holding);
    result.setActualManagementFee(managementFeeUsFund.getManagementFee());
    return result;
  }

  @Override
  public void dataProviderCheckerForUsMutualFund(final List<DataProvider> providers,
      final Collection<ManagementFee> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, ManagementFee::setManagementFee);
  }

  @Override
  public AverageManagementExpenseCalculationDTO mapperForCanadaHedgeFund(final Holding holding,
      final ManagementFee managementFeeCanadaHedgeFund) {
    var result = preBuildAverageMerDto(holding);
    result.setActualManagementFee(managementFeeCanadaHedgeFund.getManagementFee());
    return result;
  }

  @Override
  public void dataProviderCheckerForCanadaHedgeFund(final List<DataProvider> providers,
      final Collection<ManagementFee> responseFromFds) {
    dataProviderCheckValidation(providers, responseFromFds, ManagementFee::setManagementFee);
  }
}
