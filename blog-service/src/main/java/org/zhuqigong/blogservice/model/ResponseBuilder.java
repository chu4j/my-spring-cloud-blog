package org.zhuqigong.blogservice.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseBuilder implements MapResponse<String, Object> {
    private final Map<String, Object> map = new LinkedHashMap<>();

    @Override
    public MapResponse<String, Object> append(String key, Object value) {
        map.put(key, value);
        return this;
    }

    @Override
    public MapResponse<String, Object> message(Object message) {
        map.put("message", message);
        return this;
    }

    @Override
    public MapResponse<String, Object> status(Object status) {
        map.put("status", status);
        return this;
    }

    @Override
    public Map<String, Object> build() {
        return this.map;
    }

}
