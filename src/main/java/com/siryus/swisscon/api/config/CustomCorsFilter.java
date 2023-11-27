package com.siryus.swisscon.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Component
public class CustomCorsFilter implements Filter {

    @Value("${cors.allowed-origins}")
    private String allowedOriginsEnv;

    public static final String ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    public static final String ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
    public static final String ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
    public static final String ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
    public static final String ALLOWED_HTTP_METHODS = "GET, POST, PUT, DELETE, HEAD, OPTIONS";
    public static final String ALLOWED_HTTP_HEADERS = "Origin, X-Requested-With, Content-Type, Accept";

    private static List<String> ALLOWED_ORIGINS;

    @Override
    public void init(FilterConfig filterConfig) {
        ALLOWED_ORIGINS = Arrays.asList(this.allowedOriginsEnv.split(","));
        if (ALLOWED_ORIGINS.isEmpty()) {
            throw new RuntimeException("There should be at least one allowed origin");
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        String origin = request.getHeader("Origin");
        
        if (origin != null) {
            if (isAllowedOrigin(origin)) {
                response.setHeader(ALLOW_ORIGIN_HEADER, request.getHeader("Origin"));
                response.setHeader(ALLOW_METHODS_HEADER, ALLOWED_HTTP_METHODS);
                response.setHeader(ALLOW_HEADERS_HEADER, ALLOWED_HTTP_HEADERS);
                response.setHeader(ALLOW_CREDENTIALS_HEADER, "true");
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        }

        // For HTTP OPTIONS verb/method reply with ACCEPTED status code -- per CORS handshake
        if (request.getMethod().equals("OPTIONS")) {
            response.setStatus(HttpServletResponse.SC_ACCEPTED);
            return;
        }

        chain.doFilter(req, res);
    }

    private boolean isAllowedOrigin(String origin) {
        return ALLOWED_ORIGINS.contains("*") ||
                ALLOWED_ORIGINS
                        .stream()
                        .anyMatch(ao -> originMatches(ao, origin));

    }

    private boolean originMatches(String allowedOrigin, String origin) {
        String originHost;

        // initialize Url
        try {
            originHost = new URL(origin).getHost();
        } catch (MalformedURLException e) {
            return false;
        }

        // localhost urls are always allowed
        if (originHost.startsWith("localhost")) {
            return true;
        }

        // all subdomains are allowed
        if (allowedOrigin.startsWith("*.")) {
            String allowedDomain = allowedOrigin.replaceFirst("\\*.", "");
            return originHost.endsWith(allowedDomain);
        }
        
        // specific domain has to match        
        return originHost.equals(allowedOrigin);
    }

    @Override
    public void destroy() {
        // no implementation
    }
}
