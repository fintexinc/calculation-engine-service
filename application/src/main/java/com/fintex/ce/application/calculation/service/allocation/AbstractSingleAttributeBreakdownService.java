package com.fintex.ce.application.calculation.service.allocation;

import com.fintex.ce.application.calculation.service.PortfolioWeightCalculator;
import com.fintex.ce.application.util.PortfolioUtils;
import com.fintex.ce.calculation.SingleAttributeCalculationService;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.result.BaseCalculationResult;
import com.fintex.ce.model.dto.command.PortfolioHoldingsCommand;
import com.fintex.wm.commons.domain.currency.Currency;
import com.fintex.wm.commons.error.Notification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * {@link AbstractBreakdownService} specialised for the common case of a metric backed by exactly ONE Security Master
 * attribute: the consumed data is a {@code Map<PortfolioHolding, A>} of that attribute's per-holding domain object.
 * Implementing {@link SingleAttributeCalculationService} supplies {@code requiredAttributes()} / {@code prepareData}
 * for free, so a metric only declares its {@link #requiredAttribute()} and provides {@link #currencyOf} and
 * {@link #toBuckets}.
 *
 * @param <A>
 *          the attribute's per-holding domain type
 * @param <R>
 *          the concrete result type
 * @param <T>
 *          the bucket enum
 */
public abstract class AbstractSingleAttributeBreakdownService<A, R extends BaseCalculationResult, T extends Enum<T>>
    extends
      AbstractBreakdownService<Map<PortfolioHolding, A>, R, T>
    implements
      SingleAttributeCalculationService<PortfolioHoldingsCommand, A, R> {

  protected AbstractSingleAttributeBreakdownService(PortfolioWeightCalculator portfolioWeightCalculator,
      Class<T> bucketType) {
    super(portfolioWeightCalculator, bucketType);
  }

  @Override
  protected final Currency currencyFor(PortfolioHolding holding, Map<PortfolioHolding, A> data) {
    return PortfolioUtils.currencyFor(holding, data, this::currencyOf);
  }

  @Override
  protected final Map<T, BigDecimal> exposureFor(PortfolioHolding holding, Map<PortfolioHolding, A> data,
      List<Notification> warnings) {
    return toBuckets(holding, data.get(holding), warnings);
  }

  /**
   * The currency carried by the attribute datum (for FX weighting); {@code null} if absent.
   */
  protected abstract Currency currencyOf(A attribute);

  /**
   * Maps a holding's attribute datum onto the bucket enum. {@code attribute} is {@code null} when Security Master
   * returned nothing for the holding; implementations warn and return a fallback {@link #singleBucket}.
   */
  protected abstract Map<T, BigDecimal> toBuckets(PortfolioHolding holding, A attribute, List<Notification> warnings);
}
