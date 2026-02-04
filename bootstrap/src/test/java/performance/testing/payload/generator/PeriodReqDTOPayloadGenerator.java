package performance.testing.payload.generator;

import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;

public class PeriodReqDTOPayloadGenerator extends PayloadGenerator<PeriodsReqDTO> {

  @Override
  public PeriodsReqDTO generatePayload() {
    return generatePeriodReqDTO();
  }
}
