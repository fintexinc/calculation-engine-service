package api.testcases.v1x5.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class PortfolioRequestDTO {

  private String portfolioCurrency;
  private List<PortfolioHoldingDTO> portfolioHoldings;
  private List<PortfolioHoldingDTO> benchmarkHoldings;

  private Set<FactInputDTO> factInput = new HashSet<>();

  @JsonIgnore
  private LocalDate portfolioDate;

  public PortfolioRequestDTO(List<PortfolioHoldingDTO> portfolioHoldings, Set<FactInputDTO> factInput) {
    this.portfolioHoldings = portfolioHoldings;
    this.factInput = factInput;
  }
}
