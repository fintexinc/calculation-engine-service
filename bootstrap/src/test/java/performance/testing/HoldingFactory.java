package performance.testing;

import com.fintex.ce.domain.enumeration.HoldingType;
import com.fintex.ce.domain.model.holding.EtfHolding;
import com.fintex.ce.domain.model.holding.FundSeriesHolding;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.ce.domain.model.holding.StockHolding;
import com.fintex.sm.model.domain.SecurityIdentifier;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static com.fintex.ce.domain.enumeration.HoldingType.CANADA_ETF;
import static com.fintex.ce.domain.enumeration.HoldingType.CANADA_MUTUAL_FUNDS;
import static com.fintex.ce.domain.enumeration.HoldingType.CANADA_STOCKS;
import static com.fintex.ce.domain.enumeration.HoldingType.US_ETF;
import static com.fintex.ce.domain.enumeration.HoldingType.US_STOCKS;
import static com.fintex.sm.model.domain.enumeration.FiIdentifierType.FUNDSERV;
import static com.fintex.sm.model.domain.enumeration.FiIdentifierType.TICKER;
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

  public Holding getHolding(final HoldingType holdingType) {
    switch (holdingType) {
      case US_ETF :
        return getEtf(usEtfs, US_ETF, randomIndexesForUsEtfs);
      case CANADA_ETF :
        return getEtf(canadaEtfs, CANADA_ETF, randomIndexesForCanadaEtfs);
      case CANADA_MUTUAL_FUNDS :
        return getMutualFund();
      case US_STOCKS :
        return getStock(usStocks, US_STOCKS, randomIndexesForUsStocks);
      case CANADA_STOCKS :
        return getStock(canadaStocks, CANADA_STOCKS, randomIndexesForCanadaStocks);
      default :
        throw new IllegalArgumentException("Invalid holding type : " + holdingType);
    }
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

  private Holding getEtf(final List<String> codes, final HoldingType holdingType,
      final LinkedList<Integer> randomIndexes) {
    final int randomIndex = randomIndexes.pop();
    final var etfHolding = new EtfHolding();
    final String ticker = codes.get(randomIndex);
    etfHolding.setSecurityIdentifier(new SecurityIdentifier(ticker, TICKER));
    etfHolding.setType(holdingType);
    etfHolding.setValue(BigDecimal.valueOf(randomIndex));
    etfHolding.setTicker(ticker);
    return etfHolding;
  }

  private Holding getMutualFund() {
    final int randomIndex = randomIndexesForMutualFunds.pop();
    final var mutualFund = new FundSeriesHolding();
    final String fundServCode = mutualFunds.get(randomIndex);
    mutualFund.setSecurityIdentifier(new SecurityIdentifier(fundServCode, FUNDSERV));
    mutualFund.setType(CANADA_MUTUAL_FUNDS);
    mutualFund.setValue(BigDecimal.valueOf(randomIndex));
    mutualFund.setFundServCode(fundServCode);
    return mutualFund;
  }

  private Holding getStock(final List<StockParseDTO> codes, final HoldingType holdingType,
      LinkedList<Integer> randomIndexes) {
    final int randomIndex = randomIndexes.pop();
    final var stockHolding = new StockHolding();
    final String ticker = codes.get(randomIndex).getTicker();
    stockHolding.setSecurityIdentifier(new SecurityIdentifier(ticker, TICKER));
    stockHolding.setType(holdingType);
    stockHolding.setValue(BigDecimal.valueOf(randomIndex));
    stockHolding.setTicker(ticker);
    stockHolding.setExchangeCode(codes.get(randomIndex).getExchangeId());
    return stockHolding;
  }

  private int getRandomIndex(final List<?> codes) {
    return getRandomInt(1, codes.size());
  }

}
