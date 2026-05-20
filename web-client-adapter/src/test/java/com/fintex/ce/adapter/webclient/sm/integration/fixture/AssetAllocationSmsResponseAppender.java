package com.fintex.ce.adapter.webclient.sm.integration.fixture;

import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.allocation.AssetAllocationValue;
import com.fintex.wm.commons.domain.allocation.AssetAllocationWithCurrency;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a list of SMS-style {@link SecurityAttributeResult} rows for asset-allocation integration tests. Append one
 * security at a time (catalog can be broader than the holdings passed to {@code fetch}).
 */
public final class AssetAllocationSmsResponseAppender
    implements
      SmsResponseAppender<AssetAllocationWithCurrency, List<AssetAllocationValue>> {

  private final List<SecurityAttributeResult<AssetAllocationWithCurrency>> rows = new ArrayList<>();

  @Override
  public AssetAllocationSmsResponseAppender append(
      String id, FiIdentifierType idType, List<AssetAllocationValue> allocationRows) {
    SecurityIdentifier identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);

    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocations(allocationRows);

    AssetAllocationWithCurrency wrapper = new AssetAllocationWithCurrency();
    wrapper.setAssetAllocation(allocation);

    rows.add(securityAttributeResult(identifier, wrapper));
    return this;
  }

  @Override
  public List<SecurityAttributeResult<AssetAllocationWithCurrency>> build() {
    return List.copyOf(rows);
  }

  private static <T> SecurityAttributeResult<T> securityAttributeResult(SecurityIdentifier identifier, T data) {
    SecurityAttributeResult<T> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(data);
    return result;
  }
}
