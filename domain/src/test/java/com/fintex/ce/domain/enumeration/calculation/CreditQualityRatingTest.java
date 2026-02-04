package com.fintex.ce.domain.enumeration.calculation;

import org.junit.jupiter.api.Test;

import static com.fintex.ce.domain.enumeration.calculation.CreditQualityRating.NOT_RATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CreditQualityRatingTest {

  @Test
  void of_checkResult() {
    // SETUP
    final CreditQualityRating rating = NOT_RATED;

    // ACT
    final CreditQualityRating actual = CreditQualityRating.of(rating.name());

    // VERIFY
    assertEquals(rating, actual);
  }

  @Test
  void of_checkResult2() {
    // SETUP
    final String rating = NOT_RATED.getRating();

    // ACT
    final CreditQualityRating actual = CreditQualityRating.of(rating);

    // VERIFY
    assertEquals(NOT_RATED, actual);
  }

  @Test
  void of_checkResult3() {
    // SETUP
    final String rating = "NotRated1";

    // ACT
    final CreditQualityRating actual = CreditQualityRating.of(rating);

    // VERIFY
    assertNull(actual);
  }

}