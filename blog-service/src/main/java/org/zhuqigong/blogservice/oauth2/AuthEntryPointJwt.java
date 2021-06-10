package org.zhuqigong.blogservice.oauth2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.zhuqigong.blogservice.util.CorsResponseUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {
    private static final Logger logger = LoggerFactory.getLogger(AuthEntryPointJwt.class);
    private final CorsResponseUtil corsResponseUtil;

    public AuthEntryPointJwt(CorsResponseUtil corsResponseUtil) {
        this.corsResponseUtil = corsResponseUtil;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {
        logger.error("Unauthorized error: {}", authException.getMessage());
        corsResponseUtil.corsHeaderSetUp(request, response);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized,Please SignIn...");
    }
}