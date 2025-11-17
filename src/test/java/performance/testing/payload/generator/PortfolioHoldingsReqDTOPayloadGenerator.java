package performance.testing.payload.generator;

import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;

public class PortfolioHoldingsReqDTOPayloadGenerator extends PayloadGenerator<PortfolioHoldingsReqDTO> {

    @Override
    public PortfolioHoldingsReqDTO generatePayload() {
        final var portfolioHoldingsReqDTO = new PortfolioHoldingsReqDTO();
        portfolioHoldingsReqDTO.setHoldings(getHoldings());
        return portfolioHoldingsReqDTO;
    }

}
