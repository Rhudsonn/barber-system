package com.zheiry.barbersystem.entities;

import com.zheiry.barbersystem.enums.NivelAcesso;
import com.zheiry.barbersystem.enums.Status;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "barbeiros")
public class BarbeiroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false)
    private String nome;

    // O java.time.LocalDate foi introduzido no Java 8 para substituir a antiga classe java.util.Date
    // LocalDate: Armazena estritamente a data no formato AAAA-MM-DD (ex: 1995-10-25).
    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false, unique = true)
    private String telefone;

    @Column(nullable = false)
    private String especialidade;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NivelAcesso nivelAcesso;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    // Relacionamento com AGENDAMENTO
    @OneToMany(mappedBy = "barbeiro", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    private List<AgendamentoEntity> agendamentos = new ArrayList<>();

    // Relacionamento com HrarioDeTrabalho
    @OneToMany(mappedBy = "barbeiro", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HorarioDeTrabalhoEntity> horarios = new ArrayList<>();


    public BarbeiroEntity() {
    }

    public BarbeiroEntity(Long id, String nome, LocalDate dataNascimento, String telefone, String especialidade, String email, String senha, NivelAcesso nivelAcesso, Status status, List<AgendamentoEntity> agendamentos, List<HorarioDeTrabalhoEntity> horarios) {
        this.id = id;
        this.nome = nome;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.especialidade = especialidade;
        this.email = email;
        this.senha = senha;
        this.nivelAcesso = nivelAcesso;
        this.status = status;
        this.agendamentos = agendamentos;
        this.horarios = horarios;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String especialidade) {
        this.especialidade = especialidade;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public NivelAcesso getNivelAcesso() {
        return nivelAcesso;
    }

    public void setNivelAcesso(NivelAcesso nivelAcesso) {
        this.nivelAcesso = nivelAcesso;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<AgendamentoEntity> getAgendamentos() {
        return agendamentos;
    }

    public void setAgendamentos(List<AgendamentoEntity> agendamentos) {
        this.agendamentos = agendamentos;
    }

    public List<HorarioDeTrabalhoEntity> getHorarios() {
        return horarios;
    }

    public void setHorarios(List<HorarioDeTrabalhoEntity> horarios) {
        this.horarios = horarios;
    }
}
