package api.util;

import com.fintex.ce.config.enumeration.Currency;
import com.fintex.ce.config.enumeration.HoldingType;
import com.fintex.ce.dto.holding.Holding;

import static api.testcases.core.CoreApiTest.GIC_CAD_KEYWORD;
import static api.testcases.core.CoreApiTest.GIC_USD_KEYWORD;
import static com.fintex.ce.config.constant.GeneralConstants.DELIMITER;
import static com.fintex.ce.config.enumeration.HoldingType.CASH;

public class IdentifierUtils {

    public static String cutUserIdentifier(final String userIdentifer) {
        return getId(userIdentifer);
    }

    public static String cutUserIdentifier(final Holding h) {
        final String str = h.generateUserIdentifier();
        return getId(str);
    }

    private static String getId(final String str) {
        final String[] split = str.split(DELIMITER);
        if (split[0].equalsIgnoreCase(HoldingType.GIC.name())) {
            return split[1].equalsIgnoreCase(Currency.CAD.name()) ? GIC_CAD_KEYWORD : GIC_USD_KEYWORD;
        }
        if (split.length == 3 || split.length == 2) {
            return split[1];
        }
        return split[0];
    }

    public static String getHoldingCode(final String key) {
        final String[] s = key.split("_");
        if (key.equals("CASH_CAD")) {
            return "CASH.C";
        }
        if (key.equals("CASH_USD")) {
            return "CASH.U";
        }
        if (s.length == 4) {
            final String stock = s[1];
            if ("STOCKS".equals(stock)) {
                return s[s.length - 2];
            }
        }
        final String code = s[s.length - 1];
        if (CASH.name().equals(code.split("\\.")[0])) {
            return key;
        } else {
            return code;
        }
    }
}
