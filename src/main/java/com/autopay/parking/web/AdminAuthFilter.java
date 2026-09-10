package com.autopay.parking.web;

import com.autopay.parking.model.AppUser;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Protege los endpoints de administración web. Fuera de la ruta pública de
 * login, exige el token de sesión ({@code X-Admin-Token}) y deja el usuario
 * autenticado en el atributo de petición {@code adminUser}.
 */
@Component
public class AdminAuthFilter implements Filter {

    public static final String TOKEN_HEADER = "X-Admin-Token";
    public static final String USER_ATTRIBUTE = "adminUser";

    private static final String PUBLIC_LOGIN = "/api/admin/login";

    private final SessionManager sessions;

    public AdminAuthFilter(SessionManager sessions) {
        this.sessions = sessions;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String path = httpReq.getRequestURI();
        if (!path.startsWith("/api/admin") || PUBLIC_LOGIN.equals(path)) {
            chain.doFilter(request, response);
            return;
        }

        String token = httpReq.getHeader(TOKEN_HEADER);
        var user = sessions.user(token);
        if (user.isEmpty()) {
            reject(httpResp, "Sesión de administración inválida o expirada.");
            return;
        }

        request.setAttribute(USER_ATTRIBUTE, user.get());
        chain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}