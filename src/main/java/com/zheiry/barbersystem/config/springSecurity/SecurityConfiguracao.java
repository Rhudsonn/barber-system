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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Arrays;


@Configuration
@EnableMethodSecurity // permite @PreAuthorize nos controllers/services
public class SecurityConfiguracao {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;
    private final Environment environment;
    @Value("${app.cors.allowed-origins:http://localhost:*,http://127.0.0.1:*,http://10.0.2.2:*}")
    private String origensPermitidas;

    public SecurityConfiguracao(CustomUserDetailsService userDetailsService, JwtAuthFilter jwtAuthFilter,
                                Environment environment) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthFilter = jwtAuthFilter;
        this.environment = environment;
    }

    @PostConstruct
    void validarCorsDeProducao() {
        if (!Arrays.asList(environment.getActiveProfiles()).contains("prod")) return;
        List<String> origens = Arrays.stream(origensPermitidas.split(","))
                .map(String::trim).filter(origem -> !origem.isEmpty()).toList();
        if (origens.isEmpty() || origens.stream().anyMatch(origem -> !origem.startsWith("https://") || origem.contains("*"))) {
            throw new IllegalStateException("Em produção, CORS_ALLOWED_ORIGINS deve conter apenas origens HTTPS exatas.");
        }
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
                .cors(cors -> {})

                // Nunca cria HttpSession. Cada requisição se autentica via o header Authorization.
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // Login é público.
                        .requestMatchers("/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/login/esqueci-senha", "/login/redefinir-senha").permitAll()

                        .requestMatchers(HttpMethod.PATCH, "/administradores/meu-perfil/**").hasRole("ADMINISTRADOR")

                        // Cadastro de cliente é público (tela de login).
                        .requestMatchers(HttpMethod.POST, "/clientes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/barbearias/principal", "/servicos/lista/ativos", "/barbeiros/ativos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/barbearias/principal/administracao", "/barbearias", "/barbearias/{id}").hasRole("ADMINISTRADOR")

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
                        .requestMatchers(HttpMethod.PATCH, "/barbeiros/senha/{id}").hasRole("BARBEIRO")



                        // Somente ADMINISTRADOR pode cadastrar serviço.
                        .requestMatchers(HttpMethod.POST, "/servicos/**")
                        .hasRole("ADMINISTRADOR")
                        // Lista de serviços ativos: qualquer usuário autenticado.
                        .requestMatchers(HttpMethod.GET, "/servicos/lista/ativos").permitAll()


                        // Qualquer outra rota precisa estar autenticada.
                        .anyRequest().authenticated()
                )

                // Roda antes do filtro padrão de usuário/senha, populando o SecurityContext
                // a partir do token, quando presente e válido.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .userDetailsService(userDetailsService);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuracao = new CorsConfiguration();
        configuracao.setAllowedOriginPatterns(Arrays.stream(origensPermitidas.split(","))
                .map(String::trim).filter(origem -> !origem.isEmpty()).toList());
        configuracao.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuracao.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuracao.setExposedHeaders(List.of("Authorization", "Retry-After"));
        configuracao.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuracao);
        return source;
    }
}


