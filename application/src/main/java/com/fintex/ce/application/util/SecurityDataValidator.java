package com.fintex.ce.application.util;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

import static com.fintex.ce.model.error.ErrorCode.NO_SECURITY_DATA_FOR_HOLDING;

/**
 * Pre-condition checks on security-data fetcher responses. Fee / income / returns endpoints all share the same failure
 * mode: the data source happily returns HTTP 200 with a partial result set when it doesn't recognise a security or the
 * configured data provider has no record for it. Silently treating those missing rows as "data not available" pushes
 * the gap downstream where it surfaces as a misleading error (e.g. {@code MER-005 — all fee fields are null}) or,
 * worse, as a quietly under-reported result. This validator fails fast at the boundary with
 * {@link com.fintex.ce.model.error.ErrorCode#NO_SECURITY_DATA_FOR_HOLDING} (HTTP 400) so the caller knows which
 * holdings the data source couldn't supply.
 */
@UtilityClass
public final class SecurityDataValidator {

  /**
   * Throws {@link com.fintex.ce.model.error.ErrorCode#NO_SECURITY_DATA_FOR_HOLDING} for the first {@code requested}
   * holding that {@code mandatoryFor} accepts and whose {@link SecurityIdentifier} is not represented in
   * {@code rawData}.
   *
   * <p>
   * The check is <b>identifier-based</b>, not full-equality based, because a portfolio may legitimately contain two
   * holdings with the same identifier but different {@code value} (e.g. the same fund held in two accounts). The data
   * source will dedupe by identifier and return a single row, but the validator must not fail either of the
   * duplicate-holding entries.
   *
   * @param <T>
   *          payload type (FeeData, MonthlyReturns, etc.); unused by the check, kept for call-site type inference.
   */
  public static <T> void requireDataForEveryHolding(
      Map<PortfolioHolding, T> rawData,
      Collection<? extends PortfolioHolding> requested,
      Predicate<PortfolioHolding> mandatoryFor) {
    Set<SecurityIdentifier> received = rawData.keySet().stream()
        .map(PortfolioHolding::getSecurityIdentifier)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    for (PortfolioHolding holding : requested) {
      if (!mandatoryFor.test(holding)) {
        continue;
      }
      SecurityIdentifier id = holding.getSecurityIdentifier();
      if (id == null || !received.contains(id)) {
        throw NO_SECURITY_DATA_FOR_HOLDING.toExceptionForHolding(holding, holding.getIdsString());
      }
    }
  }
}