package com.fintex.ce.dto.response;

import com.fintex.ce.dto.response.core.DatesResDTO;
import com.fintex.ce.dto.response.core.KeyValueDTO;
import com.fintex.ce.dto.response.core.Warning;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AnnualReturnResDTO<T> extends DatesResDTO {

    private List<KeyValueDTO<T>> annualReturns;
    private List<Warning> warnings;
}
