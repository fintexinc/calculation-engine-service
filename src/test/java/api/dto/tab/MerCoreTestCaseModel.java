package api.dto.tab;

import com.fintex.ce.config.enumeration.ParameterType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class MerCoreTestCaseModel extends CoreTestCaseModel {
    private List<ParameterType> merParam;
}

