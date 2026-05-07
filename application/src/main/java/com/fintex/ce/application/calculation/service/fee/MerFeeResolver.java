package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.enumeration.Country;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.fintex.ce.application.constant.HoldingTypeGroup.MER_BEARING_TYPES;
import static com.fintex.ce.application.constant.HoldingTypeGroup.ZERO_MER_TYPES;
import static com.fintex.ce.model.error.ErrorCode.MISSING_FUND_FEE_DATA;
import static java.math.BigDecimal.ZERO;

/**
 * Default {@link FeeResolver} implementation. Dispatches per holding type group:
 * <ul>
 * <li>MER-bearing → delegates to the {@link CountryFeeResolutionStrategy} registered for the holding's country, then
 * walks its {@link FeeSource} chain.</li>
 * <li>Zero-MER (stocks, cash, GIC, fixed income) → sets fee = 0.</li>
 * <li>Anything else → ignored ({@link AbstractFeeCalculationService} rejects unknown leaf types upstream).</li>
 * </ul>
 *
 * <p>
 * Supported countries come from the injected list of {@link CountryFeeResolutionStrategy} beans — adding a country
 * means dropping a new {@code @Component} into the application context, no edit here.
 *
 * <pre>
 *   CANADA  →  MER  →  Management Fee  →  error (MER-005)
 *   USA     →  NER  →  GER  →  Management Fee  →  error (MER-005)
 * </pre>
 */
@Service
public class MerFeeResolver implements FeeResolver {

  private final Map<Country, CountryFeeResolutionStrategy> strategiesByCountry;

  public MerFeeResolver(List<CountryFeeResolutionStrategy> strategies) {
    this.strategiesByCountry = strategies.stream()
        .collect(Collectors.toUnmodifiableMap(CountryFeeResolutionStrategy::country, Function.identity()));
  }

  @Override
  public AverageManagementExpenseCalculation mapFeeDataToCalculation(PortfolioHolding holding, FeeData fees) {
    FinancialInstrumentType type = holding.getHoldingType();
    var builder = AverageManagementExpenseCalculation.builder()
        .marketValue(holding.getValue())
        .holdingType(type);
    if (MER_BEARING_TYPES.contains(type)) {
      builder.managementExpenseRatio(fees.getManagementExpenseRatio())
          .netExpenseRatio(fees.getNetExpenseRatio())
          .grossExpenseRatio(fees.getGrossExpenseRatio())
          .actualManagementFee(fees.getManagementFee())
          .currency(fees.getCurrency());
    }
    return builder.build();
  }

  @Override
  public List<Notification> resolveFees(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers) {
    return groupOfMers.entrySet().stream()
        .flatMap(entry -> resolveGroup(entry.getKey(), entry.getValue()).stream())
        .toList();
  }

  private List<Notification> resolveGroup(FinancialInstrumentType type,
      Map<PortfolioHolding, AverageManagementExpenseCalculation> calcsByHolding) {
    if (MER_BEARING_TYPES.contains(type)) {
      return calcsByHolding.entrySet().stream()
          .flatMap(e -> resolveSingle(e.getValue(), e.getKey()).stream())
          .toList();
    }
    if (ZERO_MER_TYPES.contains(type)) {
      calcsByHolding.values().forEach(c -> c.setModifiedFee(ZERO));
    }
    return List.of();
  }

  private List<Notification> resolveSingle(AverageManagementExpenseCalculation calc, PortfolioHolding holding) {
    CountryFeeResolutionStrategy strategy = strategiesByCountry.get(holding.getHoldingType().getCountry());
    if (strategy == null) {
      throw MISSING_FUND_FEE_DATA.toExceptionForHolding(holding, holding.getIdsString());
    }
    return walkChain(strategy.sources(), calc, holding);
  }

  private List<Notification> walkChain(List<FeeSource> sources, AverageManagementExpenseCalculation calc,
      PortfolioHolding holding) {
    List<Notification> warnings = new ArrayList<>();
    for (FeeSource source : sources) {
      Optional<BigDecimal> value = source.extract(calc);
      if (value.isPresent()) {
        calc.setModifiedFee(value.get());
        return warnings;
      }
      source.warningIfMissing(holding).ifPresent(warnings::add);
    }
    throw MISSING_FUND_FEE_DATA.toExceptionForHolding(holding, holding.getIdsString());
  }
}
