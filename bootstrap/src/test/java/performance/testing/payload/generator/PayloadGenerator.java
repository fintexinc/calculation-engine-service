package performance.testing.payload.generator;

import com.fintex.ce.adapter.rest.dto.request.PeriodsReqDTO;
import com.fintex.ce.domain.model.holding.CashHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import performance.testing.HoldingFactory;

import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.CASH;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_US;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.MUTUAL_FUND_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.STOCK_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.STOCK_US;
import static performance.testing.RandomUtil.getCurrency;
import static performance.testing.RandomUtil.getRandomInt;

public abstract class PayloadGenerator<T> {

  final static int NUMBER_OF_PAYLOADS = 300;
  protected HoldingFactory holdingFactory = new HoldingFactory();
  private static List<FinancialInstrumentType> holdingTypes;

  static {
    holdingTypes = List.of(ETF_US, ETF_CANADA, MUTUAL_FUND_CANADA, STOCK_US, STOCK_CANADA);
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
      final FinancialInstrumentType holdingType = holdingTypes.get(randomHoldingTypeNumber);
      result.add(holdingFactory.getHolding(holdingType));
    });
    return result;
  }

  private CashHolding getCashHolding() {
    final CashHolding cashHolding = new CashHolding();
    cashHolding.setHoldingType(CASH);
    cashHolding.setValue(BigDecimal.valueOf(new Random().nextInt(100500)));
    return cashHolding;
  }

  private int getRandomHoldingTypeNumber() {
    return getRandomInt(1, holdingTypes.size());
  }

}
