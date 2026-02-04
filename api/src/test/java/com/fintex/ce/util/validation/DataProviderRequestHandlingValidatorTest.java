package com.fintex.ce.util.validation;

import com.fintex.ce.domain.enumeration.DataProvider;
import com.fintex.ce.domain.model.core.ProviderAware;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataProviderRequestHandlingValidatorTest {

  @Test
  void dataProviderCheckValidation_whenProviderIsNullWithDefaultProviders() {
    // SETUP
    final BiFunction function = mock(BiFunction.class);

    // ACT
    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(buildProviderAware(null)),
        function);

    // VERIFY
    verify(function).apply(any(), any());
  }

  @Test
  void dataProviderCheckValidation_whenProviderIsEmptyWithDefaultProviders() {
    // SETUP
    final BiFunction function = mock(BiFunction.class);

    // ACT
    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(buildProviderAware("")),
        function);

    // VERIFY
    verify(function).apply(any(), any());
  }

  @Test
  void dataProviderCheckValidation_whenProviderExistsWithEmptyProviders() {
    // SETUP
    final BiFunction function = mock(BiFunction.class);

    // ACT
    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(),
        List.of(buildProviderAware("EAGLE")),
        function);

    // VERIFY
    verify(function).apply(any(), any());
  }

  @Test
  void dataProviderCheckValidation_whenProviderExistsWithDefaultProviders() {
    // SETUP
    final BiFunction function = mock(BiFunction.class);

    // ACT
    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(buildProviderAware("EAGLE")),
        function);

    // VERIFY
    verifyNoInteractions(function);
  }

  private ProviderAware buildProviderAware(final String provider) {
    return () -> provider;
  }

}