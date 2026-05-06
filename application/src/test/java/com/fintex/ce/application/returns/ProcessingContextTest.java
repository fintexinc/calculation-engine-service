package com.fintex.ce.application.returns;

import com.fintex.wm.commons.domain.currency.Currency;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProcessingContextTest {

  @Test
  void shouldDefaultFxToEmpty_whenFxIsNull() {
    ProcessingContext context = ProcessingContext.of(LocalDate.parse("2020-01-31"),
        LocalDate.parse("2024-12-31"), null);

    assertThat(context.fx()).isNotNull();
    assertThat(context.fx().conversionRequired()).isFalse();
    assertThat(context.cpsd()).isEqualTo(LocalDate.parse("2020-01-31"));
    assertThat(context.cped()).isEqualTo(LocalDate.parse("2024-12-31"));
  }

  @Test
  void shouldRetainProvidedFx_whenFxIsNotNull() {
    FxContext fxContext = new FxContext(Map.of(), Currency.CAD);

    ProcessingContext context = ProcessingContext.of(null, null, fxContext);

    assertThat(context.fx()).isSameAs(fxContext);
    assertThat(context.cpsd()).isNull();
    assertThat(context.cped()).isNull();
  }
}
