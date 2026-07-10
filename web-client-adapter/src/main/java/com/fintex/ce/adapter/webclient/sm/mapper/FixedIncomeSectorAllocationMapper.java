package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.model.domain.calculation.allocation.FixedIncomeBondSecurities;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocation;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationType;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSectorAllocationTypeValue;
import com.fintex.wm.commons.domain.allocation.FixedIncomeSecuritiesAllocationType;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps SM FixedIncomeSectorAllocation (Morningstar SuperSector) to PCE FixedIncomeBondSecurities. SM sector types are
 * translated to PCE FixedIncomeSecuritiesAllocationType-compatible string keys.
 */
@Component
public class FixedIncomeSectorAllocationMapper
    implements
      SecurityMasterResponseMapper<FixedIncomeBondSecurities, FixedIncomeSectorAllocation> {

  private static final Map<FixedIncomeSectorAllocationType, FixedIncomeSecuritiesAllocationType> SECTOR_TYPE_MAPPING;

  static {
    SECTOR_TYPE_MAPPING = new EnumMap<>(FixedIncomeSectorAllocationType.class);
    SECTOR_TYPE_MAPPING.put(FixedIncomeSectorAllocationType.GOVERNMENT,
        FixedIncomeSecuritiesAllocationType.GOVERNMENT_BONDS);
    SECTOR_TYPE_MAPPING.put(FixedIncomeSectorAllocationType.CORPORATE,
        FixedIncomeSecuritiesAllocationType.CORPORATE_BONDS);
    SECTOR_TYPE_MAPPING.put(FixedIncomeSectorAllocationType.CASH, FixedIncomeSecuritiesAllocationType.ST_INVESTMENTS);
    SECTOR_TYPE_MAPPING.put(FixedIncomeSectorAllocationType.SECURITIZED,
        FixedIncomeSecuritiesAllocationType.MORTGAGE_BACKED_SECURITIES);
    SECTOR_TYPE_MAPPING.put(FixedIncomeSectorAllocationType.MUNICIPAL, FixedIncomeSecuritiesAllocationType.OTHER_BONDS);
    SECTOR_TYPE_MAPPING.put(FixedIncomeSectorAllocationType.DERIVATIVE,
        FixedIncomeSecuritiesAllocationType.ASSET_BACKED_SECURITIES);
  }

  @Override
  public FixedIncomeBondSecurities map(FixedIncomeSectorAllocation smsResponse, PortfolioHolding holding) {
    Map<FixedIncomeSecuritiesAllocationType, BigDecimal> allocationMap = Optional.ofNullable(smsResponse)
        .map(FixedIncomeSectorAllocation::getAllocations)
        .orElse(List.of())
        .stream()
        .filter(entry -> entry.getType() != null && entry.getValue() != null)
        .filter(entry -> SECTOR_TYPE_MAPPING.containsKey(entry.getType()))
        .collect(Collectors.toMap(
            entry -> SECTOR_TYPE_MAPPING.get(entry.getType()),
            FixedIncomeSectorAllocationTypeValue::getValue,
            BigDecimal::add));

    final List<DataProvider> providers = Optional.ofNullable(smsResponse)
        .map(FixedIncomeSectorAllocation::getDataProviders)
        .orElseGet(List::of);

    return FixedIncomeBondSecurities.builder()
        .fixedIncomeBondSectors(allocationMap)
        .holdingType(holding.getHoldingType())
        .providers(providers)
        .build();
  }
}
