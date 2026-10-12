package com.zheiry.barbersystem.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "barbearia")
public class BarbeariaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nomeFantasia;

    @Column(nullable = false, length = 150)
    private String razaoSocial;

    @Column(nullable = false, unique = true, length = 14) // só dígitos
    private String cnpj;

    @Column(nullable = false, length = 11, unique = true)
    private String telefone;

    @Column(nullable = false, length = 11, unique = true)
    private String whatsapp;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 150)
    private String endereco;

    @Column(nullable = false, length = 100)
    private String cidade;


    @Column(nullable = false)
    private String estado;

    @Column(nullable = false, length = 8)
    private String cep;

    @Column(nullable = false)
    private String descricao;

    @OneToMany(mappedBy = "barbearia", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioFuncionamentoEntity> horarios = new ArrayList<>();


    public BarbeariaEntity() {
    }

    public BarbeariaEntity(Long id, String nomeFantasia, String razaoSocial, String cnpj, String telefone, String whatsapp, String email, String endereco, String cidade, String estado, String cep, String descricao, List<HorarioFuncionamentoEntity> horarios) {
        this.id = id;
        this.nomeFantasia = nomeFantasia;
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.telefone = telefone;
        this.whatsapp = whatsapp;
        this.email = email;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
        this.descricao = descricao;
        this.horarios = horarios;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<HorarioFuncionamentoEntity> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioFuncionamentoEntity> horarios) {
        this.horarios = horarios;
    }
}
