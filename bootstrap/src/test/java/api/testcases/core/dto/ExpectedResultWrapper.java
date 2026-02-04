package api.testcases.core.dto;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Used to wrap expected result from EXCEL and define the rules if exceptions present on EXCEL
 *
 * @param <E>
 *          expected result type
 */
@Data
public class ExpectedResultWrapper<E> {
  private final HttpStatus httpStatus;
  private E validExpectedResult;
  private Consumer<String> onFailedResponseFromApp;

  public ExpectedResultWrapper(E validExpectedResult) {
    this.validExpectedResult = Objects.requireNonNull(validExpectedResult);
    httpStatus = HttpStatus.OK;
  }

  /**
   * Should only be used in case of 4xx or 5xx expected status code from APP
   *
   * @param httpStatus
   *          http status code
   * @param onFailedResponseFromApp
   *          will consume the response as string from APP and here we can add some assertions
   */
  public ExpectedResultWrapper(HttpStatus httpStatus, Consumer<String> onFailedResponseFromApp) {
    this.httpStatus = Objects.requireNonNull(httpStatus);
    this.onFailedResponseFromApp = Objects.requireNonNull(onFailedResponseFromApp);
  }

  public boolean is2xxSuccessful() {
    return httpStatus.is2xxSuccessful();
  }

}
