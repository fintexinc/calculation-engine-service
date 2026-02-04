package com.fintex.ce.adapter.cache.dto;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.constant.CacheNameEntity;
import com.fintex.ce.domain.model.holding.Holding;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CacheRecordDTO {
  private Holding holding;
  private int numberOfUsages;
  private DataProvider provider;
  private CacheNameEntity cacheNameEntity;

  public CacheRecordDTO() {
  }

  public CacheRecordDTO(Holding holding, int numberOfUsages, CacheNameEntity cacheNameEntity, DataProvider provider) {
    this.holding = holding;
    this.numberOfUsages = numberOfUsages;
    this.cacheNameEntity = cacheNameEntity;
    this.provider = provider;
  }
}
