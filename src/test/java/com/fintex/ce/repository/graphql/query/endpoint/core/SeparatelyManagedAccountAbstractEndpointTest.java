package com.fintex.ce.repository.graphql.query.endpoint.core;

import com.fintex.smclient.graphql.ExternalIdentifierType;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.SeparatelyManagedAccount;
import com.fintex.smclient.graphql.SmaIdentifier;
import com.fintex.smclient.graphql.SmaIdentifierType;
import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import com.fintex.ce.dto.holding.SmaHolding;
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

class SeparatelyManagedAccountAbstractEndpointTest {

    @Test
    void collectMorningstarIds_checkResult() {
        //SETUP
        final SeparatelyManagedAccountAbstractEndpoint sut = mock(SeparatelyManagedAccountAbstractEndpoint.class);

        final String identifier = "F00000XM7J";
        final SmaHolding smaHolding = mock(SmaHolding.class);

        when(smaHolding.getIdentifier()).thenReturn(identifier);
        when(smaHolding.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.MORNINGSTAR_ID);

        doCallRealMethod().when(sut).collectIds(any());

        //ACT
        final List<SmaIdentifier> actual = sut.collectIds(List.of(smaHolding));

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(SmaIdentifierType.MORNINGSTAR_ID, actual.get(0).getType());
        assertEquals(identifier, actual.get(0).getValue());
    }

    @Test
    void collectEnvestnetIds_checkResult() {
        //SETUP
        final SeparatelyManagedAccountAbstractEndpoint sut = mock(SeparatelyManagedAccountAbstractEndpoint.class);

        final String identifier = "ACM-BSCCE";
        final SmaHolding smaHolding = mock(SmaHolding.class);

        when(smaHolding.getIdentifier()).thenReturn(identifier);
        when(smaHolding.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.ENVESTNET_ID);

        doCallRealMethod().when(sut).collectIds(any());

        //ACT
        final List<SmaIdentifier> actual = sut.collectIds(List.of(smaHolding));

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(SmaIdentifierType.ENVESTNET_ID, actual.get(0).getType());
        assertEquals(identifier, actual.get(0).getValue());
    }

    @Test
    void findHoldingBasedOnRes_checkResult() {
        //SETUP
        final String identifier = "ACM-BSCCE";
        final SeparatelyManagedAccountAbstractEndpoint sut = mock(SeparatelyManagedAccountAbstractEndpoint.class);
        final SmaHolding smaHolding = mock(SmaHolding.class);
        final SeparatelyManagedAccount separatelyManagedAccount = mock(SeparatelyManagedAccount.class);
        final ExternalIdentifiers externalIdentifiers = mock(ExternalIdentifiers.class);
        final ExternalIdentifierTypeValue externalIdentifierTypeValue = mock(ExternalIdentifierTypeValue.class);

        when(smaHolding.getIdentifier()).thenReturn(identifier);
        when(separatelyManagedAccount.getExternalIdentifiers()).thenReturn(externalIdentifiers);
        when(externalIdentifiers.getCodes()).thenReturn(List.of(externalIdentifierTypeValue));
        when(externalIdentifierTypeValue.getType()).thenReturn(ExternalIdentifierType.ENVESTNET_ID);
        when(externalIdentifierTypeValue.getValue()).thenReturn(identifier);

        doCallRealMethod().when(sut).findHoldingBasedOnRes(any(), any());

        //ACT
        final SmaHolding actual = sut.findHoldingBasedOnRes(List.of(smaHolding), separatelyManagedAccount);

        //VERIFY
        assertEquals(smaHolding, actual);
    }

    @Test
    void basicResponseMapper_verifyResponseMapper() {
        //SETUP
        final SeparatelyManagedAccountAbstractEndpoint sut = mock(SeparatelyManagedAccountAbstractEndpoint.class);

        when(sut.responseMapper(any(), any())).thenReturn(mock(RedisId.class));

        final SmaHolding smaHolding = mock(SmaHolding.class);
        final SeparatelyManagedAccount entity = mock(SeparatelyManagedAccount.class);
        doCallRealMethod().when(sut).basicResponseMapper(any(), any());
        //ACT
        sut.basicResponseMapper(entity, smaHolding);

        //VERIFY
        verify(sut).responseMapper(entity, smaHolding);
    }

    @Test
    void populateEmptyEnvestnetId_checkResult() {
        //SETUP
        final String identifier = "ACM-BSCCE";
        final SeparatelyManagedAccountAbstractEndpoint sut = mock(SeparatelyManagedAccountAbstractEndpoint.class);
        final SmaHolding smaHolding = mock(SmaHolding.class);

        final SeparatelyManagedAccount separatelyManagedAccount = new SeparatelyManagedAccount();
        final var holdings = List.of(separatelyManagedAccount);

        Mockito.when(smaHolding.getIdentifier()).thenReturn(identifier);
        Mockito.when(smaHolding.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.ENVESTNET_ID);
        doCallRealMethod().when(sut).populateEmptyResponseWithIdentifier(anyList(), any());

        //ACT
        sut.populateEmptyResponseWithIdentifier(holdings, smaHolding);

        //VERIFY
        assertNotNull(separatelyManagedAccount.getExternalIdentifiers());
        assertNotNull(separatelyManagedAccount.getExternalIdentifiers().getCodes());
        assertEquals(1, separatelyManagedAccount.getExternalIdentifiers().getCodes().size());
        assertEquals(identifier, separatelyManagedAccount.getExternalIdentifiers().getCodes().get(0).getValue());
        assertEquals(ExternalIdentifierType.ENVESTNET_ID, separatelyManagedAccount.getExternalIdentifiers().getCodes().get(0).getType());
    }

    @Test
    void populateEmptyMorningstarId_checkResult() {
        //SETUP
        final String identifier = "F00000XM7J";
        final SeparatelyManagedAccountAbstractEndpoint sut = mock(SeparatelyManagedAccountAbstractEndpoint.class);
        final SmaHolding smaHolding = mock(SmaHolding.class);

        final SeparatelyManagedAccount separatelyManagedAccount = new SeparatelyManagedAccount();
        final var holdings = List.of(separatelyManagedAccount);

        Mockito.when(smaHolding.getIdentifier()).thenReturn(identifier);
        Mockito.when(smaHolding.getHoldingIdentifier()).thenReturn(HoldingIdentifierType.MORNINGSTAR_ID);
        doCallRealMethod().when(sut).populateEmptyResponseWithIdentifier(anyList(), any());

        //ACT
        sut.populateEmptyResponseWithIdentifier(holdings, smaHolding);

        //VERIFY
        assertNotNull(separatelyManagedAccount.getExternalIdentifiers());
        assertNotNull(separatelyManagedAccount.getExternalIdentifiers().getCodes());
        assertEquals(1, separatelyManagedAccount.getExternalIdentifiers().getCodes().size());
        assertEquals(identifier, separatelyManagedAccount.getExternalIdentifiers().getCodes().get(0).getValue());
        assertEquals(ExternalIdentifierType.MORNINGSTAR_ID, separatelyManagedAccount.getExternalIdentifiers().getCodes().get(0).getType());
    }

    @Test
    void getNotExistingHoldings_checkResult() {
        //SETUP
        final String identifier1 = "F00000XM7J";
        final String identifier2 = "ACM-BSCCE";

        final SeparatelyManagedAccountAbstractEndpoint sut = mock(SeparatelyManagedAccountAbstractEndpoint.class);
        final SmaHolding smaHolding1 = mock(SmaHolding.class);
        final SmaHolding smaHolding2 = mock(SmaHolding.class);
        final SeparatelyManagedAccount separatelyManagedAccount = mock(SeparatelyManagedAccount.class);
        final ExternalIdentifiers externalIdentifiers = mock(ExternalIdentifiers.class);
        final ExternalIdentifierTypeValue externalIdentifierTypeValue = mock(ExternalIdentifierTypeValue.class);

        when(smaHolding1.getIdentifier()).thenReturn(identifier1);
        when(smaHolding2.getIdentifier()).thenReturn(identifier2);
        when(separatelyManagedAccount.getExternalIdentifiers()).thenReturn(externalIdentifiers);
        when(externalIdentifiers.getCodes()).thenReturn(List.of(externalIdentifierTypeValue));
        when(externalIdentifierTypeValue.getType()).thenReturn(ExternalIdentifierType.MORNINGSTAR_ID);
        when(externalIdentifierTypeValue.getValue()).thenReturn(identifier1);

        doCallRealMethod().when(sut).getNotExistingHoldings(any(), any());

        //ACT
        final List<SmaHolding> actual = sut.getNotExistingHoldings(
                List.of(smaHolding1, smaHolding2),
                List.of(separatelyManagedAccount));

        //VERIFY
        assertEquals(1, actual.size());
        assertEquals(smaHolding2, actual.get(0));
    }

}
