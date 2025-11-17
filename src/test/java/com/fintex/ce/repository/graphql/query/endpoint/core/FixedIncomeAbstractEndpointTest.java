package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.ExternalIdentifierType;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.FixedIncome;
import com.fintex.ce.dto.holding.FixedIncomeHolding;
import com.fintex.ce.model.redis.core.RedisId;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixedIncomeAbstractEndpointTest {

    @Test
    void collectIds_checkResult() {
        //SETUP
        final FixedIncomeAbstractEndpoint sut = mock(FixedIncomeAbstractEndpoint.class);

        final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
        when(fixedIncomeHolding.getIdentifier()).thenReturn("adpNumber");

        doCallRealMethod().when(sut).collectIds(any());

        //ACT
        final List actual = sut.collectIds(List.of(fixedIncomeHolding));

        //VERIFY
        assertEquals(List.of(fixedIncomeHolding.getIdentifier()), actual);
    }

    @Test
    void findHoldingBasedOnRes_checkResult() {
        //SETUP
        final String adpNumber = "adpNumber";
        final FixedIncomeAbstractEndpoint sut = mock(FixedIncomeAbstractEndpoint.class);
        final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
        final FixedIncome fixedIncome = mock(FixedIncome.class);
        final ExternalIdentifiers externalIdentifiers = mock(ExternalIdentifiers.class);
        final ExternalIdentifierTypeValue externalIdentifierTypeValue = mock(ExternalIdentifierTypeValue.class);

        when(fixedIncomeHolding.getIdentifier()).thenReturn(adpNumber);
        when(fixedIncome.getExternalIdentifiers()).thenReturn(externalIdentifiers);
        when(externalIdentifiers.getCodes()).thenReturn(List.of(externalIdentifierTypeValue));
        when(externalIdentifierTypeValue.getType()).thenReturn(ExternalIdentifierType.BROADRIDGE_ADP_NUMBER);
        when(externalIdentifierTypeValue.getValue()).thenReturn(adpNumber);

        doCallRealMethod().when(sut).findHoldingBasedOnRes(any(), any());

        //ACT
        final FixedIncomeHolding actual = sut.findHoldingBasedOnRes(List.of(fixedIncomeHolding), fixedIncome);

        //VERIFY
        assertEquals(fixedIncomeHolding, actual);
    }

    @Test
    void basicResponseMapper_verifyResponseMapper() {
        //SETUP
        final FixedIncomeAbstractEndpoint sut = mock(FixedIncomeAbstractEndpoint.class);

        when(sut.responseMapper(any(), any())).thenReturn(mock(RedisId.class));

        final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);
        final FixedIncome entity = mock(FixedIncome.class);
        doCallRealMethod().when(sut).basicResponseMapper(any(), any());
        //ACT
        sut.basicResponseMapper(entity, fixedIncomeHolding);

        //VERIFY
        verify(sut).responseMapper(entity, fixedIncomeHolding);
    }

    @Test
    void populateEmptyAdpNumber_checkResult() {
        //SETUP
        final String adpNumber = "adpNumber";
        final FixedIncomeAbstractEndpoint sut = mock(FixedIncomeAbstractEndpoint.class);
        final FixedIncomeHolding fixedIncomeHolding = mock(FixedIncomeHolding.class);

        final FixedIncome fixedIncome = new FixedIncome();
        final var holdings = List.of(fixedIncome);

        Mockito.when(fixedIncomeHolding.getIdentifier()).thenReturn(adpNumber);
        doCallRealMethod().when(sut).populateEmptyResponseWithIdentifier(anyList(), any());

        //ACT
        sut.populateEmptyResponseWithIdentifier(holdings, fixedIncomeHolding);

        //VERIFY
        assertNotNull(fixedIncome.getExternalIdentifiers());
        assertNotNull(fixedIncome.getExternalIdentifiers().getCodes());
        assertEquals(1, fixedIncome.getExternalIdentifiers().getCodes().size());
        assertEquals(adpNumber, fixedIncome.getExternalIdentifiers().getCodes().get(0).getValue());
        assertEquals(ExternalIdentifierType.BROADRIDGE_ADP_NUMBER, fixedIncome.getExternalIdentifiers().getCodes().get(0).getType());
    }

    @Test
    void getNotExistingHoldings_checkResult() {
        //SETUP
        final String adpNumber = "adpNumber";
        final String adpNumber2 = "adpNumber2";
        final FixedIncomeAbstractEndpoint sut = mock(FixedIncomeAbstractEndpoint.class);
        final FixedIncomeHolding fixedIncomeHolding1 = mock(FixedIncomeHolding.class);
        final FixedIncomeHolding fixedIncomeHolding2 = mock(FixedIncomeHolding.class);
        final FixedIncome fixedIncome = mock(FixedIncome.class);
        final ExternalIdentifiers externalIdentifiers = mock(ExternalIdentifiers.class);
        final ExternalIdentifierTypeValue externalIdentifierTypeValue = mock(ExternalIdentifierTypeValue.class);

        when(fixedIncomeHolding1.getIdentifier()).thenReturn(adpNumber);
        when(fixedIncomeHolding2.getIdentifier()).thenReturn(adpNumber2);
        when(fixedIncome.getExternalIdentifiers()).thenReturn(externalIdentifiers);
        when(externalIdentifiers.getCodes()).thenReturn(List.of(externalIdentifierTypeValue));
        when(externalIdentifierTypeValue.getType()).thenReturn(ExternalIdentifierType.BROADRIDGE_ADP_NUMBER);
        when(externalIdentifierTypeValue.getValue()).thenReturn(adpNumber);

        doCallRealMethod().when(sut).getNotExistingHoldings(any(), any());

        //ACT
        final List<FixedIncomeHolding> actual = sut.getNotExistingHoldings(
                List.of(fixedIncomeHolding1, fixedIncomeHolding2),
                List.of(fixedIncome));

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(fixedIncomeHolding2, actual.get(0));
    }

}
