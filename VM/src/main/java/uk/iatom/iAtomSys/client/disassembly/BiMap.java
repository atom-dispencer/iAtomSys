package uk.iatom.iAtomSys.client.disassembly;

import java.util.HashMap;

public class BiMap<K, V> {

  HashMap<K, V> map = new HashMap<K, V>();
  HashMap<V, K> inversedMap = new HashMap<V, K>();

  public void put(K k, V v) {
    map.put(k, v);
    inversedMap.put(v, k);
  }

  public V get(K k) {
    return map.get(k);
  }

  public K getKey(V v) {
    return inversedMap.get(v);
  }

}