package api.testcases.v1x5.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
public class FactInputDTO {

    private String name;
    private Map<String, Object> parameters;

    public FactInputDTO(final String name) {
        this.name = name;
    }

    public FactInputDTO(final String name, final Map<String, Object> parameters) {
        this.name = name;
        this.parameters = parameters;
    }


}
