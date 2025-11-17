package com.fintex.ce.dto.response.maxdrawdown;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MaxDrawdownDTO {

    // field used in reflection for API test case
    private String timeIntervalPeriod;
    // field used in reflection for API test case
    private BigDecimal value;
    // field used in reflection for API test case
    private LocalDate drawdownStartDate;
    // field used in reflection for API test case
    private LocalDate drawdownTroughDate;
    // field used in reflection for API test case
    private Integer recoveryTime;

    public MaxDrawdownDTO(final String timeIntervalPeriod, final BigDecimal value) {
        this.timeIntervalPeriod = timeIntervalPeriod;
        this.value = value;
    }
}
