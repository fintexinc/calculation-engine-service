package api.model;

import com.fintex.ce.dto.exception.ErrorRes2DTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResDto {
    public Set<ErrorRes2DTO> errors;
}
