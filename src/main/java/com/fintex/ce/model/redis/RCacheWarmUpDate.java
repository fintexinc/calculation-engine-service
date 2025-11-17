package com.fintex.ce.model.redis;


import com.fintex.ce.model.redis.core.RedisId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;

import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@RedisHash("CacheWarmUpDate")
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class RCacheWarmUpDate extends RedisId {

    private ZonedDateTime zonedDateTime;

}
