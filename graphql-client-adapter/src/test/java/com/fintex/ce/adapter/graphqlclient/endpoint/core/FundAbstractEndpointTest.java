package com.fintex.ce.adapter.graphqlclient.endpoint.core;

import com.fintex.ce.adapter.graphqlclient.endpoint.core.FundAbstractEndpoint;
import com.fintex.ce.adapter.graphqlclient.endpoint.monthlyreturns.MonthlyReturnsFundCanadaEndpoint;
import com.fintex.ce.domain.enumeration.HoldingIdentifierType;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.smclient.graphql.ExternalIdentifierTypeValue;
import com.fintex.smclient.graphql.ExternalIdentifiers;
import com.fintex.smclient.graphql.FundHoldingIdentifier;
import com.fintex.smclient.graphql.FundHoldingIdentifiersCodes;
import com.fintex.smclient.graphql.FundSeries;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.fintex.ce.domain.enumeration.HoldingIdentifierType.FUNDSERV;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FundAbstractEndpointTest {

  @Test
  void collectIds_checkResult() {
    // SETUP
    final FundAbstractEndpoint e = mock(FundAbstractEndpoint.class);

    final FundSeriesHolding h = mock(FundSeriesHolding.class);
    final String code = "RBF540";
    final HoldingIdentifierType fundserv = FUNDSERV;
    when(h.getFundServCode()).thenReturn(code);
    when(h.getHoldingIdentifier()).thenReturn(fundserv);

    doCallRealMethod().when(e).collectIds(any());
    // ACT
    final List<FundHoldingIdentifiersCodes> actual = e.collectIds(List.of(h));

    // VERIFY
    assertEquals(1, actual.size());
    final FundHoldingIdentifiersCodes ex = new FundHoldingIdentifiersCodes(FundHoldingIdentifier.valueOf(fundserv
        .name()), code);
    assertEquals(ex.getCode(), actual.get(0).getCode());
    assertEquals(ex.getFundholdingIdentifier(), actual.get(0).getFundholdingIdentifier());
  }

  @Test
  void findHoldingBasedOnRes_checkResults() {
    // SETUP
    final FundAbstractEndpoint f = mock(FundAbstractEndpoint.class);
    final String code = "RBF540";

    final FundSeries fundSeries = mock(FundSeries.class);
    when(f.getIds(fundSeries)).thenReturn(List.of(code));

    final FundSeriesHolding fundSeriesHolding = mock(FundSeriesHolding.class);
    when(fundSeriesHolding.getFundServCode()).thenReturn(code);

    doCallRealMethod().when(f).findHoldingBasedOnRes(any(), any());
    // ACT
    final FundSeriesHolding actual = f.findHoldingBasedOnRes(List.of(fundSeriesHolding), fundSeries);

    // VERIFY
    assertEquals(fundSeriesHolding, actual);
  }

  @Test
  void getIds_checkResults() {
    // SETUP
    final FundAbstractEndpoint f = mock(FundAbstractEndpoint.class);
    final String code = "RBF540";

    final FundSeries fundSeries = mock(FundSeries.class);
    final ExternalIdentifiers identifiers = mock(ExternalIdentifiers.class);
    when(fundSeries.getExternalIdentifiers()).thenReturn(identifiers);
    final ExternalIdentifierTypeValue fundSeriesHolding = mock(ExternalIdentifierTypeValue.class);
    when(identifiers.getCodes()).thenReturn(List.of(fundSeriesHolding));
    when(fundSeriesHolding.getValue()).thenReturn(code);

    doCallRealMethod().when(f).getIds(any());
    // ACT
    final List actual = f.getIds(fundSeries);

    // VERIFY
    assertEquals(List.of(code), actual);
  }

  @Test
  void basicResponseMapper_verifyRsponseMapper() {
    // SETUP
    final FundAbstractEndpoint e = mock(FundAbstractEndpoint.class);

    when(e.responseMapper(any(), any())).thenReturn(mock(Object.class));

    final FundSeriesHolding h = mock(FundSeriesHolding.class);
    final FundSeries entity = mock(FundSeries.class);
    doCallRealMethod().when(e).basicResponseMapper(any(), any());
    // ACT
    e.basicResponseMapper(entity, h);

    // VERIFY
    verify(e).responseMapper(entity, h);
  }

  @Test
  void basicResponseMapper_checkResult() {
    // SETUP
    final FundAbstractEndpoint<Object> e = mock(FundAbstractEndpoint.class);

    final Object expected = mock(Object.class);
    when(e.responseMapper(any(), any())).thenReturn(expected);

    final FundSeriesHolding h = mock(FundSeriesHolding.class);
    when(h.generateUserIdentifier()).thenReturn("SDF");

    final FundSeries entity = mock(FundSeries.class);

    doCallRealMethod().when(e).basicResponseMapper(any(), any());
    // ACT
    final Object actual = e.basicResponseMapper(entity, h);

    // VERIFY
    assertSame(expected, actual);
  }

  @Test
  void populateEmptyResponseWithIdentifier_expectUnsupportedException() {
    // SETUP
    final var sut = new MonthlyReturnsFundCanadaEndpoint();

    // ACT
    assertThrows(UnsupportedOperationException.class,
        () -> sut.populateEmptyResponseWithIdentifier(List.of(mock(FundSeries.class)), mock(FundSeriesHolding.class)));

    // VERIFY

  }

  @Test
  void getNotExistingHoldings_emptyResponse() {
    // SETUP
    final var sut = new MonthlyReturnsFundCanadaEndpoint();

    // ACT
    assertThrows(UnsupportedOperationException.class,
        () -> sut.populateEmptyResponseWithIdentifier(List.of(mock(FundSeries.class)), mock(FundSeriesHolding.class)));

    // VERIFY

  }

  @Test
  void populateIdentifiersIfEmpty_checkResult() {
    // SETUP
    final var sut = new MonthlyReturnsFundCanadaEndpoint();
    final List responses = mock(List.class);
    final List holdings = mock(List.class);

    // ACT
    final List actual = sut.populateIdentifiersIfEmpty(holdings, responses);

    // VERIFY
    assertEquals(responses, actual);
  }

}