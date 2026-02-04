package com.fintex.ce.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TPair<K, V> {

  private K key;
  private V value;

}
