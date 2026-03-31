package com.fintex.ce.application.validation;

import com.fintex.ce.domain.model.core.ProviderAware;
import com.fintex.ce.domain.model.enumeration.DataProvider;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;

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
      final DataProvider dataProvider = StringUtils.isEmpty(providerStr) ? null : DataProvider.fromValue(providerStr);
      if (Objects.isNull(dataProvider) || !providers.contains(dataProvider)) {
        actionFunction.apply(value, null);
      }
    });
  }

}