package api.excel.reader;

import java.util.Map;

public interface HoldingReaderTab<T> extends TabReader<Map<String, T>> {
    String TAB_NAME = "Holdings_Data";

}
