package com.example.bank_app.util;

import jakarta.servlet.http.HttpServletRequest;

public class WebUtils {

    private WebUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Extrae la IP real del cliente, considerando proxies (X-Forwarded-For).
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Extrae el User-Agent (Navegador/Dispositivo).
     */
    public static String getUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return (userAgent != null && !userAgent.isEmpty()) ? userAgent : "Unknown Device";
    }
}
