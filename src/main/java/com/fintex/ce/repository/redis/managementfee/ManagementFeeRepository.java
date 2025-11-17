package com.fintex.ce.repository.redis.managementfee;

import com.fintex.ce.model.redis.managementfee.RManagementFee;
import com.fintex.ce.repository.redis.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagementFeeRepository extends CoreRedisCacheRepository<RManagementFee> {

}
