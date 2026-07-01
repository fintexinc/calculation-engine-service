package com.fintex.ce.adapter.webclient.observability;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.regex.Pattern;

@Component
public class ExternalServiceObservability {

  public static final String OBSERVATION_NAME = "external.provider.request";

  static final String UNKNOWN = "unknown";
  static final String NONE = "none";
  static final String SUCCESS = "success";
  static final String ERROR = "error";

  static final String SERVICE_TAG = "external.service";
  static final String METHOD_TAG = "http.method";
  static final String ENDPOINT_TAG = "endpoint";
  static final String OUTCOME_TAG = "outcome";
  static final String EXCEPTION_TAG = "exception";

  static final String REQUEST_PATH_KEY = "request.path";
  static final String COMPLETED_EVENT = "external.provider.request.completed";
  static final String FAILED_EVENT = "external.provider.request.failed";

  private static final String ID_PATH_VARIABLE = "{id}";
  private static final String DATE_PATH_VARIABLE = "{date}";
  private static final Pattern UUID_SEGMENT = Pattern.compile(
      "(?i)^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$");
  private static final Pattern NUMERIC_SEGMENT = Pattern.compile("^\\d+$");
  private static final Pattern ISO_DATE_SEGMENT = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
  private static final Pattern OPAQUE_ID_SEGMENT = Pattern.compile("(?i)^(?=.*[a-z])(?=.*\\d)[a-z0-9_-]{16,}$");

  private final ObservationRegistry observationRegistry;

  @Autowired
  public ExternalServiceObservability(ObjectProvider<ObservationRegistry> observationRegistryProvider) {
    this(observationRegistryProvider.getIfAvailable(ObservationRegistry::create));
  }

  ExternalServiceObservability(ObservationRegistry observationRegistry) {
    this.observationRegistry = observationRegistry;
  }

  public <R> R observe(String serviceName, String httpMethod, String path, Supplier<R> action) {
    String serviceTag = valueOrUnknown(serviceName);
    String methodTag = valueOrUnknown(httpMethod);
    String endpointTag = endpointTag(path);

    Observation observation = Observation.createNotStarted(OBSERVATION_NAME, observationRegistry)
        .contextualName(serviceTag + " " + methodTag + " " + endpointTag)
        .lowCardinalityKeyValue(SERVICE_TAG, serviceTag)
        .lowCardinalityKeyValue(METHOD_TAG, methodTag)
        .lowCardinalityKeyValue(ENDPOINT_TAG, endpointTag)
        .highCardinalityKeyValue(REQUEST_PATH_KEY, valueOrUnknown(path));

    observation.start();
    try (Observation.Scope ignored = observation.openScope()) {
      R result = action.get();
      observation.lowCardinalityKeyValue(OUTCOME_TAG, SUCCESS);
      observation.lowCardinalityKeyValue(EXCEPTION_TAG, NONE);
      observation.event(Observation.Event.of(COMPLETED_EVENT));
      return result;
    } catch (RuntimeException exception) {
      observation.error(exception);
      observation.lowCardinalityKeyValue(OUTCOME_TAG, ERROR);
      observation.lowCardinalityKeyValue(EXCEPTION_TAG, exception.getClass().getSimpleName());
      observation.event(Observation.Event.of(FAILED_EVENT));
      throw exception;
    } finally {
      observation.stop();
    }
  }

  private static String endpointTag(String path) {
    String value = valueOrUnknown(path);
    int queryStart = value.indexOf('?');
    String pathOnly = queryStart < 0 ? value : value.substring(0, queryStart);
    return normalizePathSegments(pathOnly);
  }

  private static String normalizePathSegments(String path) {
    if (UNKNOWN.equals(path)) {
      return path;
    }
    return Arrays.stream(path.split("/", -1))
        .map(ExternalServiceObservability::normalizePathSegment)
        .reduce((left, right) -> left + "/" + right)
        .orElse(path);
  }

  private static String normalizePathSegment(String segment) {
    if (ISO_DATE_SEGMENT.matcher(segment).matches()) {
      return DATE_PATH_VARIABLE;
    }
    return isDynamicIdSegment(segment) ? ID_PATH_VARIABLE : segment;
  }

  private static boolean isDynamicIdSegment(String segment) {
    return UUID_SEGMENT.matcher(segment).matches()
        || NUMERIC_SEGMENT.matcher(segment).matches()
        || OPAQUE_ID_SEGMENT.matcher(segment).matches();
  }

  private static String valueOrUnknown(String value) {
    return value == null || value.isBlank() ? UNKNOWN : value;
  }
}
