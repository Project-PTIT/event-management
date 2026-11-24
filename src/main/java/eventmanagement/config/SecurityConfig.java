package eventmanagement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Xác thực bằng session của Spring Security (không dùng JWT).
 *
 * CSRF: theo quy ước nhóm, tắt CSRF cho /api/**. Đây KHÔNG phải bảo vệ đầy đủ chống CSRF;
 * giảm thiểu bằng cookie session HttpOnly + SameSite=Lax (xem application.properties).
 * Ngoài /api không có endpoint nào thay đổi dữ liệu, CSRF mặc định vẫn bật ở đó.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Dùng AuthenticationManager mà Spring Security tự dựng từ CustomUserDetailsService + PasswordEncoder. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   SecurityContextRepository securityContextRepository,
                                                   RestSecurityHandlers restSecurityHandlers) throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .requestCache(cache -> cache.disable())
                .logout(logout -> logout.disable())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restSecurityHandlers)
                        .accessDeniedHandler(restSecurityHandlers))
                .authorizeHttpRequests(auth -> auth
                        // Công khai: đăng nhập, đăng ký
                        .requestMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/register/customer",
                                "/api/auth/register/organizer").permitAll()
                        // Theo role
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/organizer/**").hasRole("ORGANIZER")
                        .requestMatchers("/api/orders/**").hasRole("CUSTOMER")
                        // Thống kê/danh sách vé cũ: chỉ Admin (quản lý vé của Organizer nằm ở /api/organizer/**)
                        .requestMatchers("/api/tickets/**").hasRole("ADMIN")
                        // Dữ liệu xem công khai (chỉ GET). Gồm cả /api/events/{id}/schedules và /api/schedules/{id}/tickets
                        .requestMatchers(HttpMethod.GET,
                                "/api/events/**",
                                "/api/schedules/**",
                                "/api/venues/**",
                                "/api/wards/**",
                                "/api/provinces/**",
                                "/api/artists/**").permitAll()
                        // Còn lại trong /api (logout, /api/users/me, ...): phải đăng nhập
                        .requestMatchers("/api/**").authenticated()
                        // Trang tĩnh (HTML/CSS/JS/ảnh): công khai, quyền dữ liệu do API quyết định
                        .anyRequest().permitAll());

        return http.build();
    }
}
