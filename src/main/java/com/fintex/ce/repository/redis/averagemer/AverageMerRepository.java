package com.fintex.ce.repository.redis.averagemer;

import com.fintex.ce.model.redis.averagemer.RAverageMer;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AverageMerRepository extends CoreRedisCacheRepository<RAverageMer> {

}
