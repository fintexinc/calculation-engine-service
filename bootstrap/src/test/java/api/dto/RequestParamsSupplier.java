package api.dto;

import com.fintex.ce.domain.enumeration.Currency;
import com.fintex.ce.domain.enumeration.DataProvider;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * Params only used to call FDS
 */
@Data
public class RequestParamsSupplier {

  private List<DataProvider> dataProviders;
  private Currency portfolioCurrency;
  private Set<String> accumulativeTypes;

  public RequestParamsSupplier() {
  }

  public RequestParamsSupplier(List<DataProvider> dataProviders, Currency portfolioCurrency) {
    this.dataProviders = dataProviders;
    this.portfolioCurrency = portfolioCurrency;
  }

  public RequestParamsSupplier(List<DataProvider> dataProviders) {
    this.dataProviders = dataProviders;
  }

  public RequestParamsSupplier(Set<String> accumulativeTypes) {
    this.accumulativeTypes = accumulativeTypes;
  }
}
