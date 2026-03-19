package performance.testing;

import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.domain.EquitySecurityIdentifier;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static com.fintex.sm.model.domain.enumeration.FiIdentifierType.FUNDSERV;
import static com.fintex.sm.model.domain.enumeration.FiIdentifierType.TICKER;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.ETF_US;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.MUTUAL_FUND_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.STOCK_CANADA;
import static com.fintex.sm.model.domain.enumeration.FinancialInstrumentType.STOCK_US;
import static performance.testing.RandomUtil.getRandomInt;

public class HoldingFactory {

  private static final List<String> usEtfs;
  private static final List<String> canadaEtfs;
  private static final List<String> mutualFunds;
  private static final List<String> benchmarksMorningStar;
  private static final List<StockParseDTO> canadaStocks;
  private static final List<StockParseDTO> usStocks;

  private final LinkedList<Integer> randomIndexesForUsEtfs = new LinkedList<>();
  private final LinkedList<Integer> randomIndexesForCanadaEtfs = new LinkedList<>();
  private final LinkedList<Integer> randomIndexesForMutualFunds = new LinkedList<>();
  private final LinkedList<Integer> randomIndexesForBenchmarksMorningStar = new LinkedList<>();
  private final LinkedList<Integer> randomIndexesForCanadaStocks = new LinkedList<>();
  private final LinkedList<Integer> randomIndexesForUsStocks = new LinkedList<>();

  static {
    usEtfs = ParserUtil.parse("us_tickers.txt");
    canadaEtfs = ParserUtil.parse("canada_tickers.txt");
    mutualFunds = ParserUtil.parse("fund_serv_codes.txt");
    benchmarksMorningStar = ParserUtil.parse("benchmark_morningstar_ids.txt");
    canadaStocks = ParserUtil.parseStocks("canada_stock_ticker-exchange_id.txt");
    usStocks = ParserUtil.parseStocks("us_stock_ticker-exchange_id.txt");
  }

  public Holding getHolding(final FinancialInstrumentType holdingType) {
    return switch (holdingType) {
      case ETF_US -> getEtf(usEtfs, ETF_US, randomIndexesForUsEtfs);
      case ETF_CANADA -> getEtf(canadaEtfs, ETF_CANADA, randomIndexesForCanadaEtfs);
      case MUTUAL_FUND_CANADA -> getMutualFund();
      case STOCK_US -> getStock(usStocks, STOCK_US, randomIndexesForUsStocks);
      case STOCK_CANADA -> getStock(canadaStocks, STOCK_CANADA, randomIndexesForCanadaStocks);
      default -> throw new IllegalArgumentException("Invalid holding type : " + holdingType);
    };
  }

  public void generateRandomIndexNumbersForEachHoldingSet(final int numberOfHoldings) {
    populateRandomIndexSet(numberOfHoldings, randomIndexesForUsEtfs, usEtfs);
    populateRandomIndexSet(numberOfHoldings, randomIndexesForCanadaEtfs, canadaEtfs);
    populateRandomIndexSet(numberOfHoldings, randomIndexesForMutualFunds, mutualFunds);
    populateRandomIndexSet(numberOfHoldings, randomIndexesForBenchmarksMorningStar, benchmarksMorningStar);
    populateRandomIndexSet(numberOfHoldings, randomIndexesForCanadaStocks, canadaStocks);
    populateRandomIndexSet(numberOfHoldings, randomIndexesForUsStocks, usStocks);
  }

  private void populateRandomIndexSet(final int numberOfHoldings, final LinkedList<Integer> randomIndexes,
      final List<?> parsedHoldingIdentifiers) {
    randomIndexes.clear();
    while (randomIndexes.size() < numberOfHoldings) {
      final int randomIndex = getRandomIndex(parsedHoldingIdentifiers);
      if (!randomIndexes.contains(randomIndex)) {
        randomIndexes.add(randomIndex);
      }
    }
  }

  private Holding getEtf(final List<String> codes, final FinancialInstrumentType holdingType,
      final LinkedList<Integer> randomIndexes) {
    final int randomIndex = randomIndexes.pop();
    final var holding = new Holding();
    final String ticker = codes.get(randomIndex);
    holding.setSecurityIdentifier(new SecurityIdentifier(ticker, TICKER));
    holding.setHoldingType(holdingType);
    holding.setValue(BigDecimal.valueOf(randomIndex));
    return holding;
  }

  private Holding getMutualFund() {
    final int randomIndex = randomIndexesForMutualFunds.pop();
    final var holding = new Holding();
    final String fundServCode = mutualFunds.get(randomIndex);
    holding.setSecurityIdentifier(new SecurityIdentifier(fundServCode, FUNDSERV));
    holding.setHoldingType(MUTUAL_FUND_CANADA);
    holding.setValue(BigDecimal.valueOf(randomIndex));
    return holding;
  }

  private Holding getStock(final List<StockParseDTO> codes, final FinancialInstrumentType holdingType,
      LinkedList<Integer> randomIndexes) {
    final int randomIndex = randomIndexes.pop();
    final var holding = new Holding();
    final String ticker = codes.get(randomIndex).getTicker();
    final EquitySecurityIdentifier secId = new EquitySecurityIdentifier();
    secId.setId(ticker);
    secId.setIdType(TICKER);
    secId.setExchangeId(codes.get(randomIndex).getExchangeId());
    holding.setSecurityIdentifier(secId);
    holding.setHoldingType(holdingType);
    holding.setValue(BigDecimal.valueOf(randomIndex));
    return holding;
  }

  private int getRandomIndex(final List<?> codes) {
    return getRandomInt(1, codes.size());
  }

}
