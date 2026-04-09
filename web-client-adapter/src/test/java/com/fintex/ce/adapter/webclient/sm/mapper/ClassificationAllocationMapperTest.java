package com.fintex.ce.adapter.webclient.sm.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.fintex.ce.domain.model.ClassificationAllocation;
import com.fintex.ce.domain.model.calculation.ClassificationAllocationType;
import com.fintex.ce.domain.model.holding.Holding;
import com.fintex.sm.model.DataProvider;
import com.fintex.sm.model.domain.SecurityIdentifier;
import com.fintex.sm.model.domain.allocation.SecurityClassificationAllocation;
import com.fintex.sm.model.domain.classification.SecurityClassificationLevelOne;
import com.fintex.sm.model.domain.classification.SecurityClassificationLevelTwo;
import com.fintex.sm.model.domain.classification.SecurityClassificationTypeValue;
import com.fintex.sm.model.domain.enumeration.FinancialInstrumentType;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassificationAllocationMapperTest {

  private final ClassificationAllocationMapper mapper = new ClassificationAllocationMapper();

  @Test
  void shouldMapClassificationValuesAndProvider() {
    var equityCanada = SecurityClassificationTypeValue.builder()
        .levelOne(SecurityClassificationLevelOne.EQUITY)
        .levelTwo(SecurityClassificationLevelTwo.CANADA)
        .value(BigDecimal.valueOf(0.45))
        .build();
    var fixedIncomeUs = SecurityClassificationTypeValue.builder()
        .levelOne(SecurityClassificationLevelOne.FIXED_INCOME)
        .levelTwo(SecurityClassificationLevelTwo.US)
        .value(BigDecimal.valueOf(0.30))
        .build();

    var smsResponse = new SecurityClassificationAllocation();
    smsResponse.setValues(List.of(equityCanada, fixedIncomeUs));
    smsResponse.setDataProvider(DataProvider.MORNINGSTAR);

    ClassificationAllocation result = mapper.map(smsResponse, createHolding("SEC-001"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-001");
    assertThat(result.getHoldingType()).isEqualTo(FinancialInstrumentType.MUTUAL_FUND_CANADA);
    assertThat(result.getProvider()).isEqualTo(DataProvider.MORNINGSTAR.name());
    assertThat(result.getSecurityClassificationValues()).hasSize(2);
    assertThat(result.getSecurityClassificationValues())
        .containsEntry(ClassificationAllocationType.EQUITY__CANADA, BigDecimal.valueOf(0.45));
    assertThat(result.getSecurityClassificationValues())
        .containsEntry(ClassificationAllocationType.FIXED_INCOME__US, BigDecimal.valueOf(0.30));
  }

  @Test
  void shouldReturnEmptyMap_whenResponseIsNull() {
    ClassificationAllocation result = mapper.map(null, createHolding("SEC-002"));

    assertThat(result.getHoldingId()).isEqualTo("SEC-002");
    assertThat(result.getProvider()).isNull();
    assertThat(result.getSecurityClassificationValues()).isEmpty();
  }

  @Test
  void shouldReturnEmptyMap_whenValuesListIsNull() {
    var smsResponse = new SecurityClassificationAllocation();
    smsResponse.setValues(null);

    ClassificationAllocation result = mapper.map(smsResponse, createHolding("SEC-003"));

    assertThat(result.getSecurityClassificationValues()).isEmpty();
  }

  @Test
  void shouldNotSetProvider_whenDataProviderIsNull() {
    var smsResponse = new SecurityClassificationAllocation();
    smsResponse.setValues(List.of());
    smsResponse.setDataProvider(null);

    ClassificationAllocation result = mapper.map(smsResponse, createHolding("SEC-004"));

    assertThat(result.getProvider()).isNull();
  }

  @Test
  void shouldFilterOutEntriesWithNullLevelOrValue() {
    var valid = SecurityClassificationTypeValue.builder()
        .levelOne(SecurityClassificationLevelOne.EQUITY)
        .levelTwo(SecurityClassificationLevelTwo.CANADA)
        .value(BigDecimal.valueOf(0.50))
        .build();
    var nullLevelOne = SecurityClassificationTypeValue.builder()
        .levelOne(null)
        .levelTwo(SecurityClassificationLevelTwo.US)
        .value(BigDecimal.valueOf(0.20))
        .build();
    var nullValue = SecurityClassificationTypeValue.builder()
        .levelOne(SecurityClassificationLevelOne.FIXED_INCOME)
        .levelTwo(SecurityClassificationLevelTwo.CANADA)
        .value(null)
        .build();

    var smsResponse = new SecurityClassificationAllocation();
    smsResponse.setValues(List.of(valid, nullLevelOne, nullValue));

    ClassificationAllocation result = mapper.map(smsResponse, createHolding("SEC-005"));

    assertThat(result.getSecurityClassificationValues()).hasSize(1);
    assertThat(result.getSecurityClassificationValues())
        .containsKey(ClassificationAllocationType.EQUITY__CANADA);
  }

  @Test
  void shouldSumValues_whenDuplicateClassificationsExist() {
    var entry1 = SecurityClassificationTypeValue.builder()
        .levelOne(SecurityClassificationLevelOne.EQUITY)
        .levelTwo(SecurityClassificationLevelTwo.CANADA)
        .value(BigDecimal.valueOf(0.30))
        .build();
    var entry2 = SecurityClassificationTypeValue.builder()
        .levelOne(SecurityClassificationLevelOne.EQUITY)
        .levelTwo(SecurityClassificationLevelTwo.CANADA)
        .value(BigDecimal.valueOf(0.15))
        .build();

    var smsResponse = new SecurityClassificationAllocation();
    smsResponse.setValues(List.of(entry1, entry2));

    ClassificationAllocation result = mapper.map(smsResponse, createHolding("SEC-006"));

    assertThat(result.getSecurityClassificationValues()).hasSize(1);
    assertThat(result.getSecurityClassificationValues().get(ClassificationAllocationType.EQUITY__CANADA))
        .isEqualByComparingTo("0.45");
  }

  private Holding createHolding(String securityId) {
    return new Holding()
        .setHoldingType(FinancialInstrumentType.MUTUAL_FUND_CANADA)
        .setSecurityIdentifier(new SecurityIdentifier(securityId, null));
  }
}