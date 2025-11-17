package com.fintex.ce.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GrowthOf10KReqDTO extends ReturnReqDTO {

    @JsonProperty(value = "useNAV")
    private boolean useNAV = false;

}
