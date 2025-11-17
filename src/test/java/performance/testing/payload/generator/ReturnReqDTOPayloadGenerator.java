package performance.testing.payload.generator;

import com.fintex.ce.dto.request.ReturnReqDTO;

import static com.fintex.ce.config.enumeration.Currency.CAD;

public class ReturnReqDTOPayloadGenerator extends PayloadGenerator<ReturnReqDTO> {

    @Override
    public ReturnReqDTO generatePayload() {
        final var returnReqDTO = new ReturnReqDTO();
        returnReqDTO.setHoldings(getHoldings());
        returnReqDTO.setCurrency(CAD);
        return returnReqDTO;
    }

}
