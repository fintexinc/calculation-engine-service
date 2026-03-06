package com.fintex.ce.adapter.cache.mapper;

import com.fintex.ce.adapter.cache.entity.averagemer.RAverageMer;
import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.AverageMer;
import com.fintex.ce.port.mapper.CacheEntityMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AverageMerMapper implements CacheEntityMapper<AverageMer, RAverageMer> {

  @Override
  public Optional<AverageMer> toDomain(RAverageMer entity) {
    return Optional.ofNullable(entity)
        .map(e -> {
          AverageMer domain = new AverageMer();
          domain.setMer(e.getMer());
          domain.setActualManagementFee(e.getActualManagementFee());
          domain.setMerProvider(e.getMerProvider() != null ? e.getMerProvider().name() : null);
          domain.setActualManagementFeeProvider(e.getActualManagementFeeProvider() != null
              ? e.getActualManagementFeeProvider().name()
              : null);
          domain.setNetExpenseRatio(e.getNetExpenseRatio());
          domain.setGrossExpenseRatio(e.getGrossExpenseRatio());
          domain.setNetExpenseRatioProvider(e.getNetExpenseRatioProvider() != null
              ? e.getNetExpenseRatioProvider().name()
              : null);
          domain.setGrossExpenseRatioProvider(e.getGrossExpenseRatioProvider() != null
              ? e.getGrossExpenseRatioProvider().name()
              : null);
          domain.setHoldingId(e.getHoldingId());
          domain.setProvider(e.getProvider());
          domain.setProviders(e.getProviders());
          return domain;
        });
  }

  @Override
  public Optional<RAverageMer> toEntity(AverageMer domain) {
    return Optional.ofNullable(domain)
        .map(d -> {
          RAverageMer entity = new RAverageMer();
          entity.setMer(d.getMer());
          entity.setActualManagementFee(d.getActualManagementFee());
          if (d.getMerProvider() != null) {
            entity.setMerProvider(DataProvider.of(d.getMerProvider()));
          }
          if (d.getActualManagementFeeProvider() != null) {
            entity.setActualManagementFeeProvider(DataProvider.of(d.getActualManagementFeeProvider()));
          }
          entity.setNetExpenseRatio(d.getNetExpenseRatio());
          entity.setGrossExpenseRatio(d.getGrossExpenseRatio());
          if (d.getNetExpenseRatioProvider() != null) {
            entity.setNetExpenseRatioProvider(DataProvider.of(d.getNetExpenseRatioProvider()));
          }
          if (d.getGrossExpenseRatioProvider() != null) {
            entity.setGrossExpenseRatioProvider(DataProvider.of(d.getGrossExpenseRatioProvider()));
          }
          return entity;
        });
  }

}
