package performance.testing;

import com.fintex.ce.dto.request.AverageMerRequestDTO;
import com.fintex.ce.dto.request.BestWorstPeriodsReqDTO;
import com.fintex.ce.dto.request.CorrelationReqDTO;
import com.fintex.ce.dto.request.DistributionOfReturnsReqDTO;
import com.fintex.ce.dto.request.LeadingTotalReturnPeriodsReqDTO;
import com.fintex.ce.dto.request.MultiplePortfoliosReqDTO;
import com.fintex.ce.dto.request.PeriodsReqDTO;
import com.fintex.ce.dto.request.PortfolioHoldingsReqDTO;
import com.fintex.ce.dto.request.ReturnReqDTO;
import com.fintex.ce.dto.request.RollingCalculationReqDTO;
import com.fintex.ce.dto.request.RollingCorrelationCalculationReqDTO;
import com.fintex.ce.dto.request.TopCommonHoldingsReqDTO;
import performance.testing.payload.generator.AverageMerRequestDTOPayloadGenerator;
import performance.testing.payload.generator.BestWorstPeriodsReqDTOPayloadGenerator;
import performance.testing.payload.generator.DistributionOfReturnsReqDTOPayloadGenerator;
import performance.testing.payload.generator.LeadingTotalReturnPeriodsReqDTOPayloadGenerator;
import performance.testing.payload.generator.MultiplePortfoliosReqDTOPayloadGenerator;
import performance.testing.payload.generator.PayloadGenerator;
import performance.testing.payload.generator.PeriodReqDTOPayloadGenerator;
import performance.testing.payload.generator.PortfolioHoldingsReqDTOPayloadGenerator;
import performance.testing.payload.generator.ReturnReqDTOPayloadGenerator;
import performance.testing.payload.generator.RollingCalculationReqDTOPayloadGenerator;
import performance.testing.payload.generator.TopCommonHoldingsReqDTOPayloadGenerator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class Main {

    static PortfolioControllerParser portfolioControllerParser = new PortfolioControllerParser();

    static Map<Class, PayloadGenerator> map = Map.ofEntries(
            entry(PeriodsReqDTO.class, new PeriodReqDTOPayloadGenerator()),
            entry(CorrelationReqDTO.class, new PeriodReqDTOPayloadGenerator()),
            entry(RollingCorrelationCalculationReqDTO.class, new PeriodReqDTOPayloadGenerator()),
            entry(DistributionOfReturnsReqDTO.class, new DistributionOfReturnsReqDTOPayloadGenerator()),
            entry(AverageMerRequestDTO.class, new AverageMerRequestDTOPayloadGenerator()),
            entry(PortfolioHoldingsReqDTO.class, new PortfolioHoldingsReqDTOPayloadGenerator()),
            entry(LeadingTotalReturnPeriodsReqDTO.class, new LeadingTotalReturnPeriodsReqDTOPayloadGenerator()),
            entry(BestWorstPeriodsReqDTO.class, new BestWorstPeriodsReqDTOPayloadGenerator()),
            entry(MultiplePortfoliosReqDTO.class, new MultiplePortfoliosReqDTOPayloadGenerator()),
            entry(ReturnReqDTO.class, new ReturnReqDTOPayloadGenerator()),
            entry(TopCommonHoldingsReqDTO.class, new TopCommonHoldingsReqDTOPayloadGenerator()),
            entry(RollingCalculationReqDTO.class, new RollingCalculationReqDTOPayloadGenerator())
    );

    public static void main(String[] args) {
        final Map<String, Class> calculationNamesAndRequestTypes = portfolioControllerParser.getCalculationNamesAndRequestTypes();
        final Map<String, List<Object>> calculationNamesAndPayloads = generatePayloads(calculationNamesAndRequestTypes);
        new PayloadSerialization(calculationNamesAndPayloads).serialize();
    }

    public static <T> Map<String, List<T>> generatePayloads(final Map<String, Class> calculationNamesAndRequestTypes) {
        return calculationNamesAndRequestTypes.entrySet()
                .stream()
                .filter(e -> map.containsKey(e.getValue()))
                .collect(Collectors.toMap(Map.Entry::getKey, e -> map.get(e.getValue()).generatePayloads()));
    }

}
