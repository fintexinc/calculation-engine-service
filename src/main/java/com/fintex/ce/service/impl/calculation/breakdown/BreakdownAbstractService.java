package com.fintex.ce.service.impl.calculation.breakdown;

import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.response.core.Warning;
import com.fintex.ce.dto.response.core.WarningDTO;
import com.fintex.ce.util.validation.request.RequestValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fintex.ce.util.CalculationUtils.sumProduct;
import static com.fintex.ce.util.CollectorUtils.toMap;
import static com.fintex.ce.util.PortfolioUtils.calculateInitialPortfolioWeight;

/**
 * @param <E> response object
 * @param <T> response enum type
 */
public abstract class BreakdownAbstractService<E extends WarningDTO, T> {

    protected final RequestValidator<PortfolioHoldingsReqDTO> requestValidator;

    protected BreakdownAbstractService(final RequestValidator<PortfolioHoldingsReqDTO> requestValidator) {
        this.requestValidator = requestValidator;
    }

    public abstract E calculate(final Map<Holding, Map<T, BigDecimal>> exposures,
                                final List<Holding> holdings,
                                final List<Warning> warnings);

    public abstract Map<Holding, Map<T, BigDecimal>> getLoadFromCacheStorage(PortfolioHoldingsReqDTO reqDTO, List<Warning> warnings);

    public E perform(final PortfolioHoldingsReqDTO reqDTO) {
        requestValidator.validate(reqDTO);
        final List<Warning> warnings = new ArrayList<>();
        final Map<Holding, Map<T, BigDecimal>> exposures = getLoadFromCacheStorage(reqDTO, warnings);
        return calculate(exposures, reqDTO.getHoldings(), warnings);
    }

    /**
     * calculates Net Products for all input types
     *
     * @param values   values from FDS grouped by holding types
     * @param holdings list of holdings from request
     * @param types    an array with the types for which net products should be calculated
     * @return calculate net product
     */
    public Map<T, BigDecimal> calculateNetProducts(final Map<Holding, Map<T, BigDecimal>> values,
                                                   final List<Holding> holdings,
                                                   final T[] types) {
        final Map<T, BigDecimal> products = new HashMap<>();
        final Map<Holding, BigDecimal> weights = calculateInitialPortfolioWeight(holdings);
        for (T type : types) {
            final BigDecimal product = calculateNetProduct(type, values, weights);
            products.put(type, product);
        }
        return products;
    }

    /**
     * calculates net product value by type
     *
     * @param type    type for which net products should be calculated
     * @param values  values from FDS grouped by holding types
     * @param weights calculated weights grouped by holdings
     * @return calculate net product
     */
    public BigDecimal calculateNetProduct(final T type,
                                          final Map<Holding, Map<T, BigDecimal>> values,
                                          final Map<Holding, BigDecimal> weights) {
        final Map<Holding, BigDecimal> typeExposures = values.entrySet()
                .stream()
                .filter(e -> e.getValue().containsKey(type))
                .collect(toMap(Map.Entry::getKey, e -> e.getValue().get(type)));
        return sumProduct(typeExposures, weights);
    }

}
