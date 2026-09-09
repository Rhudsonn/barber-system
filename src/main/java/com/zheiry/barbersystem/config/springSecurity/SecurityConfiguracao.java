package com.zheiry.barbersystem.config.springSecurity;

import com.zheiry.barbersystem.config.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableMethodSecurity // permite @PreAuthorize nos controllers/services
public class SecurityConfiguracao {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfiguracao(CustomUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Necessario para o LoginController injetar AuthenticationManager e chamar .authenticate(...)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Sem cookie de sessao (stateless com JWT) => sem risco de CSRF.
                .csrf(csrf -> csrf.disable())

                // Nunca cria HttpSession. Cada requisicao se autentica via o header Authorization.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Login e publico, obvio.
                        .requestMatchers("/login").permitAll()

                        // Cadastro de cliente é público (vai ficar na tela de login).
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/clientes").permitAll()

                        // Somente ADMINISTRADOR pode cadastrar barbeiro.
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/barbeiros/**")
                        .hasRole("ADMINISTRADOR")

                        // Qualquer outra rota precisa estar autenticada.
                        .anyRequest().authenticated()
                )

                // Roda antes do filtro padrao de usuario/senha, populando o SecurityContext
                // a partir do token, quando presente e valido.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .userDetailsService(userDetailsService);

        return http.build();
    }
    }
