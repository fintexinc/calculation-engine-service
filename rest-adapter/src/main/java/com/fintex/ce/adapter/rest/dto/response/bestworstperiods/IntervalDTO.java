package com.fintex.ce.adapter.rest.dto.response.bestworstperiods;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntervalDTO {

  private LocalDate startDate;
  private LocalDate endDate;

}
