package com.fintex.ce.adapter.webclient.mapper;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.domain.model.BusinessCountry;
import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.CommonHoldings;
import com.fintex.ce.domain.model.CommonHoldingsDTO;
import com.fintex.ce.domain.model.CountryExposure;
import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.EquityCountryAllocation;
import com.fintex.ce.domain.model.EquityMarketCapitalization;
import com.fintex.ce.domain.model.EquitySector;
import com.fintex.ce.domain.model.EquityStyleboxExposure;
import com.fintex.ce.domain.model.FixedIncomeBondSecurities;
import com.fintex.ce.domain.model.FixedIncomeStyleboxExposure;
import com.fintex.ce.domain.model.IncomeForecast;
import com.fintex.ce.domain.model.ManagementFee;
import com.fintex.ce.domain.model.MaturityAllocation;
import com.fintex.ce.domain.model.SalesCharge;
import com.fintex.ce.domain.model.Yield;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.util.JacksonUtil;
import com.fintex.sm.model.domain.allocation.CountryAllocation;
import com.fintex.sm.model.domain.allocation.EquitySectorAllocation;
import com.fintex.sm.model.domain.allocation.FixedIncomeSecuritiesAllocation;
import com.fintex.sm.model.domain.allocation.SectorAllocation;
import com.fintex.sm.model.domain.datapoint.Fees;
import com.fintex.sm.model.domain.datapoint.Income;
import com.fintex.sm.model.domain.datapoint.Maturities;
import com.fintex.sm.model.domain.datapoint.SalesChargeData;
import com.fintex.sm.model.domain.holding.Holdings;
import com.fintex.sm.model.domain.holding.SecurityHolding;
import com.fintex.sm.model.domain.performance.MonthlyReturns;
import com.fintex.sm.model.domain.rating.CreditQualityRatings;
import com.fintex.sm.model.domain.rating.FixedIncomeStyleBoxes;
import com.fintex.sm.model.domain.rating.StyleBoxes;
import com.fintex.sm.model.domain.security.Stock;
import com.fintex.sm.model.domain.value.CountryValue;
import com.fintex.sm.model.domain.value.CreditQualityRatingTypeValue;
import com.fintex.sm.model.domain.value.DateBigDecimalValue;
import com.fintex.sm.model.domain.value.IdentifierTypeValue;
import com.fintex.sm.model.domain.value.MultilingualString;
import com.fintex.sm.model.domain.value.NameValue;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.CollectorUtils.toTreeMap;
import static com.fintex.ce.util.DateTimeUtils.toLastDayOfMonth;
import static com.fintex.sm.model.domain.enumeration.LanguageCode.EN;

public final class SmResponseMappers {

  private SmResponseMappers() {
  }

  public static com.fintex.ce.domain.model.AssetAllocation mapAssetAllocation(
      com.fintex.sm.model.domain.allocation.AssetAllocation smResponse, Holding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smResponse)
        .map(com.fintex.sm.model.domain.allocation.AssetAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));
    com.fintex.ce.domain.model.AssetAllocation result = new com.fintex.ce.domain.model.AssetAllocation(holding.getType(), allocationMap);
    Optional.ofNullable(smResponse)
        .map(r -> r.getDataProvider())
        .ifPresent(dp -> result.setProvider(DataProvider.of(dp.name()).name()));
    return result;
  }

  public static EquitySector mapEquitySector(EquitySectorAllocation smResponse, Holding holding) {
    if (smResponse == null || smResponse.getAllocation() == null || smResponse.getAllocation().isEmpty()) {
      EquitySector equitySector = new EquitySector();
      equitySector.setAllocations(new HashMap<>());
      return equitySector;
    }
    Map<String, BigDecimal> sectors = smResponse.getAllocation().stream()
        .filter(e -> e != null && e.getNames() != null
            && e.getNames().stream().anyMatch(lang -> EN.equals(lang.getLanguageCode())))
        .collect(toMap(
            e -> e.getNames().stream()
                .filter(lang -> EN.equals(lang.getLanguageCode()))
                .findFirst().orElseThrow().getValue(),
            e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    EquitySector result = new EquitySector(sectors);
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static FixedIncomeBondSecurities mapFixedIncomeBondSector(
      FixedIncomeSecuritiesAllocation smResponse, Holding holding) {
    Map<String, BigDecimal> allocationMap = Optional.ofNullable(smResponse)
        .map(FixedIncomeSecuritiesAllocation::getAllocation)
        .orElse(List.of())
        .stream()
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));
    FixedIncomeBondSecurities result = new FixedIncomeBondSecurities(holding.getType(), allocationMap);
    Optional.ofNullable(smResponse)
        .map(r -> r.getDataProvider())
        .ifPresent(dp -> result.setProvider(DataProvider.of(dp.name()).name()));
    return result;
  }

  public static EquityStyleboxExposure mapEquityStylebox(StyleBoxes smResponse, Holding holding) {
    EquityStyleboxExposure result = new EquityStyleboxExposure();
    result.setHoldingType(holding.getType());
    if (smResponse == null || smResponse.getBoxValues() == null || smResponse.getBoxValues().isEmpty()) {
      result.setBoxValues(new HashMap<>());
      return result;
    }
    Map<String, BigDecimal> boxValues = smResponse.getBoxValues().stream()
        .filter(e -> e.getStyleBoxType() != null)
        .collect(toMap(
            e -> e.getStyleBoxType().name(),
            e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    result.setBoxValues(boxValues);
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static FixedIncomeStyleboxExposure mapFixedIncomeStylebox(
      FixedIncomeStyleBoxes smResponse, Holding holding) {
    FixedIncomeStyleboxExposure result = new FixedIncomeStyleboxExposure();
    result.setHoldingType(holding.getType());
    if (smResponse == null || smResponse.getBoxValues() == null || smResponse.getBoxValues().isEmpty()) {
      result.setBoxValues(new HashMap<>());
      return result;
    }
    Map<String, BigDecimal> boxValues = smResponse.getBoxValues().stream()
        .filter(e -> e.getStyleBoxType() != null)
        .collect(toMap(
            e -> e.getStyleBoxType().name(),
            e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    result.setBoxValues(boxValues);
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static com.fintex.ce.domain.model.EquityMarketCapitalization mapEquityMarketCap(
      com.fintex.sm.model.domain.datapoint.EquityMarketCapitalization smResponse, Holding holding) {
    if (smResponse == null || smResponse.getValues() == null || smResponse.getValues().isEmpty()) {
      return new com.fintex.ce.domain.model.EquityMarketCapitalization();
    }
    Map<String, BigDecimal> ratings = smResponse.getValues().stream()
        .filter(e -> e.getEquityMarketCapitalization() != null)
        .collect(toMap(
            e -> e.getEquityMarketCapitalization().name(),
            e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    com.fintex.ce.domain.model.EquityMarketCapitalization result =
        new com.fintex.ce.domain.model.EquityMarketCapitalization(ratings);
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static CountryExposure mapCountryExposure(CountryAllocation smResponse, Holding holding) {
    Map<String, BigDecimal> allocations;
    if (smResponse == null || smResponse.getAllocation() == null) {
      allocations = new HashMap<>();
    } else {
      allocations = smResponse.getAllocation().stream()
          .filter(e -> !StringUtils.isBlank(e.getIsoCode()))
          .collect(toMap(
              CountryValue::getIsoCode,
              e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    }
    CountryExposure result = new CountryExposure(holding.getType(), allocations);
    if (smResponse != null && smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static EquityCountryAllocation mapEquityCountryAllocation(
      CountryAllocation smResponse, Holding holding) {
    EquityCountryAllocation result = new EquityCountryAllocation();
    result.setHoldingType(holding.getType());
    if (smResponse == null || smResponse.getAllocation() == null) {
      result.setAllocations(new HashMap<>());
      return result;
    }
    Map<String, BigDecimal> allocations = smResponse.getAllocation().stream()
        .filter(e -> e != null && e.getName() != null
            && e.getName().stream().anyMatch(lang -> EN.equals(lang.getLanguageCode())))
        .collect(toMap(
            e -> e.getName().stream()
                .filter(lang -> EN.equals(lang.getLanguageCode()))
                .findFirst().orElseThrow().getValue(),
            CountryValue::getValue));
    result.setAllocations(allocations);
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static ClassificationAllocation mapClassificationAllocation(
      SectorAllocation smResponse, Holding holding) {
    ClassificationAllocation result = new ClassificationAllocation();
    result.setHoldingType(holding.getType());
    if (smResponse == null || smResponse.getAllocation() == null) {
      result.setSecurityClassificationValues(new HashMap<>());
      return result;
    }
    Map<String, BigDecimal> allocations = smResponse.getAllocation().stream()
        .filter(e -> e != null && e.getName() != null)
        .collect(Collectors.toMap(NameValue::getName, NameValue::getValue));
    result.setSecurityClassificationValues(allocations);
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static CreditQuality mapCreditQuality(CreditQualityRatings smResponse, Holding holding) {
    Map<String, BigDecimal> ratings;
    if (smResponse == null || smResponse.getRatings() == null) {
      ratings = new HashMap<>();
    } else {
      ratings = smResponse.getRatings().stream()
          .filter(e -> e != null && !StringUtils.isBlank(e.getRating()))
          .collect(toMap(
              CreditQualityRatingTypeValue::getRating,
              e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    }
    CreditQuality result = new CreditQuality(holding.getType(), ratings);
    if (smResponse != null && smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static AverageMer mapAverageMer(Fees smResponse, Holding holding) {
    AverageMer result = new AverageMer();
    if (smResponse == null) {
      return result;
    }
    if (smResponse.getManagementExpenseRatio() != null) {
      result.setMer(smResponse.getManagementExpenseRatio().getValue());
      if (smResponse.getManagementExpenseRatio().getDataProvider() != null) {
        result.setMerProvider(smResponse.getManagementExpenseRatio().getDataProvider().name());
      }
    }
    if (smResponse.getManagementFee() != null) {
      result.setActualManagementFee(smResponse.getManagementFee().getValue());
      if (smResponse.getManagementFee().getDataProvider() != null) {
        result.setActualManagementFeeProvider(smResponse.getManagementFee().getDataProvider().name());
      }
    }
    if (smResponse.getNetExpenseRatio() != null) {
      result.setNetExpenseRatio(smResponse.getNetExpenseRatio().getValue());
      if (smResponse.getNetExpenseRatio().getDataProvider() != null) {
        result.setNetExpenseRatioProvider(smResponse.getNetExpenseRatio().getDataProvider().name());
      }
    }
    if (smResponse.getGrossExpenseRatio() != null) {
      result.setGrossExpenseRatio(smResponse.getGrossExpenseRatio().getValue());
      if (smResponse.getGrossExpenseRatio().getDataProvider() != null) {
        result.setGrossExpenseRatioProvider(smResponse.getGrossExpenseRatio().getDataProvider().name());
      }
    }
    return result;
  }

  public static ManagementFee mapManagementFee(Fees smResponse, Holding holding) {
    ManagementFee result = new ManagementFee();
    if (smResponse != null && smResponse.getManagementFee() != null) {
      result.setManagementFee(smResponse.getManagementFee().getValue());
      if (smResponse.getManagementFee().getDataProvider() != null) {
        result.setProvider(smResponse.getManagementFee().getDataProvider().name());
      }
    }
    return result;
  }

  public static CommonHoldings mapCommonHoldings(Holdings smResponse, Holding holding) {
    if (smResponse == null || smResponse.getAllocation() == null || smResponse.getAllocation().isEmpty()) {
      return new CommonHoldings();
    }
    List<CommonHoldingsDTO> commonHoldings = mapSecurityHoldings(smResponse.getAllocation());
    CommonHoldings result = new CommonHoldings(JacksonUtil.serialize(commonHoldings));
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  private static List<CommonHoldingsDTO> mapSecurityHoldings(
      List<SecurityHolding> holdings) {
    if (holdings == null) {
      return List.of();
    }
    List<CommonHoldingsDTO> result = new ArrayList<>();
    for (SecurityHolding sh : holdings) {
      com.fintex.sm.model.domain.holding.Holding h = sh;
      if (h.getName() == null || h.getName().isEmpty()) {
        continue;
      }
      String name = h.getName().stream()
          .filter(lang -> EN.equals(lang.getLanguageCode()))
          .findFirst()
          .map(MultilingualString::getValue)
          .orElse(null);
      if (name == null && h.getCompanyName() == null) {
        continue;
      }

      String ticker = extractIdentifier(h, com.fintex.sm.model.domain.enumeration.FiIdentifierType.TICKER);
      String exchangeCode = extractIdentifier(h, com.fintex.sm.model.domain.enumeration.FiIdentifierType.EXCHANGE_ID);

      CommonHoldingsDTO dto = new CommonHoldingsDTO()
          .setName(name)
          .setCompanyName(h.getCompanyName())
          .setType(h.getType())
          .setValue(h.getWeighting())
          .setTicker(ticker)
          .setExchangeCode(exchangeCode);

      result.add(dto);
    }
    return result;
  }

  private static String extractIdentifier(
      com.fintex.sm.model.domain.holding.Holding holding,
      com.fintex.sm.model.domain.enumeration.FiIdentifierType type) {
    if (holding.getExternalIdentifiers() == null || holding.getExternalIdentifiers().getCodes() == null) {
      return null;
    }
    return holding.getExternalIdentifiers().getCodes().stream()
        .filter(e -> type.equals(e.getType()))
        .map(IdentifierTypeValue::getValue)
        .findFirst()
        .orElse(null);
  }

  public static IncomeForecast mapIncomeForecast(Income smResponse, Holding holding) {
    IncomeForecast result = new IncomeForecast();
    if (smResponse == null) {
      return result;
    }
    if (smResponse.getDividendYield() != null) {
      result.setDividendYield(smResponse.getDividendYield().getValue());
    }
    if (smResponse.getDistributionDates() != null) {
      result.setSchedule(smResponse.getDistributionDates().getValues());
    }
    return result;
  }

  public static Yield mapYield(Income smResponse, Holding holding) {
    Yield result = new Yield();
    if (smResponse != null && smResponse.getDividendYield() != null) {
      result.setDividendYield(smResponse.getDividendYield().getValue());
    }
    return result;
  }

  public static MaturityAllocation mapMaturityAllocation(Maturities smResponse, Holding holding) {
    MaturityAllocation result = new MaturityAllocation();
    result.setHoldingType(holding.getType());
    if (smResponse == null || smResponse.getPeriods() == null || smResponse.getPeriods().isEmpty()) {
      result.setMaturityDurationValues(new HashMap<>());
      return result;
    }
    Map<String, BigDecimal> values = smResponse.getPeriods().stream()
        .filter(e -> e.getMaturityDuration() != null)
        .collect(toMap(
            e -> e.getMaturityDuration().name(),
            e -> e.getValue() == null ? BigDecimal.ZERO : e.getValue()));
    result.setMaturityDurationValues(values);
    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  public static com.fintex.ce.domain.model.MonthlyReturns mapMonthlyReturns(
      MonthlyReturns smResponse, Holding holding) {
    com.fintex.ce.domain.model.MonthlyReturns result = new com.fintex.ce.domain.model.MonthlyReturns()
        .setHoldingType(holding.getType());

    if (smResponse == null || smResponse.getReturns() == null || smResponse.getReturns().isEmpty()) {
      return result;
    }

    TreeMap<LocalDate, BigDecimal> returns = smResponse.getReturns().stream()
        .collect(toTreeMap(
            dbv -> parseMonthDate(dbv.getDate()),
            DateBigDecimalValue::getValue));
    result.setReturns(returns);

    if (smResponse.getDataProvider() != null) {
      result.setProvider(smResponse.getDataProvider().name());
    }
    return result;
  }

  private static LocalDate parseMonthDate(String dateStr) {
    String[] split = dateStr.split("-");
    LocalDate date = LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), 1);
    return toLastDayOfMonth(date);
  }

  public static SalesCharge mapSalesCharge(SalesChargeData smResponse, Holding holding) {
    SalesCharge result = new SalesCharge();
    if (smResponse != null && smResponse.getSalesCharge() != null
        && smResponse.getSalesCharge().getType() != null) {
      result.setValue(smResponse.getSalesCharge().getType().name());
    }
    return result;
  }

  public static BusinessCountry mapBusinessCountry(Stock stock, Holding holding) {
    BusinessCountry result = new BusinessCountry();
    if (stock != null && stock.getBusinessCountry() != null) {
      result.setValue(stock.getBusinessCountry().getValue());
    }
    return result;
  }
}
