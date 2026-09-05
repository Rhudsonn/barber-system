package com.zheiry.barbersystem.config;

import com.zheiry.barbersystem.services.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableMethodSecurity // permite @PreAuthorize nos controllers/services
public class SecurityConfiguracao {

    private final CustomUserDetailsService userDetailsService;

    public  SecurityConfiguracao(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Todo:reativar o CSRF quando o login por formulário/sessão estiver em uso real.
                // Enquanto estou testando a API pelo Postman, desliguei para não tomar 403.
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Páginas públicas
                        .requestMatchers("/", "/login", "/cadastro", "/css/**", "/js/**", "/images/**").permitAll()

                        // TODO: remover depois que o cadastro de barbeiro estiver protegido por um fluxo de admin.
                        // Liberado temporariamente só para testar via Postman.
                        .requestMatchers("/barbeiros/**").permitAll()

                        // Áreas por role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/barbeiro/**").hasAnyRole("ADMIN", "BARBEIRO")
                        .requestMatchers("/cliente/**").hasAnyRole("ADMIN", "CLIENTE")

                        // Qualquer outra rota autenticada
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        //.loginPage("/login")
                        .defaultSuccessUrl("/home", true)   // ou lógica de redirecionamento por role
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
}
