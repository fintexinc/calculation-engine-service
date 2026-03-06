package com.fintex.ce.domain.model.holding;

import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Indices (indexes) are a collection of investment products (usually stocks and fixed income) that act as a benchmark
 * for investment managers to measure themselves against (or for investors to measure investment managers against). One
 * of the most important characteristics of an index is how it performs over time (trailing returns, growth 10k, annual
 * returns, etc). We will need to calculate the performance of indices or a portfolio of indices for our downstream
 * applications to use. Moreover, some upcoming calculations will also require a "benchmark" to be included in the
 * calculation, and indices can be used for that purpose.
 * <p>
 * Uses only MORNINGSTAR_ID as an identifier
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class BenchmarkIndexHolding extends Holding {

  private String mrStarId;

  public BenchmarkIndexHolding() {
  }

  public BenchmarkIndexHolding setMrStarId(String mrStarId) {
    this.mrStarId = mrStarId;
    setSecurityIdentifier(new SecurityIdentifier(mrStarId, FiIdentifierType.MORNINGSTAR_ID));
    return this;
  }

  @Override
  public String generateUserIdentifier() {
    return mrStarId;
  }
}
