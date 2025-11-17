package com.fintex.ce.util.validation.request.chainofresponsibility;

import com.fintex.ce.dto.holding.CanadaHedgeFundHolding;
import com.fintex.ce.dto.holding.CanadaPooledFundHolding;
import com.fintex.ce.dto.holding.EtfHolding;
import com.fintex.ce.dto.holding.FundSeriesHolding;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.holding.StockHolding;
import com.fintex.ce.dto.holding.UsMutualFundHolding;
import com.fintex.ce.config.enumeration.ExceptionCode;
import lombok.EqualsAndHashCode;

import java.util.List;

import static java.math.BigDecimal.ZERO;

@EqualsAndHashCode(callSuper = true)
public class HoldingValueReqValidator extends ReqValidation {

    private final List<Holding> holdings;

    public HoldingValueReqValidator(final List<Holding> holdings) {
        this.holdings = holdings;
    }

    @Override
    public void check() {
        for (Holding holding : holdings) {
            if (holding.getValue() == null) {
                if (holding instanceof FundSeriesHolding h) {
                    throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(h.getFundServCode());
                } else if (holding instanceof EtfHolding h) {
                    throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(h.getTicker());
                } else if (holding instanceof StockHolding h) {
                    throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(h.getTicker());
                } else if (holding instanceof UsMutualFundHolding h) {
                    throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(h.getTicker());
                } else if (holding instanceof CanadaPooledFundHolding h) {
                    throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(h.getMorningstarId());
                } else if (holding instanceof CanadaHedgeFundHolding h) {
                    throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationErrorWithId(h.getMorningstarId());
                }
                throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationError();
            } else if (holding.getValue().compareTo(ZERO) < ZERO.intValue()) {
                throw ExceptionCode.ERR_ALL_GTZ_001.reqValidationError();
            }
        }
    }
}

