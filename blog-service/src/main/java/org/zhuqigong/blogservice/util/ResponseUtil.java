package org.zhuqigong.blogservice.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;

@Component
public class ResponseUtil {
    @Value("${my.blog.cross-origin}")
    private String frontendOrigin;

    public void corsHeaderSetUp(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", frontendOrigin);
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "Authorization");
        response.setStatus(200);
    }
}
