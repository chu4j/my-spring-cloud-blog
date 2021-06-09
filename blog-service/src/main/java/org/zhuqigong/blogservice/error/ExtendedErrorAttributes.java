package org.zhuqigong.blogservice.error;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Configuration
public class ExtendedErrorAttributes extends DefaultErrorAttributes {
    @Override
    public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
        options.including(ErrorAttributeOptions.Include.MESSAGE);
        options.including(ErrorAttributeOptions.Include.EXCEPTION);
        Map<String, Object> attributes = super.getErrorAttributes(webRequest, options);
        String message = super.getMessage(webRequest, super.getError(webRequest));
        attributes.put("message", message);
        return attributes;
    }
}
