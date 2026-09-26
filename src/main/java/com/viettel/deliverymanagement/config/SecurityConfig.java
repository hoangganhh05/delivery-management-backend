package com.viettel.deliverymanagement.config;

import com.viettel.deliverymanagement.security.JwtAuthenticationEntryPoint;
import com.viettel.deliverymanagement.security.JwtAuthenticationFilter;
import com.viettel.deliverymanagement.security.TrackingRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

import java.util.List;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final TrackingRateLimitFilter trackingRateLimitFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000,http://localhost:8443}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Link", "X-Total-Count"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        // Cho phép toàn bộ OPTIONS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Mở permitAll() cho các endpoints công khai
                        .requestMatchers("/auth/**", "/api/v1/auth/**").permitAll()
                        .requestMatchers("/tracking/**", "/api/v1/tracking/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/payment/vnpay-callback", "/api/v1/payment/vnpay-callback").permitAll()
                        .requestMatchers(HttpMethod.POST, "/vouchers/calculate", "/api/v1/vouchers/calculate").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Phân quyền theo vai trò (RBAC)
                        // Các endpoint vận hành này dùng @PreAuthorize với ma trận quyền lưu trong DB.
                        .requestMatchers("/dashboard/**", "/api/v1/dashboard/**").authenticated()
                        // API tự phục vụ: luôn xác định tài khoản từ JWT, không nhận userId từ client.
                        // Các matcher này phải đứng trước rule /users/** dành riêng cho ADMIN.
                        .requestMatchers(
                                "/users/me",
                                "/users/profile",
                                "/users/change-password",
                                "/users/settings",
                                "/users/addresses",
                                "/users/addresses/**",
                                "/users/bank-accounts",
                                "/users/bank-accounts/**",
                                "/api/v1/users/me",
                                "/api/v1/users/profile",
                                "/api/v1/users/change-password",
                                "/api/v1/users/settings",
                                "/api/v1/users/addresses",
                                "/api/v1/users/addresses/**",
                                "/api/v1/users/bank-accounts",
                                "/api/v1/users/bank-accounts/**"
                        ).authenticated()
                        .requestMatchers("/users/**", "/api/v1/users/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/shippers/**", "/api/v1/shippers/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/vouchers", "/api/v1/vouchers").authenticated()
                        .requestMatchers(HttpMethod.GET, "/vouchers", "/api/v1/vouchers").authenticated()
                        .requestMatchers("/shipments/assign", "/api/v1/shipments/assign").authenticated()
                        .requestMatchers("/shipments/orders/**", "/api/v1/shipments/orders/**").authenticated()

                        // Tất cả các request còn lại yêu cầu xác thực JWT
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(trackingRateLimitFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}
