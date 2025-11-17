package api.excel.writer.tab;

import api.excel.writer.WritableSpreadsheet;
import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import lombok.extern.log4j.Log4j2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static api.util.CommonTools.CURRENCY_TRADING_PROVIDER;

@Log4j2
public class CommonPerformanceDatesMonthlyReturnTab extends MonthlyReturnTab implements WritableSpreadsheet {

    @Override
    protected Map<Holding, Map<LocalDate, BigDecimal>> validateMonthlyReturns(final Map<Holding, RMonthlyReturns> monthlyReturns) {
        return monthlyReturns.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getReturns()));
    }

    @Override
    protected Map<LocalDate, BigDecimal> getTBills(final Set<LocalDate> endDates, final Currency currency) {
        return CURRENCY_TRADING_PROVIDER.loadTreasuryBillsBy(com.fintex.smclient.enumeration.Currency.of(currency.name()));
    }
}