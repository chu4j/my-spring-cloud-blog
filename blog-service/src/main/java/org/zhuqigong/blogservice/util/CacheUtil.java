package org.zhuqigong.blogservice.util;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class CacheUtil {
    private CacheUtil() {
    }

    public static String generateFixedCacheKey(HttpServletRequest request, Object... params) {
        return request.getServletPath() + "_" + Arrays.stream(params).reduce((x1, x2) -> x1 + "_" + x2).orElse("");
    }
}
