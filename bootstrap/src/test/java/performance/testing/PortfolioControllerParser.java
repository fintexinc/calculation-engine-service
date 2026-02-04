package performance.testing;

import com.fintex.ce.adapter.rest.controller.PortfolioController;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PortfolioControllerParser {

  public Map<String, Class> getCalculationNamesAndRequestTypes() {
    final List<Method> methods = List.of(PortfolioController.class.getDeclaredMethods());
    return methods.stream().collect(Collectors.toMap(e -> e.getName().replace("get", ""), e -> e
        .getParameterTypes()[0]));
  }

}
