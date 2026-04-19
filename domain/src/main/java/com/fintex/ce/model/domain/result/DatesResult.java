package com.fintex.ce.model.domain.result;

import java.time.LocalDate;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public abstract class DatesResult {
  protected LocalDate ped;
  protected LocalDate psd;
}
