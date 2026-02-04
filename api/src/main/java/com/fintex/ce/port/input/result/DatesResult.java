package com.fintex.ce.port.input.result;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public abstract class DatesResult extends ErrorResult {
  protected LocalDate ped;
  protected LocalDate psd;
}
