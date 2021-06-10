package org.zhuqigong.blogservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Component
public class CorsResponseUtil {
    @Value("${my.blog.cross-origin}")
    private String[] origins;

    public void corsHeaderSetUp(HttpServletRequest request, HttpServletResponse response) {
        if (Arrays.stream(origins).anyMatch(x -> x.equals(request.getHeader("origin")))) {
            response.setHeader("Access-Control-Allow-Origin", request.getHeader("origin"));
        }
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Authorization");
        response.setStatus(200);
    }
}
