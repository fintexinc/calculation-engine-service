package com.fintex.ce.application.returns;

import com.fintex.ce.model.domain.calculation.returns.HoldingMonthlyReturns;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonthlyReturnsContextTest {

  @Test
  void shouldReplaceSnapshot_whenWithSnapshot() {
    ReturnsSnapshot<HoldingMonthlyReturns> firstSnapshot = ReturnsSnapshot.empty();
    ReturnsSnapshot<HoldingMonthlyReturns> secondSnapshot = ReturnsSnapshot.empty();
    FxContext fxContext = FxContext.empty();
    MonthlyReturnsContext<HoldingMonthlyReturns> context = new MonthlyReturnsContext<>(firstSnapshot, fxContext,
        ReturnsRole.PORTFOLIO);

    MonthlyReturnsContext<HoldingMonthlyReturns> next = context.withSnapshot(secondSnapshot);

    assertThat(next.snapshot()).isSameAs(secondSnapshot);
    assertThat(next.fxContext()).isSameAs(fxContext);
    assertThat(next.role()).isEqualTo(ReturnsRole.PORTFOLIO);
  }
}
