package com.fintex.ce.domain.model.calculation;

import java.util.Set;

public enum FixedIncomeCreditQuality {
  // credit ratings
  AAA(Set.of(CreditQualityRating.AAA)),
  AA(Set.of(CreditQualityRating.AA)),
  A(Set.of(CreditQualityRating.A)),
  BBB(Set.of(CreditQualityRating.BBB)),
  BB(Set.of(CreditQualityRating.BB)),
  B(Set.of(CreditQualityRating.B)),
  BELOW_B(Set.of(CreditQualityRating.BELOW_B)),

  INVESTMENT_GRADE(Set.of(CreditQualityRating.AAA, CreditQualityRating.AA, CreditQualityRating.A,
      CreditQualityRating.BBB)),
  HIGH_YIELD(Set.of(CreditQualityRating.BB, CreditQualityRating.B, CreditQualityRating.BELOW_B)),
  NOT_RATED(Set.of(CreditQualityRating.NOT_RATED));

  private Set<CreditQualityRating> ratings;

  FixedIncomeCreditQuality(final Set<CreditQualityRating> ratings) {
    this.ratings = ratings;
  }

  public boolean contains(final CreditQualityRating rating) {
    return this.ratings.contains(rating);
  }
}
