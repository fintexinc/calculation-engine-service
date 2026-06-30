package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.mapping.response.FixedIncomeSectorResponseMapper;
import com.fintex.ce.application.util.AllocationMappingUtils;
import com.fintex.ce.application.util.ExposureDataHolder;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.enumeration.CalculationMetric;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.allocation.FixedIncomeSectorResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.port.webclient.sm.SecurityDataFetcher;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fintex.ce.model.error.ErrorCode.MISSING_FIXED_INCOME_BOND_SECTOR;
import static java.math.BigDecimal.ZERO;

@Service
public class FixedIncomeBondSectorService
    extends
      BreakdownAbstractService<FixedIncomeSectorResult, FixedIncomeSecuritiesAllocationType> {

  static final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> ALLOCATION_DEFAULT_MAP = Collections
      .unmodifiableMap(
          Stream.of(FixedIncomeSecuritiesAllocationType.values())
              .collect(Collectors.toMap(type -> type, type -> ZERO)));

  private final SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSectorSecurityDataFetcher;
  private final FixedIncomeSectorResponseMapper responseMapper;

  public FixedIncomeBondSectorService(
      final SecurityDataFetcher<FixedIncomeBondSecurities> fixedIncomeBondSectorSecurityDataFetcher,
      final FixedIncomeSectorResponseMapper responseMapper) {
    super();
    this.fixedIncomeBondSectorSecurityDataFetcher = fixedIncomeBondSectorSecurityDataFetcher;
    this.responseMapper = responseMapper;
  }

  @Override
  public CalculationMetric getMetric() {
    return CalculationMetric.FIXED_INCOME_BOND_SECTOR;
  }

  @Override
  public ExposureDataHolder<FixedIncomeSecuritiesAllocationType> fetchExposures(
      final PortfolioHoldingsCommand command) {
    final Map<PortfolioHolding, FixedIncomeBondSecurities> rawData = fixedIncomeBondSectorSecurityDataFetcher.fetch(
        command.getHoldings(), command.getDataProviders());
    return AllocationMappingUtils.mapTypedAllocations(rawData,
        FixedIncomeBondSecurities::getFixedIncomeBondSectors,
        ALLOCATION_DEFAULT_MAP, MISSING_FIXED_INCOME_BOND_SECTOR);
  }

  @Override
  public FixedIncomeSectorResult calculate(
      final ExposureDataHolder<FixedIncomeSecuritiesAllocationType> exposureData,
      final List<PortfolioHolding> holdings) {
    final var sectors = exposureData.allocations();
    final var warnings = new ArrayList<>(exposureData.warnings());
    if (PortfolioUtils.areAllValuesZerosInMap(sectors)) {
      return responseMapper.toEmptyResponse(warnings);
    }
    final Map<FixedIncomeSecuritiesAllocationType, BigDecimal> netProducts = calculateNetProducts(
        sectors, holdings, FixedIncomeSecuritiesAllocationType.values());
    return responseMapper.fromNetProducts(netProducts, warnings);
  }
}
