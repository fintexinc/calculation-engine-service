package com.fintex.ce.port.webclient.sm;

import com.fintex.ce.model.domain.holding.PortfolioHolding;
import com.fintex.ce.model.domain.security.SecurityData;
import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.enumeration.CompositeSecurityAttribute;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Generic port for fetching Security Master attributes for the supplied holdings. A single attribute is fetched through
 * the strongly typed SMS endpoint ({@code /attributes/{attributeName}}); any combination of attributes is fetched in
 * one request through the composite endpoint ({@code /attributes}). The returned data is already mapped to the CE
 * domain types the calculation services consume; the attribute-to-type mapping is owned by the adapter-side binding
 * registry.
 */
public interface SecurityAttributesFetcher {

  <T> Map<PortfolioHolding, T> fetch(List<? extends PortfolioHolding> holdings, CompositeSecurityAttribute attribute,
      List<DataProvider> providers);

  SecurityData fetch(List<? extends PortfolioHolding> holdings, Collection<CompositeSecurityAttribute> attributes,
      List<DataProvider> providers);

}
