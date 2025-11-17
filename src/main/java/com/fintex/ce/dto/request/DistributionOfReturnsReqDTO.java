package com.fintex.ce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class DistributionOfReturnsReqDTO extends PeriodsReqDTO {

    @JsonProperty("customPerformanceStartDate")
    private LocalDate customPsd;

    @JsonProperty("numberOfBins")
    private Integer customNumberOfBins;
}
