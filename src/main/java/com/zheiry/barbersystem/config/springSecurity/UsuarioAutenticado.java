package com.zheiry.barbersystem.config.springSecurity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UsuarioAutenticado implements UserDetails {

    // Esta classe esta me ajudando porque aqui eu tenho um UserDetails customizado com o id
    // porque o User padrão do Spring Security não tem o campo id, e ja neste customizado tem

    private final Long id;
    private final String email;
    private final String senha;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean ativo;

    public UsuarioAutenticado(Long id, String email, String senha, Collection<? extends GrantedAuthority> authorities, boolean ativo) {
        this.id = id;
        this.email = email;
        this.senha = senha;
        this.authorities = authorities;
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
