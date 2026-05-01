package com.fintex.ce.model.domain.calculation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class BusinessCountry extends BaseCalculationData<BusinessCountry> {

  private String value;

}
