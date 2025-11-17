package com.fintex.ce.dto.identifiers;

import com.fintex.ce.config.enumeration.HoldingIdentifierType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ExternalIdentifiersDTO {

    private HoldingIdentifierType identifierType;
    private String value;

}
