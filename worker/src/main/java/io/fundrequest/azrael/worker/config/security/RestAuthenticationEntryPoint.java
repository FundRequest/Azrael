package io.fundrequest.azrael.worker.config.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(final HttpServletRequest arg0,
                         final HttpServletResponse arg1,
                         final AuthenticationException arg2) throws IOException {
        arg1.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}