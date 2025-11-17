package com.fintex.ce.dto.cache;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.config.enumeration.cache.CacheNameEntity;
import com.fintex.ce.dto.holding.Holding;
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
