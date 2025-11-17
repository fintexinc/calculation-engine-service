package com.fintex.ce.config.enumeration;

import com.fintex.ce.exception.SystemException;
import com.fintex.ce.exception.code.ErrorCode;

public enum DataProvider {

    BLACKROCK,
    EAGLE,
    METATAGSEXCEL,
    MORNINGSTAR,
    PHNDB,
    SEISMIC,
    MANAGEMENTFUNDEXCEL,
    UNKNOWN_VALUE,
    ENVESTNET,
    BROADRIDGE,
    PAG;

    public static DataProvider of(final com.fintex.smclient.graphql.DataProvider fdsProvider) {
        if (fdsProvider == null) {
            throw new SystemException("FDS data provider could not be empty", ErrorCode.INTERNAL_SERVER_ERROR);
        }
        final DataProvider dataProvider = of(fdsProvider.name());
        if (dataProvider == null) {
            final String message = String.format("FDS data provider %s doesn't match with existing data providers", fdsProvider);
            throw new SystemException(message, ErrorCode.INTERNAL_SERVER_ERROR);
        }
        return dataProvider;
    }

    public static DataProvider of(final String provider) {
        for (DataProvider value : values()) {
            if (value.name().equalsIgnoreCase(provider)) {
                return value;
            }
        }
        return null;
    }

    public static final DataProvider[] DEFAULT_PROVIDERS = {EAGLE, MORNINGSTAR};
}
