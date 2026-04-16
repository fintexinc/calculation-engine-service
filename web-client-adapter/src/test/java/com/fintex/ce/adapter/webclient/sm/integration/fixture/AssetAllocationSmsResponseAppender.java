package com.fintex.ce.adapter.webclient.sm.integration.fixture;

import com.fintex.wm.commons.domain.allocation.AssetAllocation;
import com.fintex.wm.commons.domain.attribute.SecurityAttributeResult;
import com.fintex.wm.commons.domain.id.FiIdentifierType;
import com.fintex.wm.commons.domain.id.SecurityIdentifier;
import com.fintex.wm.commons.domain.value.NameValue;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a list of SMS-style {@link SecurityAttributeResult} rows for asset-allocation integration tests. Append one
 * security at a time (catalog can be broader than the holdings passed to {@code fetch}).
 */
public final class AssetAllocationSmsResponseAppender implements SmsResponseAppender<AssetAllocation, List<NameValue>> {

  private final List<SecurityAttributeResult<AssetAllocation>> rows = new ArrayList<>();

  @Override
  public AssetAllocationSmsResponseAppender append(
      String id, FiIdentifierType idType, List<NameValue> allocationRows) {
    SecurityIdentifier identifier = new SecurityIdentifier();
    identifier.setId(id);
    identifier.setIdType(idType);

    AssetAllocation allocation = new AssetAllocation();
    allocation.setAllocation(allocationRows);

    rows.add(securityAttributeResult(identifier, allocation));
    return this;
  }

  @Override
  public List<SecurityAttributeResult<AssetAllocation>> build() {
    return List.copyOf(rows);
  }

  private static <T> SecurityAttributeResult<T> securityAttributeResult(SecurityIdentifier identifier, T data) {
    SecurityAttributeResult<T> result = new SecurityAttributeResult<>();
    result.setIdentifier(identifier);
    result.setData(data);
    return result;
  }
}
