package com.fintex.ce.adapter.webclient.sm.dto;

import com.fintex.ce.domain.model.enumeration.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdsAndDataProvidersRequest {

    private List<TypedIdentifiers> typedIdentifiers;

    private List<DataProvider> dataProviders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypedIdentifiers {
        private String type;
        private List<SecurityIdentifier> ids;
    }
}
