package com.fintex.ce.adapter.webclient.sm.mapper;

import com.fintex.ce.domain.model.CreditQuality;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.CreditQualityRatingType;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import com.fintex.sm.model.domain.rating.CreditQualityRatings;
import com.fintex.sm.model.domain.value.CreditQualityRatingTypeValue;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.A;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.AA;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.AAA;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.B;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.BB;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.BBB;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.BELOW_B;
import static com.fintex.sm.model.domain.enumeration.CreditQualityRatingType.NOT_RATED;
import static org.assertj.core.api.Assertions.assertThat;

class CreditQualityMapperTest {

  private final CreditQualityMapper mapper = new CreditQualityMapper();

  @Test
  void shouldMapAllFieldsCorrectly_whenResponseHasMultipleRatingsAndProvider() {
    var smsResponse = new CreditQualityRatings();
    smsResponse.setRatings(List.of(
        createRating(AAA, "15.5"),
        createRating(AA, "22.3"),
        createRating(A, "28.4"),
        createRating(BBB, "18.5"),
        createRating(BB, "8.7"),
        createRating(B, "4.2"),
        createRating(BELOW_B, "2.4")));
    smsResponse.setAverageCreditQualityRating("A");
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    Holding holding = createHolding("AGG.US");

    CreditQuality result = mapper.map(smsResponse, holding);

    assertThat(result.getRatings()).hasSize(7);
    assertThat(result.getRatings().get(AAA)).isEqualByComparingTo("15.5");
    assertThat(result.getRatings().get(AA)).isEqualByComparingTo("22.3");
    assertThat(result.getRatings().get(A)).isEqualByComparingTo("28.4");
    assertThat(result.getRatings().get(BBB)).isEqualByComparingTo("18.5");
    assertThat(result.getRatings().get(BB)).isEqualByComparingTo("8.7");
    assertThat(result.getRatings().get(B)).isEqualByComparingTo("4.2");
    assertThat(result.getRatings().get(BELOW_B)).isEqualByComparingTo("2.4");
    assertThat(result.getHoldingId()).isEqualTo("AGG.US");
    assertThat(result.getProvider()).isEqualTo(DataProvider.MORNINGSTAR.name());
  }

  @ParameterizedTest
  @MethodSource("nullAndEmptyResponses")
  void shouldReturnEmptyRatings_whenResponseIsNullOrHasNoRatings(CreditQualityRatings smsResponse) {
    Holding holding = createHolding("TEST.ID");

    CreditQuality result = mapper.map(smsResponse, holding);

    assertThat(result.getRatings()).isEmpty();
    assertThat(result.getHoldingId()).isEqualTo("TEST.ID");
    assertThat(result.getProvider()).isNull();
  }

  static Stream<Arguments> nullAndEmptyResponses() {
    var nullRatingsResponse = new CreditQualityRatings();
    nullRatingsResponse.setRatings(null);

    var emptyRatingsResponse = new CreditQualityRatings();
    emptyRatingsResponse.setRatings(List.of());

    return Stream.of(
        Arguments.of((CreditQualityRatings) null),
        Arguments.of(nullRatingsResponse),
        Arguments.of(emptyRatingsResponse));
  }

  @Test
  void shouldFilterOutEntriesWithNullRatingOrValue() {
    var validRating = createRating(AAA, "15.5");
    var nullRating = new CreditQualityRatingTypeValue(null, BigDecimal.valueOf(5.0), List.of());
    var nullValue = new CreditQualityRatingTypeValue(BB.name(), null, List.of());

    var smsResponse = new CreditQualityRatings();
    smsResponse.setRatings(List.of(validRating, nullRating, nullValue));

    CreditQuality result = mapper.map(smsResponse, createHolding("TEST.ID"));

    assertThat(result.getRatings()).hasSize(1);
    assertThat(result.getRatings()).containsKey(AAA);
  }

  @Test
  void shouldFilterOutUnknownRatingStrings() {
    var validRating = createRating(AAA, "15.5");
    var unknownRating = new CreditQualityRatingTypeValue("UNKNOWN", BigDecimal.valueOf(5.0), List.of());

    var smsResponse = new CreditQualityRatings();
    smsResponse.setRatings(List.of(validRating, unknownRating));

    CreditQuality result = mapper.map(smsResponse, createHolding("TEST.ID"));

    assertThat(result.getRatings()).hasSize(1);
    assertThat(result.getRatings()).containsKey(AAA);
  }

  @Test
  void shouldKeepFirstValue_whenDuplicateRatingsExist() {
    var smsResponse = new CreditQualityRatings();
    smsResponse.setRatings(List.of(
        createRating(AAA, "15.5"),
        createRating(AAA, "20.0")));

    CreditQuality result = mapper.map(smsResponse, createHolding("TEST.ID"));

    assertThat(result.getRatings()).hasSize(1);
    assertThat(result.getRatings().get(AAA)).isEqualByComparingTo("15.5");
  }

  @Test
  void shouldMapAllCreditQualityRatingTypes() {
    var smsResponse = new CreditQualityRatings();
    smsResponse.setRatings(List.of(
        createRating(AAA, "10.0"),
        createRating(AA, "15.0"),
        createRating(A, "20.0"),
        createRating(BBB, "25.0"),
        createRating(BB, "12.0"),
        createRating(B, "8.0"),
        createRating(BELOW_B, "5.0"),
        createRating(NOT_RATED, "5.0")));

    CreditQuality result = mapper.map(smsResponse, createHolding("FULL.TEST"));

    assertThat(result.getRatings()).hasSize(8);
    assertThat(result.getRatings().get(AAA)).isEqualByComparingTo("10.0");
    assertThat(result.getRatings().get(AA)).isEqualByComparingTo("15.0");
    assertThat(result.getRatings().get(A)).isEqualByComparingTo("20.0");
    assertThat(result.getRatings().get(BBB)).isEqualByComparingTo("25.0");
    assertThat(result.getRatings().get(BB)).isEqualByComparingTo("12.0");
    assertThat(result.getRatings().get(B)).isEqualByComparingTo("8.0");
    assertThat(result.getRatings().get(BELOW_B)).isEqualByComparingTo("5.0");
    assertThat(result.getRatings().get(NOT_RATED)).isEqualByComparingTo("5.0");
  }

  private CreditQualityRatingTypeValue createRating(CreditQualityRatingType rating, String value) {
    return new CreditQualityRatingTypeValue(rating.name(), new BigDecimal(value), List.of());
  }

  private Holding createHolding(String securityId) {
    return new Holding()
        .setHoldingType(FinancialInstrumentType.ETF_CANADA)
        .setSecurityIdentifier(new SecurityIdentifier(securityId, null));
  }
}