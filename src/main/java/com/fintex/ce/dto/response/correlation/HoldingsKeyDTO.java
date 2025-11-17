package com.fintex.ce.dto.response.correlation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.*;
import com.fintex.ce.dto.response.commonholdings.ParentHoldingDTO;
import com.fintex.ce.util.FilterUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

import static com.fintex.ce.util.PortfolioUtils.createKey;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HoldingsKeyDTO {

    private HoldingType type;
    private HoldingIdentifierType holdingIdentifier;
    private String fundServCode;
    private String ticker;
    private String exchangeCode;
    private String key;
    private BigDecimal allocation;
    private String name;
    private Currency currency;
    private String morningstarId;

    public static HoldingsKeyDTO buildHoldingsKeyDTO(final Holding holding) {
        return buildDTO(holding, new HoldingsKeyDTO());
    }

    public static HoldingsKeyDTO buildParentKeyDTO(final Holding holding) {
        return buildDTO(holding, new ParentHoldingDTO());
    }

    private static HoldingsKeyDTO buildDTO(final Holding holding, final HoldingsKeyDTO holdingsKeyDTO) {
        holdingsKeyDTO.setType(holding.getType());
        holdingsKeyDTO.setHoldingIdentifier(holding.getHoldingIdentifier());
        if (FilterUtils.CANADA_MUTUAL_PREDICATE.test(holding)) {
            final FundSeriesHolding fundSeriesHolding = (FundSeriesHolding) holding;
            holdingsKeyDTO.setFundServCode(fundSeriesHolding.getFundServCode());
        } else if (FilterUtils.ETF_PREDICATE.test(holding)) {
            final EtfHolding etfHolding = (EtfHolding) holding;
            holdingsKeyDTO.setTicker(etfHolding.getTicker());
            holdingsKeyDTO.setExchangeCode(etfHolding.getExchangeCode());
        } else if (FilterUtils.STOCK_PREDICATE.test(holding)) {
            final StockHolding stockHolding = (StockHolding) holding;
            holdingsKeyDTO.setTicker(stockHolding.getTicker());
            holdingsKeyDTO.setExchangeCode(stockHolding.getExchangeCode());
        } else if(FilterUtils.CASH_PREDICATE.test(holding)) {
            final CashHolding cashHolding = (CashHolding) holding;
            holdingsKeyDTO.setCurrency(cashHolding.getCurrency());
        } else if(FilterUtils.GIC_PREDICATE.test(holding)) {
            final GicHolding gicHolding = (GicHolding) holding;
            holdingsKeyDTO.setName(gicHolding.getName());
        } else if(FilterUtils.CANADA_POOLED_FUND_PREDICATE.test(holding)) {
            final CanadaPooledFundHolding canadaPooledFundHolding = (CanadaPooledFundHolding) holding;
            holdingsKeyDTO.setMorningstarId(canadaPooledFundHolding.getMorningstarId());
        } else if(FilterUtils.CANADA_HEDGE_FUND_PREDICATE.test(holding)) {
            final CanadaHedgeFundHolding canadaHedgeFundHolding = (CanadaHedgeFundHolding) holding;
            holdingsKeyDTO.setMorningstarId(canadaHedgeFundHolding.getMorningstarId());
        } else if(FilterUtils.US_MUTUAL_FUND_PREDICATE.test(holding)) {
            final UsMutualFundHolding usMutualFundHolding = (UsMutualFundHolding) holding;
            holdingsKeyDTO.setTicker(usMutualFundHolding.getTicker());
        } else if(FilterUtils.SEPARATELY_MANAGED_ACCOUNT_PREDICATE.test(holding)) {
            final SmaHolding smaHolding = (SmaHolding) holding;
            holdingsKeyDTO.setTicker(smaHolding.getIdentifier());
        }
        holdingsKeyDTO.setKey(createKey(holding));
        return holdingsKeyDTO;
    }

}
