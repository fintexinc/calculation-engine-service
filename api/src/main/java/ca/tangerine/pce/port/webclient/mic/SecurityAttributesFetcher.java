package ca.tangerine.pce.port.webclient.mic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ca.tangerine.pce.model.domain.holding.PortfolioHolding;
import ca.tangerine.pce.model.domain.security.SecurityData;
import ca.tangerine.wm.commons.domain.DataProvider;
import ca.tangerine.wm.commons.domain.enumeration.CompositeSecurityAttribute;

/**
 * Generic port for fetching Market Investment Catalogue attributes for the supplied holdings. A single attribute is
 * fetched through the strongly typed MIC endpoint ({@code /attributes/{attributeName}}); any combination of attributes
 * is fetched in one request through the composite endpoint ({@code /attributes}). The returned data is already mapped
 * to the CE domain types the calculation services consume; the attribute-to-type mapping is owned by the adapter-side
 * binding registry.
 */
public interface SecurityAttributesFetcher {

  <T> Map<PortfolioHolding, T> fetch(List<? extends PortfolioHolding> holdings, CompositeSecurityAttribute attribute,
      List<DataProvider> providers);

  SecurityData fetch(List<? extends PortfolioHolding> holdings, Collection<CompositeSecurityAttribute> attributes,
      List<DataProvider> providers);

}
