package com.fintex.ce.adapter.rest.dto.response.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CalculationArrayValueResDTO extends WarningDTO {

  private Object arrayValue;

}
