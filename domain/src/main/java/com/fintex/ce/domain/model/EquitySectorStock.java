package com.fintex.ce.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EquitySectorStock extends EquitySector {

  private String sectorName;

  public EquitySectorStock(String sectorName) {
    this.sectorName = sectorName;
  }

}
