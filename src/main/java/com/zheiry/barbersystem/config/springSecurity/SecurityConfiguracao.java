package com.zheiry.barbersystem.config.springSecurity;

import com.zheiry.barbersystem.config.jwt.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


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

    // Necessário para o LoginController injetar AuthenticationManager e chamar .authenticate(...)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Sem cookie de sessão (stateless com JWT) => sem risco de CSRF.
                .csrf(csrf -> csrf.disable())

                // Nunca cria HttpSession. Cada requisição se autentica via o header Authorization.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Login é público.
                        .requestMatchers("/login").permitAll()

                        // Cadastro de cliente é público (tela de login).
                        .requestMatchers(HttpMethod.POST, "/clientes").permitAll()

                        .requestMatchers(HttpMethod.GET,"/clientes/ativos").hasAnyRole("ADMINISTRADOR", "BARBEIRO") //.hasAnyRole - aceita mais de uma role_
                        .requestMatchers(HttpMethod.GET, "/clientes/inativos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET,"/clientes/busca/nome").hasAnyRole("ADMINISTRADOR", "BARBEIRO")
                        .requestMatchers(HttpMethod.PATCH, "/clientes/meu-perfil/{id}").hasRole("CLIENTE") // Alterar informações
                        .requestMatchers(HttpMethod.PATCH, "/clientes/meu-perfil/senha/{id}").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/clientes/delete/{id}").hasRole("CLIENTE")
                        .requestMatchers(HttpMethod.PATCH, "/clientes/reativar/{id}").hasRole("ADMINISTRADOR")


                        .requestMatchers(HttpMethod.POST, "/barbeiros/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/barbeiros/ativos").hasAnyRole("BARBEIRO", "CLIENTE", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/barbeiros/inativos").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.GET, "/barbeiros/busca/nome").hasAnyRole("CLIENTE", "BARBEIRO", "ADMINISTRADOR")
                        .requestMatchers(HttpMethod.PATCH, "/barbeiros/meu-perfil/{id}").hasRole("BARBEIRO")



                        // Somente ADMINISTRADOR pode cadastrar serviço.
                        .requestMatchers(HttpMethod.POST, "/servicos/**")
                        .hasRole("ADMINISTRADOR")
                        // Lista de serviços ativos: qualquer usuário autenticado.
                        .requestMatchers(HttpMethod.GET, "/servicos/lista/ativos")
                        .authenticated()


                        // Qualquer outra rota precisa estar autenticada.
                        .anyRequest().authenticated()
                )

                // Roda antes do filtro padrão de usuário/senha, populando o SecurityContext
                // a partir do token, quando presente e válido.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .userDetailsService(userDetailsService);

        return http.build();
    }
}


