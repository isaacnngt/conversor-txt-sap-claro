package br.com.guruagil.dto;
import java.util.List;

public class PerguntaDTO {
    private String descricao;
    private List<String> alternativas;

    public PerguntaDTO() {
    }

    public PerguntaDTO(String descricao, List<String> alternativas) {
        this.descricao = descricao;
        this.alternativas = alternativas;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public List<String> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas;
    }
}