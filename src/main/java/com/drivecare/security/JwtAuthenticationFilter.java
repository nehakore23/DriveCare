
package com.drivecare.security;

import com.drivecare.entity.Mechanic;
import com.drivecare.repository.MechanicRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final MechanicRepository mechanicRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService,
            MechanicRepository mechanicRepository) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.mechanicRepository = mechanicRepository;
    }

    // Login and registration don't need JWT
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        String path = request.getServletPath();

        return path.startsWith("/api/auth/")
                || path.equals("/api/mechanic/login")
                || !path.startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        String token = null;
        String email = null;
        String role = null;

        // Check Authorization header
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            token = authHeader.substring(7);

            try {
                // Extract email and role from JWT
                email = jwtService.extractEmail(token);
                role = jwtService.extractRole(token);

            } catch (Exception e) {

                System.out.println("Invalid JWT token");
            }
        }

        // Continue only if JWT contains email
        if (email != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = null;

            /*
             * MECHANIC LOGIN TOKEN
             */
            if ("MECHANIC".equals(role)) {

                Mechanic mechanic =
                        mechanicRepository.findByEmail(email)
                                .orElse(null);

                if (mechanic != null && mechanic.isStatus()) {

                    userDetails =
                            new org.springframework.security.core.userdetails.User(
                                    mechanic.getEmail(),
                                    mechanic.getPassword(),
                                    true,
                                    true,
                                    true,
                                    true,
                                    Collections.singletonList(
                                            new SimpleGrantedAuthority(
                                                    "ROLE_MECHANIC"
                                            )
                                    )
                            );
                }
            }

            /*
             * NORMAL USER / CUSTOMER / ADMIN TOKEN
             */
            else {

                try {

                    userDetails =
                            userDetailsService.loadUserByUsername(email);

                } catch (Exception e) {

                    System.out.println(
                            "User not found: " + email
                    );
                }
            }

            /*
             * Validate JWT and create authentication
             */
            if (userDetails != null
                    && jwtService.isTokenValid(token)) {

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}

