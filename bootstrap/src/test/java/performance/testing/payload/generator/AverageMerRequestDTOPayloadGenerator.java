package performance.testing.payload.generator;

import com.fintex.ce.adapter.rest.dto.request.AverageMerRequestDTO;

public class AverageMerRequestDTOPayloadGenerator extends PayloadGenerator<AverageMerRequestDTO> {

  @Override
  public AverageMerRequestDTO generatePayload() {
    final var averageMerRequestDTO = new AverageMerRequestDTO();
    averageMerRequestDTO.setHoldings(getHoldings());
    return averageMerRequestDTO;
  }

}
