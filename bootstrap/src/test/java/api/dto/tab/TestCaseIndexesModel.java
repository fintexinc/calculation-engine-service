package api.dto.tab;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class TestCaseIndexesModel {

  private Integer fromIndex;
  private Integer toIndex;

}
