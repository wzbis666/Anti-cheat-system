package com.anticheat.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collections;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);

    @Value("${api.key:}")
    private String validApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = request.getHeader("X-Api-Key");

        if (apiKey != null && !apiKey.isEmpty()) {
            if (isApiKeyValid(apiKey)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        "plugin", null,
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PLUGIN"))
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("API Key认证成功");
            } else {
                logger.warn("无效的API Key尝试, remote={}", request.getRemoteAddr());
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isApiKeyValid(String apiKey) {
        if (validApiKey == null || validApiKey.isEmpty()) {
            return false;
        }
        try {
            byte[] a = apiKey.getBytes("UTF-8");
            byte[] b = validApiKey.getBytes("UTF-8");
            return MessageDigest.isEqual(a, b);
        } catch (Exception e) {
            return false;
        }
    }
}
