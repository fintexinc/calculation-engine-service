package performance.testing.payload.generator;

import com.fintex.ce.adapter.rest.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;

import static performance.testing.RandomUtil.getRandomInt;

public class DistributionOfReturnsReqDTOPayloadGenerator extends PayloadGenerator<DistributionOfReturnsReqDTO> {

  @Override
  public DistributionOfReturnsReqDTO generatePayload() {
    final var result = DistributionOfReturnsReqDTOFactory.build(generatePeriodReqDTO());
    result.setCustomNumberOfBins(getRandomInt(5, 30));
    return result;
  }

  static class DistributionOfReturnsReqDTOFactory {
    static DistributionOfReturnsReqDTO build(final PeriodsReqDTO periodsReqDTO) {
      final DistributionOfReturnsReqDTO distributionOfReturnsReqDTO = new DistributionOfReturnsReqDTO();
      distributionOfReturnsReqDTO.setHoldings(periodsReqDTO.getHoldings());
      distributionOfReturnsReqDTO.setBenchmarkHoldings(periodsReqDTO.getBenchmarkHoldings());
      distributionOfReturnsReqDTO.setCurrency(periodsReqDTO.getCurrency());
      return distributionOfReturnsReqDTO;
    }
  }
}
