package com.fintex.ce.domain.calculation;

import com.fintex.ce.dto.response.core.PeriodResDTO;

import java.util.Set;

/**
 * Period based calculation
 *
 * @param <E> period based response object
 */
public interface PeriodBasedCalculation<E extends PeriodResDTO> {

    /**
     * Calculates period based method
     *
     * @param periods user entered periods (not default one)
     * @return period based response object
     */
    E calculate(final Set<String> periods);

}
