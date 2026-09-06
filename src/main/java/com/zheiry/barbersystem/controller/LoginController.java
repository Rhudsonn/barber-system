package com.zheiry.barbersystem.controller;


import com.zheiry.barbersystem.config.jwt.JwtService;
import com.zheiry.barbersystem.config.springSecurity.CustomUserDetailsService;
import com.zheiry.barbersystem.dto.LoginEntradaDto;
import com.zheiry.barbersystem.dto.LoginSaidaDto;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public LoginController(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping
    public ResponseEntity<LoginSaidaDto> login(@RequestBody @Valid LoginEntradaDto dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.senha())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.email());
        String token = jwtService.gerarToken(userDetails);

        return ResponseEntity.ok(new LoginSaidaDto(token, "Bearer"));
    }
}
