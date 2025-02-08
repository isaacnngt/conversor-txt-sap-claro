package br.com.guruagil.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ARQUIVO_TB")
public class Arquivo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ARQUIVO")
    private Long id;

    @Column(name = "NOME", length = 80)
    private String nome;

    @Column(name = "DT_INCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "DT_ALTERACAO")
    private LocalDateTime dataAlteracao;

    @Column(name = "SN_IMPORTADO", length = 1)
    private String statusImportacao;

    @Column(name = "USUARIO", length = 10)
    private String usuario;

    @OneToMany(mappedBy = "arquivo", cascade = CascadeType.ALL)
    private List<Pergunta> perguntas = new ArrayList<>();

    // Getters e Setters
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

    public LocalDateTime getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(LocalDateTime dataInclusao) {
        this.dataInclusao = dataInclusao;
    }

    public LocalDateTime getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(LocalDateTime dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    public String getStatusImportacao() {
        return statusImportacao;
    }

    public void setStatusImportacao(String statusImportacao) {
        this.statusImportacao = statusImportacao;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public List<Pergunta> getPerguntas() {
        return perguntas;
    }

    public void setPerguntas(List<Pergunta> perguntas) {
        this.perguntas = perguntas;
    }

    @PrePersist
    public void prePersist() {
        this.dataInclusao = LocalDateTime.now();
        this.dataAlteracao = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.dataAlteracao = LocalDateTime.now();
    }
}
