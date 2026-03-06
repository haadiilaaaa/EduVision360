package lk.icbt.eduvision.eduvision360.security.config;

import lk.icbt.eduvision.eduvision360.security.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOrigin("http://localhost:3000");
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");
                    config.setAllowCredentials(true);
                    return config;
                }))
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers("/api/auth/**").permitAll()

                        // student attendance endpoints
                        .requestMatchers("/api/attendance/register-face").hasAuthority("STUDENT")
                        .requestMatchers("/api/attendance/mark").hasAuthority("STUDENT")
                        .requestMatchers("/api/attendance/my").hasAuthority("STUDENT")

                        // student
                        .requestMatchers("/api/student/**").hasAuthority("STUDENT")

                        // teacher
                        .requestMatchers("/api/courses/teacher/my").hasAuthority("TEACHER")
                        .requestMatchers("/api/teacher/attendance/**").hasAuthority("TEACHER")
                        .requestMatchers("/api/teacher/materials/**").hasAuthority("TEACHER")
                        .requestMatchers("/api/teacher/announcements/**").hasAuthority("TEACHER")

                        // ✅ predictions (granular)
                        .requestMatchers(HttpMethod.GET, "/api/predictions/student/**").hasAuthority("STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/predictions/teacher/**").hasAuthority("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/predictions/admin/**").hasAuthority("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/predictions/dropout/run").hasAnyAuthority("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/predictions/dropout").hasAnyAuthority("TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/predictions/courses/*/students").hasAnyAuthority("TEACHER", "ADMIN")

                        // admin
                        .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/departments/**").hasAuthority("ADMIN")

                        // courses
                        .requestMatchers(HttpMethod.GET, "/api/courses/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/courses/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/courses/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasAuthority("ADMIN")

                        // other
                        .requestMatchers("/api/profile/**").authenticated()
                        .requestMatchers("/api/class-sessions/student/**").hasAuthority("STUDENT")
                        .requestMatchers("/api/class-sessions/teacher/**").hasAuthority("TEACHER")
                        .requestMatchers(HttpMethod.POST, "/api/class-sessions").hasAnyAuthority("ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/api/class-sessions/**").hasAnyAuthority("ADMIN", "TEACHER")
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/student/feedback/**").hasAuthority("STUDENT")
                        .requestMatchers("/api/teacher/feedback/**").hasAuthority("TEACHER")
                        .requestMatchers("/api/admin/feedback/**").hasAuthority("ADMIN")
                        .requestMatchers("/api/student/ai/**").hasAuthority("STUDENT")

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}