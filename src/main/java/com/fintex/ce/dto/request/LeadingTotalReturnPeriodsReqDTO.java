package com.fintex.ce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class LeadingTotalReturnPeriodsReqDTO extends PeriodsReqDTO {

    @JsonProperty("customPerformanceStartDate")
    private LocalDate customPsd;

}
