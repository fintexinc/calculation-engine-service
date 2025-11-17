package com.fintex.ce.util.graphql;

import com.fintex.smclient.graphql.FloatDatapoint;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphqlUtilTest {

    @Test
    void getOrNull_isNotNull() {
        //SETUP
        final FloatDatapoint f = mock(FloatDatapoint.class);
        when(f.getValue()).thenReturn(BigDecimal.ONE);

        //ACT
        final BigDecimal actual = GraphqlUtil.getBigDecimalOrNull(f);

        //VERIFY
        assertEquals(f.getValue(), actual);
    }

    @Test
    void getOrNull_isNull() {
        //SETUP
        final FloatDatapoint f = mock(FloatDatapoint.class);

        //ACT
        final BigDecimal actual = GraphqlUtil.getBigDecimalOrNull(f);

        //VERIFY
        assertNull(actual);
    }

}