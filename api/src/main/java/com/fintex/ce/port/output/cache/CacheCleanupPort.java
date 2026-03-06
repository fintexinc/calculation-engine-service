package com.fintex.ce.port.output.cache;

public interface CacheCleanupPort {

  void removeFxRatesFromCache();

  void removeByHoldingId(String holdingId);

  void clearCache();

  void evictLocalCaches();

}
