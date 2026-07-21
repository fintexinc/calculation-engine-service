package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.util.AllocationHelper;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.application.util.PortfolioUtils.calculateInitialPortfolioWeight;

/**
 * Base class for breakdown calculation services, providing the weighted net-product aggregation shared by the
 * allocation and exposure metrics. The {@code calculateNetProducts} / {@code calculateNetProduct} methods delegate to
 * {@link AllocationHelper} for the actual math; the instance-method form is kept here so subclasses can call them via
 * inheritance and so existing tests can stub the call without static mocking. {@code calculateNetProducts} iterates
 * over types and dispatches through {@code this.calculateNetProduct} so test doubles can intercept per-type
 * invocations.
 *
 * @param <D>
 *          the strongly typed data the service consumes
 * @param <R>
 *          result object
 * @param <T>
 *          allocation enum type
 */
public abstract class BreakdownAbstractService<D, R extends BaseCalculationResult, T>
    implements
      CalculationService<PortfolioHoldingsCommand, D, R> {

  protected BreakdownAbstractService() {
  }

  public Map<T, BigDecimal> calculateNetProducts(Map<PortfolioHolding, Map<T, BigDecimal>> values,
      List<PortfolioHolding> holdings, T[] types) {
    Map<PortfolioHolding, BigDecimal> weights = calculateRawHoldingValueWeights(holdings);
    Map<T, BigDecimal> products = new HashMap<>();
    for (T type : types) {
      products.put(type, calculateNetProduct(type, values, weights));
    }
    return products;
  }

  public Map<PortfolioHolding, BigDecimal> calculateRawHoldingValueWeights(List<PortfolioHolding> holdings) {
    return calculateInitialPortfolioWeight(holdings);
  }

  public BigDecimal calculateNetProduct(T type, Map<PortfolioHolding, Map<T, BigDecimal>> values,
      Map<PortfolioHolding, BigDecimal> weights) {
    return AllocationHelper.calculateNetProduct(type, values, weights);
  }
}
