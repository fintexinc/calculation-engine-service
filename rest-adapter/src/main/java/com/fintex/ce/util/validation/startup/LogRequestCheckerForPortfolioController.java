package com.fintex.ce.util.validation.startup;

import com.fintex.ce.adapter.rest.aop.annotation.LogRequest;
import com.fintex.ce.domain.exception.GeneralRuntimeException;
import com.fintex.ce.adapter.rest.controller.PortfolioController;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LogRequestCheckerForPortfolioController {

  public static boolean isPresentHttpServletRequestParameterInMethod(final Method method) {
    return Arrays.asList(method.getParameterTypes()).contains(HttpServletRequest.class);
  }

  /*
   * Check if all methods in PortfolioController that is annotated with PostMapping have HttpServletRequest parameter.
   * We need this to LogRequestAspect would work properly.
   * 
   * @see LogRequestAspect.class
   */
  public static void checkPortfolioControllerMethodsHavingHttpServletRequestParameterIfClassIsAnnotatedWithLogRequest() {
    final List<Method> incorrectMethods = new ArrayList<>();

    final Annotation[] annotations = PortfolioController.class.getAnnotations();
    for (Annotation annotation : annotations) {
      if (annotation instanceof LogRequest) {
        for (final var method : PortfolioController.class.getDeclaredMethods()) {
          if (Arrays.stream(method.getDeclaredAnnotations()).map(Annotation::annotationType).collect(Collectors
              .toList()).contains(PostMapping.class)) {
            if (!isPresentHttpServletRequestParameterInMethod(method)) {
              incorrectMethods.add(method);
            }
          }
        }

        if (!incorrectMethods.isEmpty()) {
          throw new GeneralRuntimeException(
              "Following methods in PortfolioController must have HttpServletRequest as parameter" +
                  " to ensure that request logging is working :\n- "
                  + String.join("\n- ", incorrectMethods.toString().split(", ")) + "\n");
        }
      }
    }
  }

}
