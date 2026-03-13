package com.example.seat_service.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${auth0.audience}")
    private String audience;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    private final Auth0JwtConverter auth0JwtConverter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public
                        .requestMatchers(HttpMethod.GET, "/api/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/venues/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cities/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/seats/**").permitAll()

                        // admin — cities
                        .requestMatchers(HttpMethod.POST, "/api/cities/**").hasAuthority("admin:cities")
                        .requestMatchers(HttpMethod.PUT, "/api/cities/**").hasAuthority("admin:cities")
                        .requestMatchers(HttpMethod.DELETE, "/api/cities/**").hasAuthority("admin:cities")

                        // admin — venues
                        .requestMatchers(HttpMethod.POST, "/api/venues/**").hasAuthority("admin:venues")
                        .requestMatchers(HttpMethod.PUT, "/api/venues/**").hasAuthority("admin:venues")
                        .requestMatchers(HttpMethod.DELETE, "/api/venues/**").hasAuthority("admin:venues")

                        // admin — seats
                        .requestMatchers(HttpMethod.POST, "/api/seats/**").hasAuthority("admin:seats")
                        .requestMatchers(HttpMethod.PUT, "/api/seats/**").hasAuthority("admin:seats")
                        .requestMatchers(HttpMethod.DELETE, "/api/seats/**").hasAuthority("admin:seats")

                        // admin — events
                        .requestMatchers("/api/events/admin/**").hasAuthority("admin:all")
                        .requestMatchers(HttpMethod.POST, "/api/events/**").hasAuthority("admin:events")
                        .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAuthority("admin:events")
                        .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAuthority("admin:events")

                        // user — bookings
                        .requestMatchers(HttpMethod.POST, "/api/bookings/lock").hasAuthority("write:locks")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/lock").hasAuthority("delete:locks")
                        .requestMatchers(HttpMethod.POST, "/api/bookings").hasAuthority("write:bookings")
                        .requestMatchers(HttpMethod.GET, "/api/bookings/**").hasAuthority("read:bookings")
                        .requestMatchers(HttpMethod.DELETE, "/api/bookings/**").hasAuthority("delete:bookings")

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(auth0JwtConverter)
                        )
                );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromIssuerLocation(issuerUri);

        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);

        jwtDecoder.setJwtValidator(withAudience);
        return jwtDecoder;
    }
}