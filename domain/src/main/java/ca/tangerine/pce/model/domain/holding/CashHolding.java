package ca.tangerine.pce.model.domain.holding;

import ca.tangerine.pce.model.domain.enumeration.InterestFreq;
import ca.tangerine.wm.commons.domain.currency.Currency;
import ca.tangerine.wm.commons.domain.enumeration.Country;
import ca.tangerine.wm.commons.domain.enumeration.FinancialInstrumentType;
import ca.tangerine.wm.commons.domain.id.SecurityIdentifier;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString(callSuper = true)
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public final class CashHolding extends PortfolioHolding implements MonthlyReturnGeneratableHolding {

  private final Currency currency;
  private final LocalDate investmentDate;
  private final BigDecimal clientIntRate;
  private final InterestFreq interestFreq;

  /**
   * Deserialization entry point, mirroring the creator on {@link PortfolioHolding}. Written out rather than generated
   * by {@code @Jacksonized} because that annotation emits Jackson 2 metadata, which Jackson 3 does not read.
   */
  @JsonCreator
  public CashHolding(
      @JsonProperty("value") BigDecimal value,
      @JsonProperty("holdingType") FinancialInstrumentType holdingType,
      @JsonProperty("country") Country country,
      @JsonProperty("securityIdentifier") SecurityIdentifier securityIdentifier,
      @JsonProperty("currency") Currency currency,
      @JsonProperty("investmentDate") LocalDate investmentDate,
      @JsonProperty("clientIntRate") BigDecimal clientIntRate,
      @JsonProperty("interestFreq") InterestFreq interestFreq) {
    super(value, holdingType, country, securityIdentifier);
    this.currency = currency;
    this.investmentDate = investmentDate;
    this.clientIntRate = clientIntRate;
    this.interestFreq = interestFreq;
  }

  public InterestFreq getInterestFreq() {
    if (Objects.isNull(interestFreq)) {
      return InterestFreq.ANNUAL;
    }
    return interestFreq;
  }

  public LocalDate getInvestmentDate() {
    if (Objects.isNull(investmentDate)) {
      return GicHolding.DEFAULT_START_DATE;
    }
    return investmentDate;
  }

  public boolean hasClientIntRate() {
    return clientIntRate != null;
  }

}
