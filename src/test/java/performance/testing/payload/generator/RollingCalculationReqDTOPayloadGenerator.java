package performance.testing.payload.generator;

import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.request.RollingCalculationReqDTO;

public class RollingCalculationReqDTOPayloadGenerator extends PayloadGenerator<RollingCalculationReqDTO> {

    @Override
    public RollingCalculationReqDTO generatePayload() {
        final var rollingCalculationReqDTO = RollingCalculationReqDTOFactory.build(generatePeriodReqDTO());
        return rollingCalculationReqDTO;
    }

    static class RollingCalculationReqDTOFactory {
        static RollingCalculationReqDTO build(final PeriodsReqDTO periodsReqDTO) {
            final RollingCalculationReqDTO rollingCalculationReqDTO = new RollingCalculationReqDTO();
            rollingCalculationReqDTO.setHoldings(periodsReqDTO.getHoldings());
            rollingCalculationReqDTO.setBenchmarkHoldings(periodsReqDTO.getBenchmarkHoldings());
            rollingCalculationReqDTO.setCurrency(periodsReqDTO.getCurrency());
            return rollingCalculationReqDTO;
        }
    }

}
