package com.fintex.ce.adapter.webclient.sm.integration.fixture;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.datapoint.Fees;
import com.fintex.sm.model.domain.datapoint.FloatDatapoint;
import com.fintex.sm.model.domain.datapoint.ManagementFeeDatapoint;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds SMS-style fee rows for fees integration tests. Each {@link #append} adds one security's {@link Fees} payload.
 */
public final class FeesSmsResponseAppender implements SmsResponseAppender<Fees, FeesSmsResponseAppender.FeesValues> {

  private final List<SecurityAttributeResult<Fees>> rows = new ArrayList<>();

  public record FeesValues(
      String managementFee,
      String managementExpenseRatio,
      String netExpenseRatio,
      String grossExpenseRatio,
      String actual12B1Fee) {
  }

  @Override
  public FeesSmsResponseAppender append(String id, FiIdentifierType idType, FeesValues values) {
    SecurityIdentifier identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);

    Fees fees = new Fees();
    fees.setManagementFee(managementFeeDatapoint(values.managementFee()));
    fees.setManagementExpenseRatio(floatDatapoint(values.managementExpenseRatio()));
    fees.setNetExpenseRatio(floatDatapoint(values.netExpenseRatio()));
    fees.setGrossExpenseRatio(floatDatapoint(values.grossExpenseRatio()));
    fees.setActual12B1Fee(floatDatapoint(values.actual12B1Fee()));

    rows.add(new SecurityAttributeResult<>(identifier, fees));
    return this;
  }

  @Override
  public List<SecurityAttributeResult<Fees>> build() {
    return List.copyOf(rows);
  }

  private static ManagementFeeDatapoint managementFeeDatapoint(String value) {
    ManagementFeeDatapoint dp = new ManagementFeeDatapoint();
    dp.setValue(new BigDecimal(value));
    dp.setDataProvider(DataProvider.MORNINGSTAR);
    return dp;
  }

  private static FloatDatapoint floatDatapoint(String value) {
    FloatDatapoint dp = new FloatDatapoint();
    dp.setValue(new BigDecimal(value));
    dp.setDataProvider(DataProvider.MORNINGSTAR);
    return dp;
  }
}
