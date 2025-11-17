package api.testcases.core.dto;

import api.excel.writer.WritableSpreadsheet;
import lombok.Data;

import java.util.Objects;

@Data
public class TabInfoDTO {
    private final WritableSpreadsheet tab;
    // if TRUE then tab will be populated before each test case specifically for test case holdings / because of different Data Providers
    // if FALSE then tab will be populated only at once at the beginning of testing for all holdings from HOLDINGS_DATA tab
    private final boolean beforeEachTest;

    public TabInfoDTO(WritableSpreadsheet tab, boolean beforeEachTest) {
        this.tab = Objects.requireNonNull(tab);
        this.beforeEachTest = beforeEachTest;
    }

    public TabInfoDTO(WritableSpreadsheet tab) {
        this.tab = Objects.requireNonNull(tab);
        this.beforeEachTest = false;
    }
}
