package com.fintex.ce.application.validation;

import com.fintex.sm.model.DataProvider;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class DataProviderRequestHandlingValidatorTest {

  @Test
  void dataProviderCheckValidation_whenProvidersIsNullAndNoDefaults_shouldTriggerAction() {
    final BiFunction function = mock(BiFunction.class);

    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(new Item(null)),
        Item::getProviders,
        List.of(),
        function);

    verify(function).apply(any(), any());
  }

  @Test
  void dataProviderCheckValidation_whenProvidersIsEmptyAndNoDefaults_shouldTriggerAction() {
    final BiFunction function = mock(BiFunction.class);

    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(new Item(List.of())),
        Item::getProviders,
        List.of(),
        function);

    verify(function).apply(any(), any());
  }

  @Test
  void dataProviderCheckValidation_whenProvidersIsNullAndDefaultsMatchAllowed_shouldNotTriggerAction() {
    final BiFunction function = mock(BiFunction.class);

    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(new Item(null)),
        Item::getProviders,
        List.of(DataProvider.MORNINGSTAR),
        function);

    verifyNoInteractions(function);
  }

  @Test
  void dataProviderCheckValidation_whenProvidersIsEmptyAndDefaultsMatchAllowed_shouldNotTriggerAction() {
    final BiFunction function = mock(BiFunction.class);

    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(new Item(List.of())),
        Item::getProviders,
        List.of(DataProvider.MORNINGSTAR),
        function);

    verifyNoInteractions(function);
  }

  @Test
  void dataProviderCheckValidation_whenProviderExistsWithEmptyAllowed_shouldTriggerAction() {
    final BiFunction function = mock(BiFunction.class);

    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(),
        List.of(new Item(List.of(DataProvider.MORNINGSTAR))),
        Item::getProviders,
        List.of(DataProvider.MORNINGSTAR),
        function);

    verify(function).apply(any(), any());
  }

  @Test
  void dataProviderCheckValidation_whenProviderExistsAndMatchesAllowed_shouldNotTriggerAction() {
    final BiFunction function = mock(BiFunction.class);

    DataProviderRequestHandlingValidator.dataProviderCheckValidation(
        List.of(DataProvider.values()),
        List.of(new Item(List.of(DataProvider.MORNINGSTAR))),
        Item::getProviders,
        List.of(),
        function);

    verifyNoInteractions(function);
  }

  private record Item(List<DataProvider> providers) {
    List<DataProvider> getProviders() {
      return providers;
    }
  }

}
