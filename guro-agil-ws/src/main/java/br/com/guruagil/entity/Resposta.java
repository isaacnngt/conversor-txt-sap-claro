package br.com.guruagil.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RESPOSTA_TB")
public class Resposta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_RESPOSTA")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_PERGUNTA_FK")
    private Pergunta pergunta;

    @ManyToOne
    @JoinColumn(name = "ID_ARQUIVO_FK")
    private Arquivo arquivo;

    @Column(name = "RESPOSTA")
    private String resposta;

    @Column(name = "DT_INCLUSAO")
    private LocalDateTime dataInclusao;

    @Column(name = "DT_ALTERACAO")
    private LocalDateTime dataAlteracao;

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pergunta getPergunta() {
        return pergunta;
    }

    public void setPergunta(Pergunta pergunta) {
        this.pergunta = pergunta;
    }

    public Arquivo getArquivo() {
        return arquivo;
    }

    public void setArquivo(Arquivo arquivo) {
        this.arquivo = arquivo;
    }

    public String getResposta() {
        return resposta;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
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
