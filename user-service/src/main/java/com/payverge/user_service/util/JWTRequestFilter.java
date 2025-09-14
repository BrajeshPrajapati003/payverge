package com.payverge.user_service.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.awt.*;
import java.io.IOException;
import java.util.List;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {
    private JWTUtil jwtUtil;

    public JWTRequestFilter(JWTUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        final String authorizationHeader = request.getHeader("Authorization");
        String username = null;
        String jwt = null;

        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            jwt = authorizationHeader.substring(7);

            if(jwt == null || jwt.isBlank()){
                chain.doFilter(request, response);
                return; // Skip processing if token empty
            }
            try{
                username = jwtUtil.extractUsername(jwt);
                // Only extract role if JWT is valid and present
                String role = jwtUtil.extractRole(jwt);
                // Use role for authorities as needed
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(role))
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }catch(Exception e) {
                // log error if you want
                System.out.println("JWT Request Filter - do filter internal failure");
            }
        }else{
            chain.doFilter(request, response);
            return;
        }
    }
}
