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
import static com.fintex.ce.model.error.ErrorCode.COUNTRY_NOT_SUPPORTED;
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
 * Supported countries come from every {@link CountryFeeResolutionStrategy} bean, injected as a list. This resolver is a
 * pure chain-walker: the concrete chains (and the error raised when a chain is exhausted) live in the strategies.
 *
 * <pre>
 *   CANADA →  MER  →  Management Fee  →  error (MER-005)
 *   USA    →  NER  →  GER  →  error (MER-002)
 * </pre>
 *
 * <p>
 * <b>The MER and Fees metrics share this single resolver, so both apply the same per-country policy.</b> In particular,
 * the US chain deliberately omits the Management Fee: a US holding missing both NER and GER fails with
 * {@link com.fintex.ce.model.error.ErrorCode#MISSING_NER_AND_GER} (MER-002) for the Fees metric as well as for MER —
 * there is no Fees-only Management-Fee fallback. The Management Fee <i>metric</i> is unaffected; it does not use this
 * resolver (see {@code ManagementFeeCalculationServiceImpl}).
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
    Country country = holding.getCountry();
    CountryFeeResolutionStrategy strategy = strategiesByCountry.get(country);
    if (strategy == null) {
      throw COUNTRY_NOT_SUPPORTED.toExceptionForHolding(holding, country == null ? null : country.name());
    }
    return walkChain(strategy, calc, holding);
  }

  private List<Notification> walkChain(CountryFeeResolutionStrategy strategy, AverageManagementExpenseCalculation calc,
      PortfolioHolding holding) {
    List<Notification> warnings = new ArrayList<>();
    for (FeeSource source : strategy.sources()) {
      Optional<BigDecimal> value = source.extract(calc);
      if (value.isPresent()) {
        calc.setModifiedFee(value.get());
        return warnings;
      }
      source.warningIfMissing(holding).ifPresent(warnings::add);
    }
    throw strategy.exhaustedError().toExceptionForHolding(holding, holding.getIdsString());
  }
}
