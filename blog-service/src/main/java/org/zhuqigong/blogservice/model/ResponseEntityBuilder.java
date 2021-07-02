package org.zhuqigong.blogservice.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResponseEntityBuilder implements Response<String, Object> {
    private final Map<String, Object> map = new LinkedHashMap<>();

    @Override
    public Response<String, Object> append(String key, Object value) {
        map.put(key, value);
        return this;
    }

    @Override
    public Response<String, Object> message(Object message) {
        map.put("message", message);
        return this;
    }

    @Override
    public Response<String, Object> statusCode(Object status) {
        map.put("status", status);
        return this;
    }

    @Override
    public Map<String, Object> build() {
        return this.map;
    }

}
