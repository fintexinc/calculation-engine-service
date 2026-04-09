package com.fintex.ce.application.calculation.metric;

import com.fintex.ce.application.util.ComparisonUtils;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeSectorResult;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.enumeration.FixedIncomeSecuritiesAllocationType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class FixedIncomeBondSectorCalculationTest {

  @Test
  void shouldCalculateFixedIncomeSector_whenPortfolioContainsOnlyAom() {
    final Holding aom = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.ETF_US,
        new SecurityIdentifier("AOM", FiIdentifierType.TICKER));

    Map<Holding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> exposures = new HashMap<>();
    final HashMap<FixedIncomeSecuritiesAllocationType, BigDecimal> fixedIncomeSectorTypes = getFixedIncomeSecuritiesAllocationTypeOfAOM();
    exposures.put(aom, fixedIncomeSectorTypes);

    Map<Holding, BigDecimal> fixedIncomePlusCash = Map.of(aom, BigDecimal.valueOf(0.6081876));

    var sut = new FixedIncomeBondSectorCalculation(exposures, List.of(aom), List.of(), fixedIncomePlusCash);

    FixedIncomeSectorResult expected = getExpectedOfAom();

    final FixedIncomeSectorResult actual = sut.calculate();

    ComparisonUtils.compareMaps(expected.getFixedIncomeSector(), actual.getFixedIncomeSector());
    Assertions.assertEquals(expected.getWarnings(), actual.getWarnings());
  }

  private FixedIncomeSectorResult getExpectedOfAom() {
    final HashMap<FixedIncomeSecuritiesAllocationType, BigDecimal> expectedResult = new HashMap<>();
    expectedResult.put(FixedIncomeSecuritiesAllocationType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(
        0.1410290846));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.OTHER_BONDS, BigDecimal.valueOf(0.0000935047));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(0.2849409274));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.ST_INVESTMENTS, BigDecimal.valueOf(0.1345309217));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(0.0026411354));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.4367644262));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.SECURITIZED_DEBT, BigDecimal.valueOf(0.0000000000));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.DIRECT_MORTGAGES, BigDecimal.valueOf(0.0000000000));
    var expected = new FixedIncomeSectorResult();
    expected.setFixedIncomeSector(expectedResult);
    expected.setWarnings(List.of());
    return expected;
  }

  private HashMap<FixedIncomeSecuritiesAllocationType, BigDecimal> getFixedIncomeSecuritiesAllocationTypeOfAOM() {
    final HashMap<FixedIncomeSecuritiesAllocationType, BigDecimal> fixedIncomeSectorTypes = new HashMap<>();
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(
        0.1401155072));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.OTHER_BONDS, BigDecimal.valueOf(0.000092899));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(0.2830950982));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.ST_INVESTMENTS, BigDecimal.valueOf(0.1336594389));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(
        0.0026240263));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.433935094));
    return fixedIncomeSectorTypes;
  }

  private HashMap<FixedIncomeSecuritiesAllocationType, BigDecimal> getFixedIncomeSecuritiesAllocationTypeOfRBF605() {
    final HashMap<FixedIncomeSecuritiesAllocationType, BigDecimal> fixedIncomeSectorTypes = new HashMap<>();
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(0.0));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.OTHER_BONDS, BigDecimal.valueOf(0.021217966381923));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(
        0.206117387710113));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.ST_INVESTMENTS, BigDecimal.valueOf(
        0.06585836318545));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(0.0));
    fixedIncomeSectorTypes.put(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(
        0.706806282722513));
    return fixedIncomeSectorTypes;
  }

  @Test
  void shouldCalculateFixedIncomeSector_whenPortfolioContainsAomAndRbf605() {
    final Holding aom = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.ETF_US,
        new SecurityIdentifier("AOM", FiIdentifierType.TICKER));
    final Holding rbf605 = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("RBF605", FiIdentifierType.FUNDSERV));

    Map<Holding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> exposures = new HashMap<>();
    exposures.put(rbf605, getFixedIncomeSecuritiesAllocationTypeOfRBF605());
    exposures.put(aom, getFixedIncomeSecuritiesAllocationTypeOfAOM());

    Map<Holding, BigDecimal> fixedIncomePlusCash = Map.of(aom, BigDecimal.valueOf(0.6081876), rbf605, BigDecimal
        .valueOf(0.3489427));

    var sut = new FixedIncomeBondSectorCalculation(exposures, List.of(aom, rbf605), List.of(), fixedIncomePlusCash);

    FixedIncomeSectorResult expected = getExpectedOfAomAndRbf605();

    final FixedIncomeSectorResult actual = sut.calculate();

    ComparisonUtils.compareMaps(expected.getFixedIncomeSector(), actual.getFixedIncomeSector());
    Assertions.assertEquals(expected.getWarnings(), actual.getWarnings());
  }

  @Test
  void shouldCalculateFixedIncomeSectorFromAomOnly_whenRbf605FixedIncomePlusCashIsZero() {
    final Holding aom = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.ETF_US,
        new SecurityIdentifier("AOM", FiIdentifierType.TICKER));
    final Holding rbf605 = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.MUTUAL_FUND_CANADA,
        new SecurityIdentifier("RBF605", FiIdentifierType.FUNDSERV));

    Map<Holding, Map<FixedIncomeSecuritiesAllocationType, BigDecimal>> exposures = new HashMap<>();
    exposures.put(rbf605, getFixedIncomeSecuritiesAllocationTypeOfRBF605());
    exposures.put(aom, getFixedIncomeSecuritiesAllocationTypeOfAOM());

    Map<Holding, BigDecimal> fixedIncomePlusCash = Map.of(aom, BigDecimal.valueOf(0.6081876), rbf605, BigDecimal
        .valueOf(0.0));

    var sut = new FixedIncomeBondSectorCalculation(exposures, List.of(aom, rbf605), List.of(), fixedIncomePlusCash);

    FixedIncomeSectorResult expected = getExpectedOfAom();

    final FixedIncomeSectorResult actual = sut.calculate();

    ComparisonUtils.compareMaps(expected.getFixedIncomeSector(), actual.getFixedIncomeSector());
    Assertions.assertEquals(expected.getWarnings(), actual.getWarnings());
  }

  private FixedIncomeSectorResult getExpectedOfAomAndRbf605() {
    final HashMap<FixedIncomeSecuritiesAllocationType, BigDecimal> expectedResult = new HashMap<>();
    expectedResult.put(FixedIncomeSecuritiesAllocationType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(
        0.0894013464));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.OTHER_BONDS, BigDecimal.valueOf(0.0078267193));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS, BigDecimal.valueOf(0.2560853117));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.ST_INVESTMENTS, BigDecimal.valueOf(0.1093913635));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(0.0016742721));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.535620987));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.SECURITIZED_DEBT, BigDecimal.valueOf(0.0000000000));
    expectedResult.put(FixedIncomeSecuritiesAllocationType.DIRECT_MORTGAGES, BigDecimal.valueOf(0.0000000000));
    var result = new FixedIncomeSectorResult();
    result.setFixedIncomeSector(expectedResult);
    result.setWarnings(List.of());
    return result;
  }
}
