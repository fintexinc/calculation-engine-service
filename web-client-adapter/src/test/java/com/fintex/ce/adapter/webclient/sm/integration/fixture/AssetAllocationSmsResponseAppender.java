package com.fintex.ce.adapter.webclient.sm.integration.fixture;

import com.fintex.ce.adapter.webclient.sm.dto.SecurityAttributeResult;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.allocation.AssetAllocation;
import com.fintex.sm.model.domain.enumeration.FiIdentifierType;
import com.fintex.sm.model.domain.value.NameValue;

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

    rows.add(new SecurityAttributeResult<>(identifier, allocation));
    return this;
  }

  @Override
  public List<SecurityAttributeResult<AssetAllocation>> build() {
    return List.copyOf(rows);
  }
}
