package api.excel.writer.tab;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.dto.holding.Holding;
import com.fintex.ce.model.redis.RMonthlyReturns;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class MonthlyReturnTab {
    public void setDefaultHoldings(List<Holding> defaultHoldings) {
        // TODO
    }

    protected abstract Map<Holding, Map<LocalDate, BigDecimal>> validateMonthlyReturns(Map<Holding, RMonthlyReturns> monthlyReturns);

    protected abstract Map<LocalDate, BigDecimal> getTBills(Set<LocalDate> endDates, Currency currency);
}
