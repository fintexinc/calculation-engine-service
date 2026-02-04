package com.fintex.ce.service;

import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

public interface SMUsageStatisticsService {
  @Async
  CompletableFuture<Integer> registerSMCall(String keyword);
}
