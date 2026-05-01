package com.fintex.ce.model.domain.calculation.fee;

import com.fintex.ce.model.domain.calculation.BaseCalculationData;
import com.fintex.wm.commons.domain.sales.SalesChargeType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SalesCharge extends BaseCalculationData<SalesCharge> {

  private SalesChargeType type;

}
