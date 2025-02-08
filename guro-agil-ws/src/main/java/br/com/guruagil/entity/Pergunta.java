package br.com.guruagil.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PERGUNTA_TB")
public class Pergunta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERGUNTA")
    private Long id;

    @Column(name = "DESCRICAO", length = 300)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "ID_ARQUIVO_FK")
    private Arquivo arquivo;

    @Column(name = "DT_INCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "DT_ALTERACAO")
    private LocalDateTime dataAlteracao;

    @OneToMany(mappedBy = "pergunta", cascade = CascadeType.ALL)
    private List<Resposta> respostas = new ArrayList<>();

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Arquivo getArquivo() {
        return arquivo;
    }

    public void setArquivo(Arquivo arquivo) {
        this.arquivo = arquivo;
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

    public List<Resposta> getRespostas() {
        return respostas;
    }

    public void setRespostas(List<Resposta> respostas) {
        this.respostas = respostas;
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

