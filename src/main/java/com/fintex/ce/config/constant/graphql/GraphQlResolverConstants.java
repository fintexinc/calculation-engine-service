package com.fintex.ce.config.constant.graphql;

import com.fintex.smclient.graphql.ExternalIdentifiersQueryDefinition;
import com.fintex.smclient.graphql.FloatDatapointQueryDefinition;
import com.fintex.smclient.graphql.ManagementFeeDatapointQueryDefinition;
import com.fintex.smclient.graphql.StringDatapointQuery;
import com.fintex.smclient.graphql.StringDatapointQueryDefinition;

public class GraphQlResolverConstants {

    public static final ManagementFeeDatapointQueryDefinition MANAGEMENT_FEE_DATAPOINT_QUERY_DEFINITION = mf -> mf.value().dataProvider();
    public static final FloatDatapointQueryDefinition FLOAT_WITH_DATA_PROVIDER_QUERY_DEFINITION = d -> d.value().dataProvider();

    public static final StringDatapointQueryDefinition STRING_DATAPOINT_QUERY_DEFINITION = StringDatapointQuery::value;
    public static final StringDatapointQueryDefinition STRING_WITH_DATA_PROVIDER_DEFINITION = q -> q.value().dataProvider();

    public static final ExternalIdentifiersQueryDefinition EXTERNAL_IDENTIFIERS_QUERY_DEFINITION = id -> id.codes(
            qCode -> qCode.value().type()
    );

    private GraphQlResolverConstants() {
    }

}
