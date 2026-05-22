package com.fintex.ce.application.calculation.service.fee;

import com.fintex.ce.model.domain.calculation.fee.AverageManagementExpenseCalculation;
import com.fintex.ce.model.domain.calculation.fee.FeeData;
import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.wm.commons.domain.enumeration.FinancialInstrumentType;
import com.fintex.wm.commons.error.Notification;

import java.util.List;
import java.util.Map;

/**
 * Strategy abstraction for per-holding fee resolution. Implementations decide which fee fields are read from
 * {@link FeeData}, how the resolution chain is walked, and which warnings are emitted.
 *
 * <p>
 * Used by {@link AbstractFeeCalculationService} subclasses that need a configurable fee-extraction policy. The current
 * implementation, {@link MerFeeResolver}, walks the {@link FeeSource} chain per holding country; future strategies
 * (e.g. an income-distribution resolver) can plug in without touching the calculation services.
 */
public interface FeeResolver {

  /**
   * Maps a raw {@link FeeData} response into the per-holding calculation carrier — copies the fee fields and the
   * source-reported currency for MER-bearing holdings; leaves the carrier empty otherwise.
   */
  AverageManagementExpenseCalculation mapFeeDataToCalculation(PortfolioHolding holding, FeeData fees);

  /**
   * Sets {@code modifiedFee} on each calculation per the implementation's policy. Returns the warnings produced during
   * resolution (e.g. fallbacks where a primary fee was missing). Throws if the policy considers a holding's data fatal.
   */
  List<Notification> resolveFees(
      Map<FinancialInstrumentType, Map<PortfolioHolding, AverageManagementExpenseCalculation>> groupOfMers);
}
