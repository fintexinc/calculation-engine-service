package com.fintex.ce.util.validation;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.ce.model.redis.core.RedisId;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataProviderRequestHandlingValidatorTest {

    @Test
    void dataProviderCheckValidation_whenProviderIsNullWithDefaultProviders() {
        //SETUP
        final BiFunction function = mock(BiFunction.class);

        //ACT
        DataProviderRequestHandlingValidator.dataProviderCheckValidation(
                List.of(DataProvider.values()),
                List.of(buildRedisId(null)),
                function);

        //VERIFY
        verify(function).apply(any(), any());
    }

    @Test
    void dataProviderCheckValidation_whenProviderIsEmptyWithDefaultProviders() {
        //SETUP
        final BiFunction function = mock(BiFunction.class);

        //ACT
        DataProviderRequestHandlingValidator.dataProviderCheckValidation(
                List.of(DataProvider.values()),
                List.of(buildRedisId("")),
                function);

        //VERIFY
        verify(function).apply(any(), any());
    }

    @Test
    void dataProviderCheckValidation_whenProviderExistsWithEmptyProviders() {
        //SETUP
        final BiFunction function = mock(BiFunction.class);

        //ACT
        DataProviderRequestHandlingValidator.dataProviderCheckValidation(
                List.of(),
                List.of(buildRedisId("EAGLE")),
                function);

        //VERIFY
        verify(function).apply(any(), any());
    }

    @Test
    void dataProviderCheckValidation_whenProviderExistsWithDefaultProviders() {
        //SETUP
        final BiFunction function = mock(BiFunction.class);

        //ACT
        DataProviderRequestHandlingValidator.dataProviderCheckValidation(
                List.of(DataProvider.values()),
                List.of(buildRedisId("EAGLE")),
                function);

        //VERIFY
        verifyNoInteractions(function);
    }

    private RedisId buildRedisId(final String provider) {
        final RedisId redisId = new RedisId() {};
        redisId.setProvider(provider);
        return redisId;
    }

}