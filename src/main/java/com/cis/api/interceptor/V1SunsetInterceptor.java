package com.cis.api.interceptor;

import com.cis.api.config.SystemStateConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.LinkedHashMap;
import java.util.Map;


@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class V1SunsetInterceptor implements HandlerInterceptor {

    static final String WARNING_HEADER_NAME  = "Warning";
    static final String WARNING_HEADER_VALUE = "299 - \"API v1 is deprecated. Please migrate to /api/v2/\"";

    private final SystemStateConfig systemStateConfig;
    private final ObjectMapper      objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest  request,
                             HttpServletResponse response,
                             Object              handler) throws Exception {

        if (!systemStateConfig.isV1Sunset()) {
            return true;
        }

        String method = request.getMethod();

        if (HttpMethod.GET.matches(method)) {
            log.debug("V1 sunset active — appending deprecation Warning header for GET {}", request.getRequestURI());
            response.addHeader(WARNING_HEADER_NAME, WARNING_HEADER_VALUE);
            return true;
        }

        log.warn("V1 sunset active — blocking {} {} with 410 Gone", method, request.getRequestURI());
        response.setStatus(HttpStatus.GONE.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  410);
        body.put("error",   "Gone");
        body.put("message", "API v1 has been sunset. Please migrate to /api/v2/");

        objectMapper.writeValue(response.getWriter(), body);
        return false;
    }
}