package com.fintex.ce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.dto.request.core.PortfolioReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PeriodsReqDTO extends PortfolioReqDTO {

    @JsonProperty("customIntervalPerformanceStartDate")
    private LocalDate customIntervalPsd;

    @JsonProperty("customPerformanceEndDate")
    private LocalDate customPed;

    @JsonProperty("timeIntervalPeriods")
    private Set<String> periods;

}
