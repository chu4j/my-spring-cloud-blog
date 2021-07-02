package org.zhuqigong.blogservice.model;

import java.util.Map;

public interface Response<K, V> {
    Response<K, V> append(K key, V value);

    Response<K, V> message(V message);

    Response<K, V> statusCode(V status);

    Map<K, V> build();
}
