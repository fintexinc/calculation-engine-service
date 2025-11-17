package com.fintex.ce.util.validation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.model.redis.core.RedisId;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DataProviderRequestHandlingValidator {

    private DataProviderRequestHandlingValidator() {
    }

    public static <T extends RedisId, U> void dataProviderCheckValidation(final List<DataProvider> providers,
                                                                          final Collection<T> responseFromFds,
                                                                          final BiFunction<T, U, T> actionFunction) {
        dataProviderCheckValidation(providers,
                responseFromFds,
                getDataProviderFunction(),
                actionFunction);
    }

    private static <T extends RedisId> Function<T, DataProvider> getDataProviderFunction() {
        return redisObject -> {
                if (StringUtils.isEmpty(redisObject.getProvider())) {
                    return null;
                } else {
                    return DataProvider.of(redisObject.getProvider());
                }
        };
    }

    public static <T extends RedisId, U> void dataProviderCheckValidation(final List<DataProvider> providers,
                                                                          final Collection<T> responseFromFds,
                                                                          final Function<T, DataProvider> getterForProvider,
                                                                          final BiFunction<T, U, T> actionFunction) {
        responseFromFds.forEach(value -> {
            final DataProvider dataProvider = getterForProvider.apply(value);
            if (Objects.isNull(dataProvider) || !providers.contains(dataProvider)) {
                actionFunction.apply(value, null);
            }
        });
    }

}
