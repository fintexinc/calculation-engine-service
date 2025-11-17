package com.fintex.ce.config.enumeration;

import com.fintex.ce.exception.SystemException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataProviderTest {

    @Test
    void of_checkResult() {
        //SETUP

        //ACT
        final DataProvider actual = DataProvider.of(com.fintex.smclient.graphql.DataProvider.EAGLE);

        //VERIFY
        assertEquals(DataProvider.EAGLE, actual);
    }

    @Test
    void of_checkResult2() {
        //SETUP

        //ACT
        final DataProvider actual = DataProvider.of(com.fintex.smclient.graphql.DataProvider.MORNINGSTAR);

        //VERIFY
        assertEquals(DataProvider.MORNINGSTAR, actual);
    }

    @Test
    void of_checkResult3() {
        //SETUP

        com.fintex.smclient.graphql.DataProvider d = null;

        //ACT
        assertThrows(SystemException.class, () -> DataProvider.of(d));
    }

}