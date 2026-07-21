package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.config.FxProperties;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.ce.util.FilterUtils;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.FilterUtils.CASH_PREDICATE;
import static com.fintex.ce.util.FilterUtils.GIC_PREDICATE;

/**
 * Shared implementation for sector breakdown services (equity sector, fixed-income bond sector). Builds per-holding
 * sector exposures from the single Security Master attribute the concrete service declares (cash/GIC holdings are
 * excluded), then aggregates them into net products using portfolio weights normalized to the default target currency
 * configured in {@link FxProperties#getDefaultTargetCurrency()} via {@link PortfolioWeightCalculator} — so
 * multi-currency portfolios produce correct sector percentages. Each holding's currency is resolved through
 * {@link PortfolioUtils#currencyFor}: cash/GIC directly off the typed holding, everything else from the fetched sector
 * data via {@link #currencyOf}. When a holding has no resolvable currency, its raw value participates in the weight
 * unchanged (no warning). Subclasses supply the type-specific exposure extraction, currency resolution, and result
 * construction.
 *
 * @param <D>
 *          raw per-holding security data type fetched from Security Master
 * @param <R>
 *          concrete result type
 * @param <T>
 *          allocation enum type
 */
public abstract class AbstractSectorAllocationService<D, R extends BaseCalculationResult, T>
    extends
      BreakdownAbstractService<Map<PortfolioHolding, D>, R, T>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, D, R> {

  // TODO(TMI-475): the SingleAttributeCalculationService assumption (one Security Master attribute per service) only
  // holds for funds, whose sector split comes from SECTOR_ALLOCATION. Individual stocks have no sector-allocation
  // attribute and need a separate sector/industry attribute resolved per holding. Revisit this interface — and the
  // stock-vs-fund exposure handling in the concrete services — once TMI-475 decides the stock sector attribute.

  protected final PortfolioWeightCalculator portfolioWeightCalculator;

  protected AbstractSectorAllocationService(PortfolioWeightCalculator portfolioWeightCalculator) {
    this.portfolioWeightCalculator = portfolioWeightCalculator;
  }

  @Override
  public R perform(PortfolioHoldingsCommand command, Map<PortfolioHolding, D> data) {
    List<PortfolioHolding> holdings = command.getHoldings();
    Map<PortfolioHolding, D> rawData = FilterUtils.restrictToHoldings(data, holdings);
    List<Notification> warnings = new ArrayList<>();

    Map<PortfolioHolding, Map<T, BigDecimal>> exposures = new HashMap<>();
    Map<PortfolioHolding, Currency> currencies = new HashMap<>();
    for (PortfolioHolding holding : holdings) {
      Currency currency = PortfolioUtils.currencyFor(holding, rawData, this::currencyOf);
      if (currency != null) {
        currencies.put(holding, currency);
      }
      if (CASH_PREDICATE.or(GIC_PREDICATE).test(holding)) {
        continue;
      }
      exposures.put(holding, toSectorExposure(holding, rawData.get(holding), warnings));
    }
    if (exposures.isEmpty()) {
      return emptyResponse(warnings);
    }

    PortfolioWeightCalculator.Result weightResult = portfolioWeightCalculator.compute(holdings, currencies);
    warnings.addAll(weightResult.warnings());

    Map<T, BigDecimal> netProducts = calculateNetProductsWithWeights(exposures, weightResult.weights(),
        allocationTypes());
    return fromNetProducts(netProducts, warnings);
  }

  protected abstract T[] allocationTypes();

  protected abstract Currency currencyOf(D data);

  protected abstract Map<T, BigDecimal> toSectorExposure(PortfolioHolding holding, D data,
      List<Notification> warnings);

  protected abstract R emptyResponse(List<Notification> warnings);

  protected abstract R fromNetProducts(Map<T, BigDecimal> netProducts, List<Notification> warnings);

}
