package com.github.dewarepk.keytron.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.KeyPair;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final KeyPair keyPair;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        return !req.getRequestURI().equals("/v1/licenses/validate");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "MISSING_BEARER");
            return;
        }

        String token = auth.substring(7);
        try {
            var claims = Jwts.parser().setSigningKey(keyPair.getPublic()).build().parseClaimsJws(token).getBody();

            var authn = new UsernamePasswordAuthenticationToken(
                    claims.get("act"), null, List.of(new SimpleGrantedAuthority("LICENSE_TOKEN")));
            authn.setDetails(Map.of("lic", claims.get("lic"), "act", claims.get("act")));
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(authn);
            chain.doFilter(req, res);
        } catch (JwtException e) {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "INVALID_TOKEN");
        }
    }
}
