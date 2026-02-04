package com.fintex.ce.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CommonDates {

  private LocalDate start;
  private LocalDate end;

  public boolean hasNoDates() {
    return start == null && end == null;
  }

}
