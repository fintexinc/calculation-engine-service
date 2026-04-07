package com.fintex.ce.domain.model.calculation;

import com.fintex.sm.model.domain.enumeration.CreditQualityRatingType;

import java.util.Set;

public enum FixedIncomeCreditQuality {
  // credit ratings
  AAA(Set.of(CreditQualityRatingType.AAA)),
  AA(Set.of(CreditQualityRatingType.AA)),
  A(Set.of(CreditQualityRatingType.A)),
  BBB(Set.of(CreditQualityRatingType.BBB)),
  BB(Set.of(CreditQualityRatingType.BB)),
  B(Set.of(CreditQualityRatingType.B)),
  BELOW_B(Set.of(CreditQualityRatingType.BELOW_B)),

  INVESTMENT_GRADE(Set.of(CreditQualityRatingType.AAA, CreditQualityRatingType.AA, CreditQualityRatingType.A,
      CreditQualityRatingType.BBB)),
  HIGH_YIELD(Set.of(CreditQualityRatingType.BB, CreditQualityRatingType.B, CreditQualityRatingType.BELOW_B)),
  NOT_RATED(Set.of(CreditQualityRatingType.NOT_RATED));

  private Set<CreditQualityRatingType> ratings;

  FixedIncomeCreditQuality(final Set<CreditQualityRatingType> ratings) {
    this.ratings = ratings;
  }

  public boolean contains(final CreditQualityRatingType rating) {
    return this.ratings.contains(rating);
  }
}
