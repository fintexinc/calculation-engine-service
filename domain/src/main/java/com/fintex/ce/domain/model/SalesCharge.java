package com.fintex.ce.domain.model;

import com.fintex.sm.model.domain.enumeration.SalesChargeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Accessors(chain = true)
public class SalesCharge extends BaseCalculationData<SalesCharge> {

  private SalesChargeType type;

}
