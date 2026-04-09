package com.fintex.ce.application.validation;

import com.fintex.sm.model.DataProvider;

import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataProviderRequestHandlingValidator {

  private DataProviderRequestHandlingValidator() {
  }

  public static <T, U> void dataProviderCheckValidation(final List<DataProvider> allowedProviders,
      final Collection<T> responseFromFds,
      final Function<T, List<DataProvider>> getterForProviders,
      final List<DataProvider> defaultProviders,
      final BiFunction<T, U, T> actionFunction) {
    responseFromFds.forEach(value -> {
      List<DataProvider> itemProviders = getterForProviders.apply(value);
      if (CollectionUtils.isEmpty(itemProviders)) {
        itemProviders = defaultProviders;
      }
      if (CollectionUtils.isEmpty(allowedProviders)
          || CollectionUtils.isEmpty(itemProviders)
          || Collections.disjoint(itemProviders, allowedProviders)) {
        actionFunction.apply(value, null);
      }
    });
  }

}
