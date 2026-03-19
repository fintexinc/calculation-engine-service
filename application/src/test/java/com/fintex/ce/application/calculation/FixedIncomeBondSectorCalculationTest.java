package com.fintex.ce.application.calculation;

import com.fintex.ce.domain.model.calculation.FixedIncomeSectorType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.result.FixedIncomeSectorResult;
import com.fintex.ce.util.ComparisonUtils;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FixedIncomeBondSectorCalculationTest {

  @Test
  void shouldCalculateFixedIncomeSector_whenPortfolioContainsOnlyAom() {
    final Holding aom = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.ETF_US)
        .setSecurityIdentifier(new SecurityIdentifier("AOM", FiIdentifierType.TICKER));

    Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> exposures = new HashMap<>();
    final HashMap<FixedIncomeSectorType, BigDecimal> fixedIncomeSectorTypes = getFixedIncomeSectorTypeOfAOM();
    exposures.put(aom, fixedIncomeSectorTypes);

    Map<Holding, BigDecimal> fixedIncomePlusCash = Map.of(aom, BigDecimal.valueOf(0.6081876));

    var sut = new FixedIncomeBondSectorCalculation(exposures, List.of(aom), List.of(), fixedIncomePlusCash);

    FixedIncomeSectorResult expected = getExpectedOfAom();

    final FixedIncomeSectorResult actual = sut.calculate();

    ComparisonUtils.compareMaps(expected.getFixedIncomeSector(), actual.getFixedIncomeSector());
    Assertions.assertEquals(expected.getWarnings(), actual.getWarnings());
  }

  private FixedIncomeSectorResult getExpectedOfAom() {
    final HashMap<FixedIncomeSectorType, BigDecimal> expectedResult = new HashMap<>();
    expectedResult.put(FixedIncomeSectorType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(0.1410290846));
    expectedResult.put(FixedIncomeSectorType.OTHER_BONDS, BigDecimal.valueOf(0.0000935047));
    expectedResult.put(FixedIncomeSectorType.CORPORATE_BONDS, BigDecimal.valueOf(0.2849409274));
    expectedResult.put(FixedIncomeSectorType.ST_INVESTMENTS, BigDecimal.valueOf(0.1345309217));
    expectedResult.put(FixedIncomeSectorType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(0.0026411354));
    expectedResult.put(FixedIncomeSectorType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.4367644262));
    var expected = new FixedIncomeSectorResult();
    expected.setFixedIncomeSector(expectedResult);
    expected.setWarnings(List.of());
    return expected;
  }

  private HashMap<FixedIncomeSectorType, BigDecimal> getFixedIncomeSectorTypeOfAOM() {
    final HashMap<FixedIncomeSectorType, BigDecimal> fixedIncomeSectorTypes = new HashMap<>();
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(0.1401155072));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.OTHER_BONDS, BigDecimal.valueOf(0.000092899));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.CORPORATE_BONDS, BigDecimal.valueOf(0.2830950982));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.ST_INVESTMENTS, BigDecimal.valueOf(0.1336594389));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(0.0026240263));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.433935094));
    return fixedIncomeSectorTypes;
  }

  private HashMap<FixedIncomeSectorType, BigDecimal> getFixedIncomeSectorTypeOfRBF605() {
    final HashMap<FixedIncomeSectorType, BigDecimal> fixedIncomeSectorTypes = new HashMap<>();
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(0.0));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.OTHER_BONDS, BigDecimal.valueOf(0.021217966381923));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.CORPORATE_BONDS, BigDecimal.valueOf(0.206117387710113));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.ST_INVESTMENTS, BigDecimal.valueOf(0.06585836318545));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(0.0));
    fixedIncomeSectorTypes.put(FixedIncomeSectorType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.706806282722513));
    return fixedIncomeSectorTypes;
  }

  @Test
  void shouldCalculateFixedIncomeSector_whenPortfolioContainsAomAndRbf605() {
    final Holding aom = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.ETF_US)
        .setSecurityIdentifier(new SecurityIdentifier("AOM", FiIdentifierType.TICKER));
    final Holding rbf605 = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.MUTUAL_FUND_CANADA)
        .setSecurityIdentifier(new SecurityIdentifier("RBF605", FiIdentifierType.FUNDSERV));

    Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> exposures = new HashMap<>();
    exposures.put(rbf605, getFixedIncomeSectorTypeOfRBF605());
    exposures.put(aom, getFixedIncomeSectorTypeOfAOM());

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
    final Holding aom = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.ETF_US)
        .setSecurityIdentifier(new SecurityIdentifier("AOM", FiIdentifierType.TICKER));
    final Holding rbf605 = new Holding(BigDecimal.valueOf(50), FinancialInstrumentType.MUTUAL_FUND_CANADA)
        .setSecurityIdentifier(new SecurityIdentifier("RBF605", FiIdentifierType.FUNDSERV));

    Map<Holding, Map<FixedIncomeSectorType, BigDecimal>> exposures = new HashMap<>();
    exposures.put(rbf605, getFixedIncomeSectorTypeOfRBF605());
    exposures.put(aom, getFixedIncomeSectorTypeOfAOM());

    Map<Holding, BigDecimal> fixedIncomePlusCash = Map.of(aom, BigDecimal.valueOf(0.6081876), rbf605, BigDecimal
        .valueOf(0.0));

    var sut = new FixedIncomeBondSectorCalculation(exposures, List.of(aom, rbf605), List.of(), fixedIncomePlusCash);

    FixedIncomeSectorResult expected = getExpectedOfAom();

    final FixedIncomeSectorResult actual = sut.calculate();

    ComparisonUtils.compareMaps(expected.getFixedIncomeSector(), actual.getFixedIncomeSector());
    Assertions.assertEquals(expected.getWarnings(), actual.getWarnings());
  }

  private FixedIncomeSectorResult getExpectedOfAomAndRbf605() {
    final HashMap<FixedIncomeSectorType, BigDecimal> expectedResult = new HashMap<>();
    expectedResult.put(FixedIncomeSectorType.MORTGAGE_BACKED_SECURITIES, BigDecimal.valueOf(0.0894013464));
    expectedResult.put(FixedIncomeSectorType.OTHER_BONDS, BigDecimal.valueOf(0.0078267193));
    expectedResult.put(FixedIncomeSectorType.CORPORATE_BONDS, BigDecimal.valueOf(0.2560853117));
    expectedResult.put(FixedIncomeSectorType.ST_INVESTMENTS, BigDecimal.valueOf(0.1093913635));
    expectedResult.put(FixedIncomeSectorType.ASSET_BACKED_SECURITIES, BigDecimal.valueOf(0.0016742721));
    expectedResult.put(FixedIncomeSectorType.GOVERNMENT_BONDS, BigDecimal.valueOf(0.535620987));
    var result = new FixedIncomeSectorResult();
    result.setFixedIncomeSector(expectedResult);
    result.setWarnings(List.of());
    return result;
  }
}
