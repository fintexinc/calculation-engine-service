package api.model;

import com.fintex.ce.domain.model.holding.CashHolding;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CashHoldingDTO extends CashHolding {

  private String holdingCode;

  @Override
  public String generateUserIdentifier() {
    return holdingCode;
  }
}
