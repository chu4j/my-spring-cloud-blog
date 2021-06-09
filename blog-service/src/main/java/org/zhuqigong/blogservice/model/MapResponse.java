package org.zhuqigong.blogservice.model;

import java.util.Map;

public interface MapResponse<K, V> {
    MapResponse<K, V> append(K key, V value);

    Map<K, V> build();
}
