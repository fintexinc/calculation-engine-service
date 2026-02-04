package com.fintex.ce.adapter.jdbc.entity;

import com.fintex.ce.adapter.jdbc.entity.SMUsageStatistics;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SMUsageStatisticsTest {

  @Test
  void getTotalNumberOfUsages_checkResult() {
    // SETUP
    final SMUsageStatistics sm = new SMUsageStatistics()
        .setDay1Count(100)
        .setDay2Count(2)
        .setDay3Count(3)
        .setDay4Count(4)
        .setDay5Count(500)
        .setDay6Count(6)
        .setDay0Count(7);

    final int actual = sm.getTotalNumberOfUsages();
    // ACT

    // VERIFY
    assertEquals(622, actual);
  }

}
