package ca.tangerine.pce.webclient.boc.mapper;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static ca.tangerine.pce.webclient.boc.client.FxRateSource.Frequency.DAILY;
import static ca.tangerine.pce.webclient.boc.client.FxRateSource.Frequency.MONTHLY;
import static org.assertj.core.api.Assertions.assertThat;

import ca.tangerine.pce.webclient.boc.dto.BankOfCanadaFxRateResponse;
import ca.tangerine.pce.webclient.boc.dto.BankOfCanadaFxRateResponse.Observation;

class BankOfCanadaFxRateMapperTest {

  private static final String SERIES_NAME = "FXUSDCAD";
  private static final List<String> SERIES_NAMES = List.of(SERIES_NAME);

  private final BankOfCanadaFxRateMapper mapper = new BankOfCanadaFxRateMapper();

  // --- MONTHLY ---

  @Test
  void monthly_shouldMapToMonthEndRates() {
    var response = response(
        observation("2024-01-15", "1.3400"),
        observation("2024-01-31", "1.3450"),
        observation("2024-02-15", "1.3500"),
        observation("2024-02-28", "1.3550"));

    Map<LocalDate, BigDecimal> result = mapper.map(response, SERIES_NAMES, MONTHLY);

    assertThat(result).hasSize(2);
    assertThat(result.get(LocalDate.of(2024, 1, 31))).isEqualByComparingTo("1.3450");
    assertThat(result.get(LocalDate.of(2024, 2, 29))).isEqualByComparingTo("1.3550");
  }

  @Test
  void monthly_shouldTakeLastObservationPerMonth() {
    var response = response(
        observation("2024-03-01", "1.3000"),
        observation("2024-03-15", "1.3100"),
        observation("2024-03-28", "1.3200"));

    Map<LocalDate, BigDecimal> result = mapper.map(response, SERIES_NAMES, MONTHLY);

    assertThat(result).hasSize(1);
    assertThat(result.get(LocalDate.of(2024, 3, 31))).isEqualByComparingTo("1.3200");
  }

  @Test
  void monthly_shouldHandleMultipleMonths() {
    var response = response(
        observation("2024-01-31", "1.3400"),
        observation("2024-02-29", "1.3500"),
        observation("2024-03-28", "1.3600"));

    Map<LocalDate, BigDecimal> result = mapper.map(response, SERIES_NAMES, MONTHLY);

    assertThat(result).hasSize(3);
    assertThat(result).containsKey(LocalDate.of(2024, 1, 31));
    assertThat(result).containsKey(LocalDate.of(2024, 2, 29));
    assertThat(result).containsKey(LocalDate.of(2024, 3, 31));
  }

  // --- DAILY ---

  @Test
  void daily_shouldKeepAllObservationsByActualDate() {
    var response = response(
        observation("2024-01-02", "1.3400"),
        observation("2024-01-03", "1.3450"),
        observation("2024-01-04", "1.3500"));

    Map<LocalDate, BigDecimal> result = mapper.map(response, SERIES_NAMES, DAILY);

    assertThat(result).hasSize(3);
    assertThat(result.get(LocalDate.of(2024, 1, 2))).isEqualByComparingTo("1.3400");
    assertThat(result.get(LocalDate.of(2024, 1, 3))).isEqualByComparingTo("1.3450");
    assertThat(result.get(LocalDate.of(2024, 1, 4))).isEqualByComparingTo("1.3500");
  }

  @Test
  void daily_shouldSkipObservationsWithMissingSeries() {
    var obsWithValue = observation("2024-01-02", "1.3400");
    var obsWithoutValue = new Observation();
    obsWithoutValue.setDate("2024-01-03");

    var response = response(obsWithValue, obsWithoutValue);

    Map<LocalDate, BigDecimal> result = mapper.map(response, SERIES_NAMES, DAILY);

    assertThat(result).hasSize(1);
    assertThat(result).containsKey(LocalDate.of(2024, 1, 2));
  }

  // --- COMMON ---

  @Test
  void shouldReturnEmptyMapForNullResponse() {
    assertThat(mapper.map(null, SERIES_NAMES, DAILY)).isEmpty();
  }

  @Test
  void shouldReturnEmptyMapForNullObservations() {
    assertThat(mapper.map(new BankOfCanadaFxRateResponse(), SERIES_NAMES, DAILY)).isEmpty();
  }

  @Test
  void shouldReturnEmptyMapForEmptyObservations() {
    var response = new BankOfCanadaFxRateResponse();
    response.setObservations(List.of());

    assertThat(mapper.map(response, SERIES_NAMES, MONTHLY)).isEmpty();
  }

  @Test
  void shouldReturnEmptyMapForEmptySeriesNames() {
    var response = response(observation("2024-01-15", "1.3400"));

    assertThat(mapper.map(response, List.of(), DAILY)).isEmpty();
  }

  @Test
  void shouldUseFirstAvailableSeriesFromList() {
    var response = response(observation("2024-01-31", "1.3400"));

    Map<LocalDate, BigDecimal> result = mapper.map(response, List.of(SERIES_NAME, "FXEURCAD"), MONTHLY);

    assertThat(result).hasSize(1);
    assertThat(result.get(LocalDate.of(2024, 1, 31))).isEqualByComparingTo("1.3400");
  }

  private BankOfCanadaFxRateResponse response(Observation... observations) {
    var response = new BankOfCanadaFxRateResponse();
    response.setObservations(List.of(observations));
    return response;
  }

  private Observation observation(String date, String value) {
    var obs = new Observation();
    obs.setDate(date);
    obs.setDynamicProperty(SERIES_NAME, Map.of("v", value));
    return obs;
  }
}