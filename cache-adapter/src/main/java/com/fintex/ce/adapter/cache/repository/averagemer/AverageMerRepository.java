package com.fintex.ce.adapter.cache.repository.averagemer;

import com.fintex.ce.adapter.cache.entity.averagemer.RAverageMer;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AverageMerRepository extends CoreRedisCacheRepository<RAverageMer> {

}
