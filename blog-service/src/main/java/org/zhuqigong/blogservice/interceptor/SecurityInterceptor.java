package org.zhuqigong.blogservice.interceptor;

import org.apache.commons.lang.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.zhuqigong.blogservice.util.PageParamsUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class SecurityInterceptor implements HandlerInterceptor {
    private static final String ERROR_MESSAGE = "Invalid request parameter";

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) throws Exception {
        Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
        String[] pages = parameterMap.getOrDefault("page", null);
        String[] sizes = parameterMap.getOrDefault("size", null);
        if (pages == null && sizes == null) {
            return true;
        }
        if ((pages != null && pages.length > 1) || (sizes != null && sizes.length > 1)) {
            response.setStatus(400);
            response.getWriter().write(ERROR_MESSAGE);
            return false;
        }
        if (pages != null) {
            String page = pages[0];
            if (NumberUtils.isDigits(page)) {
                int i = Integer.parseInt(page);
                int newPageNumber = PageParamsUtil.securePageNumber(i);
                parameterMap.put("page", new String[]{String.valueOf(newPageNumber)});
            } else {
                response.setStatus(400);
                response.getWriter().write(ERROR_MESSAGE);
                return false;
            }
        }
        if (sizes != null) {
            String size = sizes[0];
            if (NumberUtils.isDigits(size)) {
                int i = Integer.parseInt(size);
                int newSize = PageParamsUtil.securePageSize(i);
                parameterMap.put("size", new String[]{String.valueOf(newSize)});
            } else {
                response.setStatus(400);
                response.getWriter().write(ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }
}
