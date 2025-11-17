package com.fintex.ce.util.graphql;

import com.fintex.ce.config.enumeration.DataProvider;
import com.fintex.smclient.graphql.FloatDatapoint;
import com.fintex.smclient.graphql.ManagementFeeDatapoint;

import java.math.BigDecimal;

public class GraphqlUtil {

    private GraphqlUtil() {
    }

    public static BigDecimal getBigDecimalOrNull(final FloatDatapoint floatDatapoint) {
        return floatDatapoint == null ? null : floatDatapoint.getValue();
    }

    public static DataProvider getDataProviderOrNull(final FloatDatapoint floatDatapoint) {
        return floatDatapoint == null ? null : DataProvider.of(floatDatapoint.getDataProvider());
    }

    public static DataProvider getDataProviderOrNull(final ManagementFeeDatapoint managementFeeDatapoint) {
        return managementFeeDatapoint == null ? null : DataProvider.of(managementFeeDatapoint.getDataProvider());
    }

    public static BigDecimal getBigDecimalOrNull(final ManagementFeeDatapoint managementFeeDatapoint) {
        return managementFeeDatapoint == null ? null : managementFeeDatapoint.getValue();
    }

}
