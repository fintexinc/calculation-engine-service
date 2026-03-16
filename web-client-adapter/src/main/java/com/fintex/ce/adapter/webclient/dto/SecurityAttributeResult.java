package com.fintex.ce.adapter.webclient.dto;

import com.fintex.sm.model.domain.SecurityIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper matching SMS API SecurityAttributeResult format.
 * Each result contains the security identifier and its associated attribute data.
 *
 * @param <T> The attribute data type (e.g., AssetAllocation, EquitySector)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAttributeResult<T> {

    private SecurityIdentifier identifier;

    private T data;
}
