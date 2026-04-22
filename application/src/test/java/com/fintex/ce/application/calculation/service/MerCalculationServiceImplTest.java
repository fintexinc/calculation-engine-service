package com.fintex.ce.application.calculation.service;

import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.dto.command.AverageMerCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.TestConstants.DEFAULT_DATA_PROPERTIES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerCalculationServiceImplTest {

  @Mock
  private SecurityDataFetcher<FeeData> feesSecurityDataFetcher;

  @Test
  void shouldReturnMerMetric_whenGetMetricCalled() {
    var sut = new MERCalculationServiceImpl(feesSecurityDataFetcher, DEFAULT_DATA_PROPERTIES);

    assertThat(sut.getMetric()).isEqualTo(CalculationMetric.MER);
  }

  @Test
  void shouldMapCanadianEtfFeesToCalculationDto_whenHoldingIsCanadianEtf() {
    var sut = new MERCalculationServiceImpl(feesSecurityDataFetcher, DEFAULT_DATA_PROPERTIES);
    var identifier = new SecurityIdentifier("XBAL", FiIdentifierType.TICKER);
    var holding = new PortfolioHolding(BigDecimal.valueOf(50_000), FinancialInstrumentType.ETF_CANADA, identifier);
    var fees = new FeeData()
        .setManagementExpenseRatio(new BigDecimal("0.0225"))
        .setManagementFee(new BigDecimal("0.0125"));

    var dto = sut.mapFeeDataToCalculation(holding, fees);

    assertThat(dto.getHoldingType()).isEqualTo(FinancialInstrumentType.ETF_CANADA);
    assertThat(dto.getMarketValue()).isEqualByComparingTo("50000");
    assertThat(dto.getManagementExpenseRatio()).isEqualByComparingTo("0.0225");
    assertThat(dto.getActualManagementFee()).isEqualByComparingTo("0.0125");
    assertThat(dto.getNetExpenseRatio()).isNull();
    assertThat(dto.getGrossExpenseRatio()).isNull();
  }

  @Test
  void shouldMapUsMutualFundFeesToNetAndGrossExpenseRatios_whenHoldingIsUsMutualFund() {
    var sut = new MERCalculationServiceImpl(feesSecurityDataFetcher, DEFAULT_DATA_PROPERTIES);
    var identifier = new SecurityIdentifier("VTSAX", FiIdentifierType.TICKER);
    var holding = new PortfolioHolding(BigDecimal.valueOf(100_000), FinancialInstrumentType.MUTUAL_FUND_US, identifier);
    var fees = new FeeData()
        .setNetExpenseRatio(new BigDecimal("0.0400"))
        .setGrossExpenseRatio(new BigDecimal("0.0450"))
        .setManagementExpenseRatio(new BigDecimal("0.99"))
        .setManagementFee(new BigDecimal("0.99"));

    var dto = sut.mapFeeDataToCalculation(holding, fees);

    assertThat(dto.getHoldingType()).isEqualTo(FinancialInstrumentType.MUTUAL_FUND_US);
    assertThat(dto.getNetExpenseRatio()).isEqualByComparingTo("0.0400");
    assertThat(dto.getGrossExpenseRatio()).isEqualByComparingTo("0.0450");
    assertThat(dto.getManagementExpenseRatio()).isNull();
    assertThat(dto.getActualManagementFee()).isNull();
  }

  @Test
  void shouldFetchFeesFromSecurityMasterAndGroupByHoldingType_whenFetchDataCalled() {
    var sut = new MERCalculationServiceImpl(feesSecurityDataFetcher, DEFAULT_DATA_PROPERTIES);
    var identifier = new SecurityIdentifier("XBAL", FiIdentifierType.TICKER);
    var holding = new PortfolioHolding(BigDecimal.valueOf(50_000), FinancialInstrumentType.ETF_CANADA, identifier);
    var feeData = new FeeData()
        .setManagementExpenseRatio(new BigDecimal("0.02"))
        .setManagementFee(new BigDecimal("0.01"));
    when(feesSecurityDataFetcher.fetch(List.of(holding), List.of(DataProvider.MORNINGSTAR)))
        .thenReturn(Map.of(holding, feeData));

    var command = new AverageMerCommand();
    command.setHoldings(List.of(holding));
    command.setDataProviders(List.of(DataProvider.MORNINGSTAR));

    Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> grouped = sut.fetchData(
        command);

    verify(feesSecurityDataFetcher).fetch(List.of(holding), List.of(DataProvider.MORNINGSTAR));
    assertThat(grouped).containsKey(FinancialInstrumentType.ETF_CANADA);
    assertThat(grouped.get(FinancialInstrumentType.ETF_CANADA)).isNotNull();
    assertThat(grouped.get(FinancialInstrumentType.ETF_CANADA)).containsKey(holding);
    var calc = grouped.get(FinancialInstrumentType.ETF_CANADA).get(holding);
    assertThat(calc.getManagementExpenseRatio()).isEqualByComparingTo("0.02");
    assertThat(calc.getActualManagementFee()).isEqualByComparingTo("0.01");
  }
}
