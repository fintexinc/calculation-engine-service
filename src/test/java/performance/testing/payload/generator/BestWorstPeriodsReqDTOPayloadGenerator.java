package performance.testing.payload.generator;

import com.fintex.ce.dto.request.BestWorstPeriodsReqDTO;

import static performance.testing.RandomUtil.getCurrency;

public class BestWorstPeriodsReqDTOPayloadGenerator extends PayloadGenerator<BestWorstPeriodsReqDTO> {

    @Override
    public BestWorstPeriodsReqDTO generatePayload() {
        final var bestWorstPeriodsReqDTO = new BestWorstPeriodsReqDTO();
        bestWorstPeriodsReqDTO.setHoldings(getHoldings());
        bestWorstPeriodsReqDTO.setCurrency(getCurrency());
        return bestWorstPeriodsReqDTO;
    }

}
