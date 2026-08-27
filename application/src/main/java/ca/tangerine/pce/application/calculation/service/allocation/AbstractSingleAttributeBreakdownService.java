package ca.tangerine.pce.application.calculation.service.allocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import ca.tangerine.pce.application.calculation.service.PortfolioWeightCalculator;
import ca.tangerine.pce.application.util.PortfolioUtils;
import ca.tangerine.pce.calculation.SingleAttributeCalculationService;
import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.result.BaseCalculationResult;
import ca.tangerine.pce.model.dto.command.PortfolioHoldingsCommand;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.error.Notification;

/**
 * {@link AbstractBreakdownService} specialised for the common case of a metric backed by exactly ONE Market Investment
 * Catalogue attribute: the consumed data is a {@code Map<PortfolioHolding, A>} of that attribute's per-holding domain
 * object. Implementing {@link SingleAttributeCalculationService} supplies {@code requiredAttributes()} /
 * {@code prepareData} for free, so a metric only declares its {@link #requiredAttribute()} and provides
 * {@link #currencyOf} and {@link #toBuckets}.
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
   * Maps a holding's attribute datum onto the bucket enum. {@code attribute} is {@code null} when Market Investment
   * Catalogue returned nothing for the holding; implementations warn and return a fallback {@link #singleBucket}.
   */
  protected abstract Map<T, BigDecimal> toBuckets(PortfolioHolding holding, A attribute, List<Notification> warnings);
}
