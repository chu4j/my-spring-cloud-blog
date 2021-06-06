package org.zhuqigong.blogservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.zhuqigong.blogservice.interceptor.SecurityInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
  private final SecurityInterceptor securityInterceptor;
  @Value("${my.blog.cross-origin}")
  private String crossOrigin;

  public WebMvcConfig(
      SecurityInterceptor securityInterceptor) {
    this.securityInterceptor = securityInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
//    registry.addInterceptor(securityInterceptor).excludePathPatterns("/admin/signIn")
//        .excludePathPatterns("/admin/signOut").excludePathPatterns("/oauth/**").excludePathPatterns("/login");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**")
        .allowCredentials(true)
        .allowedOrigins(crossOrigin)
        .allowedHeaders("*")
        .allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
  }
}
