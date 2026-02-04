package com.fintex.ce.adapter.jdbc.entity;

import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.constant.CacheCategory;
import com.fintex.ce.constant.CacheNameEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Data
@Accessors(chain = true)
@Entity
@Table(name = "fas_usage_statistics", schema = "public")
public class SMUsageStatistics {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "cache_name_entity")
  @Enumerated(EnumType.STRING)
  private CacheNameEntity cacheNameEntity;
  @Column(name = "cache_category")
  @Enumerated(EnumType.STRING)
  private CacheCategory cacheCategory;

  @Column(name = "provider")
  private String provider;

  @Column(name = "holding_id")
  private String holdingId;
  @Column(name = "holding_type")
  @Enumerated(EnumType.STRING)
  private HoldingType holdingType;

  @Column(name = "holding_id_type")
  @Enumerated(EnumType.STRING)
  private HoldingIdentifierType holdingIdType;

  @Column(name = "day_0_count")
  private int day0Count;
  @Column(name = "day_1_count")
  private int day1Count;
  @Column(name = "day_2_count")
  private int day2Count;
  @Column(name = "day_3_count")
  private int day3Count;
  @Column(name = "day_4_count")
  private int day4Count;
  @Column(name = "day_5_count")
  private int day5Count;
  @Column(name = "day_6_count")
  private int day6Count;

  public int getTotalNumberOfUsages() {
    return day0Count + day1Count + day2Count + day3Count
        + day4Count + day5Count + day6Count;
  }
}
