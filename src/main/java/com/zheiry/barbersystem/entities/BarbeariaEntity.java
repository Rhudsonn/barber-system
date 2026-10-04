package com.zheiry.barbersystem.entities;

import jakarta.persistence.*;

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

    //@OneToMany(mappedBy = "barbearia", cascade = CascadeType.ALL, orphanRemoval = true)
    //private List<HorarioFuncionamentoEntity> horarios = new ArrayList<>();

}
