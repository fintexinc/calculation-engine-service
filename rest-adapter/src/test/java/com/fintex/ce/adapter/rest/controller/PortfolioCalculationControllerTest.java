package com.fintex.ce.adapter.rest.controller;

import com.fintex.ce.adapter.rest.dto.response.core.ErrorDTO;
import com.fintex.ce.adapter.rest.service.RestExceptionHandlingServiceImpl;
import com.fintex.ce.calculation.CalculationService;
import com.fintex.ce.domain.dto.command.CalculationCommand;
import com.fintex.ce.domain.model.enumeration.CalculationMetric;
import com.fintex.ce.domain.model.result.ErrorResult;

import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Stream;

import static com.fintex.ce.adapter.rest.controller.PortfolioCalculationController.BASE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PortfolioCalculationControllerTest {

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;
  private EnumMap<CalculationMetric, CalculationService> mockServices;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    mockServices = new EnumMap<>(CalculationMetric.class);
    List<CalculationService<?, ?>> serviceList = Arrays.stream(CalculationMetric.values())
        .map(this::createMockService)
        .toList();

    var controller = new PortfolioCalculationController(
        serviceList, new RestExceptionHandlingServiceImpl());

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
        .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
        .build();
  }

  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("calculationMetricArguments")
  @SuppressWarnings("unchecked")
  void shouldReturnMappedResponse_whenValidMetricRequested(
      CalculationMetric metric,
      CalculationCommand command,
      ErrorResult serviceResult,
      Class<? extends ErrorDTO> responseType) throws Exception {
    lenient().when(mockServices.get(metric).perform(any())).thenReturn(serviceResult);

    command.setMetric(metric);
    String requestBody = objectMapper.writeValueAsString(command);

    MvcResult mvcResult = mockMvc.perform(
        post(BASE_PATH + "/" + metric.getValue())
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody))
        .andExpect(result -> {
          if (result.getResolvedException() != null) {
            throw new AssertionError("Controller threw: " + result.getResolvedException().getMessage(),
                result.getResolvedException());
          }
        })
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn();

    verify(mockServices.get(metric)).perform(any());

    ErrorDTO expectedDto = responseType.getDeclaredConstructor().newInstance();
    BeanUtils.copyProperties(serviceResult, expectedDto);

    String responseBody = mvcResult.getResponse().getContentAsString();
    ErrorDTO actualDto = objectMapper.readValue(responseBody, responseType);

    assertThat(actualDto)
        .isNotNull()
        .isInstanceOf(responseType)
        .usingRecursiveComparison()
        .ignoringCollectionOrder()
        .isEqualTo(expectedDto);
  }

  @Test
  void shouldThrowException_whenUnknownMetricRequested() {
    String requestBody = """
        {"metric": "trailing-total-returns"}
        """;

    assertThatThrownBy(() -> mockMvc.perform(
        post(BASE_PATH + "/unknown-metric")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)))
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("unknown-metric");
  }

  @Test
  void shouldThrowException_whenMetricInBodyMismatchesPathParameter() {
    String requestBody = """
        {"metric": "sharpe-ratio", "holdings": []}
        """;

    assertThatThrownBy(() -> mockMvc.perform(
        post(BASE_PATH + "/trailing-total-returns")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestBody)))
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Metric mismatch");
  }

  @Test
  void shouldReturn415_whenContentTypeIsNotJson() throws Exception {
    mockMvc.perform(
        post(BASE_PATH + "/trailing-total-returns")
            .contentType(MediaType.TEXT_PLAIN)
            .content("not json"))
        .andExpect(status().isUnsupportedMediaType());
  }

  @SuppressWarnings("unchecked")
  private CalculationService<?, ?> createMockService(CalculationMetric metric) {
    CalculationService mock = mock(CalculationService.class);
    lenient().when(mock.getMetric()).thenReturn(metric);
    lenient().when(mock.perform(any())).thenReturn(new ErrorResult() {});
    mockServices.put(metric, mock);
    return mock;
  }

  static Stream<Arguments> calculationMetricArguments() {
    return CalculationTestDataProvider.calculationMetricArguments();
  }
}
