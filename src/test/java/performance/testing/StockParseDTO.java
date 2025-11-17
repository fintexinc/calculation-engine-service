package performance.testing;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockParseDTO {
    private String ticker;
    private String exchangeId;
}
