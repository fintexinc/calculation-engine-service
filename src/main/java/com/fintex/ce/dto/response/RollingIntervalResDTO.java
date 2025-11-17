package com.fintex.ce.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintex.ce.dto.response.core.IntervalResDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RollingIntervalResDTO {

    @JsonProperty("period")
    private String timeIntervalPeriod;
    @JsonProperty("values")
    private Set<IntervalResDTO> rollingReturn;

}