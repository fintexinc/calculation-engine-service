package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.config.enumeration.ExceptionCode;
import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import com.fintex.ce.dto.holding.BenchmarkIndexHolding;
import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.CashHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.GicHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.MonthlyReturnGeneratableHolding;
import com.fintex.ce.dto.holding.SmaHolding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.exception.ReqValidationException;
import com.fintex.ce.util.DateTimeUtils;
import com.fintex.ce.util.FilterUtils;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
public class HoldingReqValidation extends ReqValidation {

    private final List<Holding> holdings;

    public HoldingReqValidation(final List<Holding> holdings) {
        this.holdings = holdings;
    }

    @Override
    public void check() {
        final var listOfHoldings = getHoldingsExcludingGicHoldings();
        if (new HashSet<>(listOfHoldings).size() != listOfHoldings.size()) {
            throw ExceptionCode.ERR_DH_001.reqValidationError();
        }
        for (Holding holding : this.holdings) {
            if (holding.getType() == null) {
                throw throwException(holding, "Holding type could not be null");
            }
            validateGicHolding(holding);
            if (holding instanceof EtfHolding h) {
                if (StringUtils.isBlank(h.getTicker())) {
                    throw throwException(holding, "Holding ticker could not be empty");
                }
                if (h.getHoldingIdentifier() == null) {
                    throw throwException(holding, "Holding identifier could not be null");
                }
            } else if (holding instanceof FundSeriesHolding h) {
                if (StringUtils.isBlank(h.getFundServCode())) {
                    throw throwException(holding, "Holding fundServCode could not be empty");
                }
                if (h.getHoldingIdentifier() == null) {
                    throw throwException(holding, "Holding identifier could not be null");
                }
            } else if (holding instanceof UsMutualFundHolding h) {
                if (StringUtils.isBlank(h.getTicker())) {
                    throw throwException(holding, "Holding ticker could not be empty");
                }
                if (h.getHoldingIdentifier() == null) {
                    throw throwException(holding, "Holding identifier could not be null");
                }
                if (!HoldingIdentifierType.TICKER.equals(h.getHoldingIdentifier())) {
                    throw throwException(holding, "UsMutualFundHolding holding identifier could only be specified as: " + HoldingIdentifierType.TICKER);
                }
            }  else if (holding instanceof CanadaPooledFundHolding h) {
                if (StringUtils.isBlank(h.getMorningstarId())) {
                    throw throwException(holding, "Holding morningstarId could not be empty");
                }
                if (h.getHoldingIdentifier() == null) {
                    throw throwException(holding, "Holding identifier could not be null");
                }
                if (!HoldingIdentifierType.MORNINGSTAR_ID.equals(h.getHoldingIdentifier())) {
                    throw throwException(holding, "CanadaPooledFundHolding holding identifier could only be specified as: " + HoldingIdentifierType.MORNINGSTAR_ID);
                }
            } else if (holding instanceof CanadaHedgeFundHolding h) {
                if (StringUtils.isBlank(h.getMorningstarId())) {
                    throw throwException(holding, "Holding morningstarId could not be empty");
                }
                if (h.getHoldingIdentifier() == null) {
                    throw throwException(holding, "Holding identifier could not be null");
                }
                if (!HoldingIdentifierType.MORNINGSTAR_ID.equals(h.getHoldingIdentifier())) {
                    throw throwException(holding, "CanadaHedgeFundHolding holding identifier could only be specified as: " + HoldingIdentifierType.MORNINGSTAR_ID);
                }
            } else if (holding instanceof StockHolding h) {
                if (StringUtils.isBlank(h.getTicker())) {
                    throw throwException(holding, "Stock ticker could not be empty");
                }
                if (StringUtils.isBlank(h.getExchangeCode())) {
                    throw throwException(holding, "Stock exchange code could not be null");
                }
                if (h.getHoldingIdentifier() == null) {
                    throw throwException(holding, "Holding identifier could not be null");
                }
            } else if (holding instanceof BenchmarkIndexHolding h) {
                if (StringUtils.isBlank(h.getMrStarId())) {
                    throw throwException(holding, "Benchmark index mrStarId could not be empty");
                }
                if (!HoldingIdentifierType.MORNINGSTAR_ID.equals(h.getHoldingIdentifier())) {
                    throw throwException(holding, "Benchmark index holding identifier could only be specified as: " + HoldingIdentifierType.MORNINGSTAR_ID);
                }
            } else if (holding instanceof FixedIncomeHolding h) {
                if (StringUtils.isBlank(h.getIdentifier())) {
                    throw throwException(holding, "Fixed Income Holding identifier could not be empty");
                }
                if (!HoldingIdentifierType.BROADRIDGE_ADP_NUMBER.equals(h.getHoldingIdentifier())) {
                    throw throwException(holding, "Fixed Income Holding identifier could only be specified as: " + HoldingIdentifierType.BROADRIDGE_ADP_NUMBER);
                }
            } else if (holding instanceof SmaHolding h) {
                if (StringUtils.isBlank(h.getIdentifier())) {
                    throw throwException(holding, "Separately Managed Holding identifier could not be empty");
                }
                if (!HoldingIdentifierType.MORNINGSTAR_ID.equals(h.getHoldingIdentifier())
                        && !HoldingIdentifierType.ENVESTNET_ID.equals(h.getHoldingIdentifier())) {
                    throw throwException(
                            holding,
                            "Separately Managed Holding identifier could only be specified as: %s or %s"
                                    .formatted(HoldingIdentifierType.MORNINGSTAR_ID, HoldingIdentifierType.ENVESTNET_ID));
                }
            }
        }

        validateCurrencyOfCashIfThereAreTwoOrMoreCashHoldings();
    }

    private List<Holding> getHoldingsExcludingGicHoldings() {
        return holdings.stream().filter(h -> !(h instanceof GicHolding)).toList();
    }

    private void validateGicHolding(Holding holding) throws ReqValidationException {
        Optional.of(holding)
                .filter(FilterUtils.GIC_PREDICATE)
                .map(MonthlyReturnGeneratableHolding.class::cast)
                .map(MonthlyReturnGeneratableHolding::getInvestmentDate)
                .filter(DateTimeUtils::isDateOlderQuincentenaryFromNow)
                .ifPresent(ignored -> {
                    throw throwException(holding, String.format("Investment date could not be before %s years ago", DateTimeUtils.QUINCENTENARY));
                });
    }

    public void validateCurrencyOfCashIfThereAreTwoOrMoreCashHoldings() {
        final List<CashHolding> cashHoldings = this.holdings
                .stream()
                .filter(CashHolding.class::isInstance)
                .map(CashHolding.class::cast)
                .toList();
        if (cashHoldings.size() > 1) {
            cashHoldings.forEach(cashHolding -> {
                if (Objects.isNull(cashHolding.getCurrency())) {
                    throw ExceptionCode.ERR_RRC_MC_002.reqValidationError();
                }
            });
        }
    }

    public static ReqValidationException throwException(final Holding h, final String message) {
        final String code = Optional.ofNullable(h).map(Holding::generateUserIdentifier).orElse("");
        return new ReqValidationException(code, message);
    }
}
