package com.fintex.ce.adapter.cache.entity.topcommonholdings;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fintex.ce.domain.model.CommonHoldingsDTO;
import com.fintex.ce.adapter.cache.entity.core.RedisId;
import com.fintex.ce.util.JacksonUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@RedisHash("TopCommonHoldings")
@NoArgsConstructor
@Accessors(chain = true)
public class RCommonHoldings extends RedisId implements Serializable {

  private List<CommonHoldingsDTO> deserializedHoldings;
  private String holdings;

  public RCommonHoldings(String holdings) {
    this.holdings = holdings;
  }

  /**
   * The data in this Redis Object will be saved as String. And when we want to work with the List of CommonHoldingsDTO
   * then we just deserialize to "List<CommonHoldingsDTO>".
   * <p>
   * The reason of that is when the data is as "List<CommonHoldingsDTO>" then if a list has more than 30K objects then
   * the response time is more then 4 minutes, which is not OK. The data saved as String returns in less than 1 second.
   *
   * deserializedHoldings is used for Caffeine cache. Redis doesn't store deserializedHoldings
   *
   * @return list of CommonHoldingsDTO.
   */
  public List<CommonHoldingsDTO> getHoldings() {
    if (!CollectionUtils.isEmpty(deserializedHoldings)) {
      return deserializedHoldings;
    }

    synchronized (this) {
      if (CollectionUtils.isEmpty(deserializedHoldings)) {
        deserializedHoldings = JacksonUtil.deserialize(holdings, new TypeReference<>() {
        });
        holdings = null;
      }
      return deserializedHoldings;
    }

  }

}
