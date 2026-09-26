package com.pdev.rempms.documentservice.config;


import com.pdev.rempms.documentservice.constants.CommonConstants;
import io.github.resilience4j.core.lang.Nullable;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RequestFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /**
         * ignore service registry health check url
         */
        if (!request.getRequestURI().equalsIgnoreCase("/health/healthCheck")) {
            logRequest(request);
        }
        filterChain.doFilter(request, response);
    }

    private void logRequest(HttpServletRequest request) {
        Map<String, List<String>> headersMap = Collections.list(request.getHeaderNames()).stream().collect(Collectors.toMap(
                Function.identity(), h -> Collections.list(request.getHeaders(h))));
        log.info("[REQUEST] IpAddress: {} | URI: {} | Method: {} | Headers: {} ",
                getClientIp(request)
                , request.getRequestURI()
                , request.getMethod()
                , headersMap);
    }

    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (isEmpty(ipAddress) || CommonConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }

        if (isEmpty(ipAddress) || CommonConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }

        if (isEmpty(ipAddress) || CommonConstants.UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            try {
                InetAddress inetAddress = InetAddress.getLocalHost();
                ipAddress = inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }

        if (!isEmpty(ipAddress) && ipAddress.length() > 15 && ipAddress.indexOf(",") >= 0) {
            ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
        }

        return ipAddress;
    }

    private static boolean isEmpty(@Nullable Object str) {
        return str == null || "".equals(str);
    }

}

