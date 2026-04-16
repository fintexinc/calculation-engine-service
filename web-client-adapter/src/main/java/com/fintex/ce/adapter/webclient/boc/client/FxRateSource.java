package com.fintex.ce.adapter.webclient.boc.client;

import java.util.List;
import lombok.Data;

@Data
public class FxRateSource {

  private String path;
  private List<String> seriesNames;
  private String startDate;
  private String endDate;
  private Frequency frequency = Frequency.DAILY;

  public enum Frequency {
    DAILY,
    MONTHLY
  }
}