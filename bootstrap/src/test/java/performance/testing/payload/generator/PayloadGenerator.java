package performance.testing.payload.generator;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;
import performance.testing.HoldingFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static com.fintex.ce.domain.enumeration.HoldingType.*;
import static performance.testing.RandomUtil.getCurrency;
import static performance.testing.RandomUtil.getRandomInt;

public abstract class PayloadGenerator<T> {

  final static int NUMBER_OF_PAYLOADS = 300;
  protected HoldingFactory holdingFactory = new HoldingFactory();
  private static List<HoldingType> holdingTypes;

  static {
    holdingTypes = List.of(US_ETF, CANADA_ETF, CANADA_MUTUAL_FUNDS, US_STOCKS, CANADA_STOCKS);
  }

  public abstract T generatePayload();

  public List<T> generatePayloads() {
    final List<T> result = new ArrayList<>();
    for (int i = 0; i < NUMBER_OF_PAYLOADS; i++) {
      result.add(generatePayload());
    }
    return result;
  }

  public PeriodsReqDTO generatePeriodReqDTO() {
    final PeriodsReqDTO periodsReqDTO = new PeriodsReqDTO();
    periodsReqDTO.setHoldings(getHoldings());
    periodsReqDTO.setBenchmarkHoldings(getHoldings());
    periodsReqDTO.setCurrency(getCurrency());
    return periodsReqDTO;
  }

  protected List<Holding> getHoldings() {
    final int numberOfHoldings = getRandomInt(2, 10);
    holdingFactory.generateRandomIndexNumbersForEachHoldingSet(numberOfHoldings);
    final List<Holding> result = new ArrayList<>();
    result.add(getCashHolding());
    IntStream.range(0, numberOfHoldings).forEach(h -> {
      final int randomHoldingTypeNumber = getRandomHoldingTypeNumber();
      final HoldingType holdingType = holdingTypes.get(randomHoldingTypeNumber);
      result.add(holdingFactory.getHolding(holdingType));
    });
    return result;
  }

  private CashHolding getCashHolding() {
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setType(CASH);
    cashHolding.setValue(BigDecimal.valueOf(new Random().nextInt(100500)));
    return cashHolding;
  }

  private int getRandomHoldingTypeNumber() {
    return getRandomInt(1, holdingTypes.size());
  }

}
