package ca.tangerine.pce.application.returns;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import ca.tangerine.pce.application.returns.processor.ReturnsProcessor;
import ca.tangerine.pce.model.domain.calculation.returns.ReturnsData;

/**
 * Runs the ordered {@link ReturnsProcessor} chain for a given {@link ProcessingCase}. The case → ordered-processors map
 * is composed once at startup from the injected processor list (each processor declares its applicability via
 * {@link ReturnsProcessor#isApplicable(ProcessingCase)}); per-call dispatch is then a simple map lookup plus a
 * sequential fold. Extracted from {@code MonthlyReturnsService} so per-case pipeline strategies can share the same
 * runner without duplicating the wiring.
 */
@Component
public class ProcessorsRunner {

  private final Map<ProcessingCase, List<ReturnsProcessor>> processorsByCase;

  public ProcessorsRunner(List<ReturnsProcessor> processors) {
    this.processorsByCase = buildProcessorsByCase(processors);
  }

  /**
   * Threads {@code initial} through every processor registered for {@code processingCase}, in {@code @Order} sequence.
   * Each processor's output snapshot is the next processor's input, so the returned snapshot is the cumulative
   * transformation of all applicable processors for the chosen case.
   */
  public <T extends ReturnsData> ReturnsSnapshot<T> run(ReturnsSnapshot<T> initial,
      ProcessingContext processingContext, ProcessingCase processingCase) {
    List<ReturnsProcessor> processors = processorsByCase.get(processingCase);
    ReturnsSnapshot<T> current = initial;
    for (ReturnsProcessor processor : processors) {
      current = processor.process(current, processingContext);
    }
    return current;
  }

  private static Map<ProcessingCase, List<ReturnsProcessor>> buildProcessorsByCase(List<ReturnsProcessor> processors) {
    Map<ProcessingCase, List<ReturnsProcessor>> byCase = new EnumMap<>(ProcessingCase.class);
    for (ProcessingCase processingCase : ProcessingCase.values()) {
      byCase.put(processingCase, processors.stream()
          .filter(processor -> processor.isApplicable(processingCase))
          .toList());
    }
    return Map.copyOf(byCase);
  }
}
