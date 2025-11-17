package performance.testing.payload.generator;

import com.fintex.ce.dto.request.LeadingTotalReturnPeriodsReqDTO;

import static performance.testing.RandomUtil.getCurrency;

public class LeadingTotalReturnPeriodsReqDTOPayloadGenerator extends PayloadGenerator<LeadingTotalReturnPeriodsReqDTO> {

    @Override
    public LeadingTotalReturnPeriodsReqDTO generatePayload() {
        final var leadingTotalReturnPeriodsReqDTO = new LeadingTotalReturnPeriodsReqDTO();
        leadingTotalReturnPeriodsReqDTO.setHoldings(getHoldings());
        leadingTotalReturnPeriodsReqDTO.setCurrency(getCurrency());
        return leadingTotalReturnPeriodsReqDTO;
    }

}
