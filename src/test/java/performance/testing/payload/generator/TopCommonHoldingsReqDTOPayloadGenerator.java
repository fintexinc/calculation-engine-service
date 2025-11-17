package performance.testing.payload.generator;

import com.fintex.ce.dto.request.TopCommonHoldingsReqDTO;

public class TopCommonHoldingsReqDTOPayloadGenerator extends PayloadGenerator<TopCommonHoldingsReqDTO> {

    @Override
    public TopCommonHoldingsReqDTO generatePayload() {
        final var topCommonHoldingsReqDTO = new TopCommonHoldingsReqDTO();
        topCommonHoldingsReqDTO.setHoldings(getHoldings());
        return topCommonHoldingsReqDTO;
    }

}
