package br.com.guruagil.dto;
import java.util.Map;

public class RecomendacaoDTO {
    private String metodologiaRecomendada;
    private Map<String, Integer> pontuacoes;
    private String mensagem;

    public RecomendacaoDTO() {
    }

    public RecomendacaoDTO(String metodologiaRecomendada, Map<String, Integer> pontuacoes, String mensagem) {
        this.metodologiaRecomendada = metodologiaRecomendada;
        this.pontuacoes = pontuacoes;
        this.mensagem = mensagem;
    }

    public String getMetodologiaRecomendada() {
        return metodologiaRecomendada;
    }

    public void setMetodologiaRecomendada(String metodologiaRecomendada) {
        this.metodologiaRecomendada = metodologiaRecomendada;
    }

    public Map<String, Integer> getPontuacoes() {
        return pontuacoes;
    }

    public void setPontuacoes(Map<String, Integer> pontuacoes) {
        this.pontuacoes = pontuacoes;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
