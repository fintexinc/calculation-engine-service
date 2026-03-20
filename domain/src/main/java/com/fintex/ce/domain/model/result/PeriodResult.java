package com.fintex.ce.domain.model.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class PeriodResult extends WarningResult {
  protected LocalDate ped;
  protected LocalDate psd;
  protected LocalDate customIpsd;
}
