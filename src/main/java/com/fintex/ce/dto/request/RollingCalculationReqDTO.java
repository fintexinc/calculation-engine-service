package com.fintex.ce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class RollingCalculationReqDTO extends PeriodsReqDTO {

    @JsonProperty("customPerformanceStartDate")
    private LocalDate customPsd;

    @JsonProperty("rollingTimeIntervalPeriod")
    private Set<String> rollingPeriods;


}
