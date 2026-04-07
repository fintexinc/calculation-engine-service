package com.fintex.ce.application.validation;

import com.fintex.ce.domain.model.core.ProviderAware;
import com.fintex.sm.model.DataProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataProviderRequestHandlingValidator {

  private DataProviderRequestHandlingValidator() {
  }

  public static <T extends ProviderAware, U> void dataProviderCheckValidation(final List<DataProvider> providers,
      final Collection<T> responseFromFds,
      final BiFunction<T, U, T> actionFunction) {
    dataProviderCheckValidation(providers, responseFromFds, ProviderAware::getProvider, actionFunction);
  }

  public static <T extends ProviderAware, U> void dataProviderCheckValidation(final List<DataProvider> providers,
      final Collection<T> responseFromFds,
      final Function<T, String> getterForProvider,
      final BiFunction<T, U, T> actionFunction) {
    responseFromFds.forEach(value -> {
      final String providerStr = getterForProvider.apply(value);
      DataProvider dataProvider = null;
      if (!StringUtils.isEmpty(providerStr)) {
        try { // todo remove when all dataProvider fields are migrated from String -> enum
          dataProvider = DataProvider.valueOf(providerStr.toUpperCase());
        } catch (IllegalArgumentException ignored) {
        }
      }
      if (Objects.isNull(dataProvider) || !providers.contains(dataProvider)) {
        actionFunction.apply(value, null);
      }
    });
  }

}