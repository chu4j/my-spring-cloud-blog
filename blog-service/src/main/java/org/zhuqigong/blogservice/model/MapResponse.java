package org.zhuqigong.blogservice.model;

import java.util.Map;

public interface MapResponse<K, V> {
    MapResponse<K, V> append(K key, V value);

    MapResponse<K, V> message(V message);

    MapResponse<K, V> status(V status);

    Map<K, V> build();
}
