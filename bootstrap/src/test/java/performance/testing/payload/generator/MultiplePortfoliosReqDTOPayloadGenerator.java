package performance.testing.payload.generator;

import com.fintex.ce.adapter.rest.dto.request.MultiplePortfoliosReqDTO;

import java.util.HashSet;
import java.util.Set;

import static com.fintex.ce.adapter.rest.dto.request.MultiplePortfoliosReqDTO.Portfolio;
import static performance.testing.RandomUtil.getRandomInt;

public class MultiplePortfoliosReqDTOPayloadGenerator extends PayloadGenerator<MultiplePortfoliosReqDTO> {

  @Override
  public MultiplePortfoliosReqDTO generatePayload() {
    final var portfoliosReqDTO = new MultiplePortfoliosReqDTO();
    portfoliosReqDTO.setBenchmarkHoldings(getHoldings());
    portfoliosReqDTO.setPortfolios(getPortfolios());
    return portfoliosReqDTO;
  }

  private Set<Portfolio> getPortfolios() {
    final int randomInt = getRandomInt(1, 3);
    final Set<Portfolio> portfolios = new HashSet<>();
    for (int i = 0; i < randomInt; i++) {
      portfolios.add(new Portfolio(getHoldings()));
    }
    return portfolios;
  }

}
