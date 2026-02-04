package com.fintex.ce.adapter.cache.repository.managementfee;

import com.fintex.ce.adapter.cache.entity.managementfee.RManagementFee;
import com.fintex.ce.adapter.cache.repository.core.CoreRedisCacheRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManagementFeeRepository extends CoreRedisCacheRepository<RManagementFee> {

}
