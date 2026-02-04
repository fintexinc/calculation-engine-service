package api.testcases.core.dto;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Used to trigger the test case execution right inside the test implementation. In this way we are able to write all
 * the logs just for specific test case and trigger any assertion during any step without interrupting the whole test
 * suite
 *
 * @param <E>
 */
public class TestCaseExecutor<E> {

  // used to actually trigger the whole test case execution
  private final Supplier<Results<E>> testCaseExecutor;
  // just to show to the user the current test case number
  private final Integer testCaseNumber;

  public TestCaseExecutor(Supplier<Results<E>> testCaseExecutor, Integer testCaseNumber) {
    this.testCaseExecutor = Objects.requireNonNull(testCaseExecutor);
    this.testCaseNumber = Objects.requireNonNull(testCaseNumber);
  }

  /**
   * Should be executed only at once for the same test case! During the first execution the result of this method should
   * be stored into any variable or field and this method should not be called twice!
   *
   * @return results for the test case
   */
  public Results<E> invokeTestCaseExecution() {
    return this.testCaseExecutor.get();
  }

  @Override
  public String toString() {
    return "Executor for Test Case Number " + testCaseNumber;
  }
}
