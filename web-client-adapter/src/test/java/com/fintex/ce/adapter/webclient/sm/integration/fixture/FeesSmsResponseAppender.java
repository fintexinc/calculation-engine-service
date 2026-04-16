package com.fintex.ce.adapter.webclient.sm.integration.fixture;

import com.fintex.wm.commons.domain.DataProvider;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.datapoint.FloatDatapoint;
import com.fintex.wm.commons.domain.financial.Fees;
import com.fintex.wm.commons.domain.financial.ManagementFeeDatapoint;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

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

    rows.add(securityAttributeResult(identifier, fees));
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

  private static <T> SecurityAttributeResult<T> securityAttributeResult(SecurityIdentifier identifier, T data) {
    SecurityAttributeResult<T> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(data);
    return result;
  }
}
