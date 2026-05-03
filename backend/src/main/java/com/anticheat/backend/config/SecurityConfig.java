package com.anticheat.backend.config;

import com.anticheat.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private com.anticheat.backend.security.ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/api/ai/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/punishment/all", "/api/punishment/active", "/api/punishment/uuid/**", "/api/punishment/check/**").authenticated()
                .requestMatchers("/api/punishment/ban").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/punishment/unban/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/punishment/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/player/all", "/api/player/high-risk").authenticated()
                .requestMatchers("/api/player/kick/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/player/*/risk").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/player/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/player/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/cheat/all", "/api/cheat/page", "/api/cheat/player/**", "/api/cheat/type/**").authenticated()
                .requestMatchers("/api/cheat/add").hasAnyRole("ADMIN", "SUPER_ADMIN", "PLUGIN")
                .requestMatchers(HttpMethod.DELETE, "/api/cheat/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/report/all", "/api/report/pending", "/api/report/count/**").authenticated()
                .requestMatchers("/api/report/handle/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/report/create").hasAnyRole("ADMIN", "SUPER_ADMIN", "PLUGIN")
                .requestMatchers(HttpMethod.DELETE, "/api/report/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/whitelist/all", "/api/whitelist/active", "/api/whitelist/check/**").authenticated()
                .requestMatchers("/api/whitelist/add").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/whitelist/remove/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/whitelist/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/settings/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/stats/**").authenticated()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000", "http://localhost:3001",
            "http://127.0.0.1:3000", "http://127.0.0.1:3001"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-Api-Key"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
