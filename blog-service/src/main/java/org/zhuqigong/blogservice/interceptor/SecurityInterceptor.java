package org.zhuqigong.blogservice.interceptor;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.zhuqigong.blogservice.model.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class SecurityInterceptor implements HandlerInterceptor {
    @Value("${my.blog.cross-origin}")
    private String crossOrigin;

    @Override
    public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response,
                             @NotNull Object handler)
            throws Exception {
        if (!request.getRequestURI().contains("/admin")) {
            return true;
        } else {
            User user = (User) request.getSession().getAttribute("user");
            if (user == null) {
                response.setHeader("Access-Control-Allow-Origin", crossOrigin);
                response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE, PUT");
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setStatus(HttpStatus.OK.value());
                response.getWriter().write("Unauthorized");
                return false;
            } else {
                return true;
            }
        }
    }
}
