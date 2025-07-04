package br.com.guruagil.dto;
import java.util.List;
import java.util.Map;

public class RecomendacaoDTO {
    // Lista de metodologias recomendadas
    private List<String> metodologiasRecomendadas;
    // Mapa contendo as pontuações para cada metodologia
    private Map<String, Integer> pontuacoes;
    // Mensagem informativa sobre a recomendação
    private String mensagem;
    //Para armazenar as descricoes de cada tecnologia
    private Map<String, String> descricoes;

    // Construtor vazio
    public RecomendacaoDTO() {
    }

    // Construtor com parâmetros para inicializar os atributos
    public RecomendacaoDTO(List<String> metodologiasRecomendadas,
                           Map<String, Integer> pontuacoes,
                           Map<String, String> descricoes,
                           String mensagem) {
        this.metodologiasRecomendadas = metodologiasRecomendadas;
        this.pontuacoes = pontuacoes;
        this.descricoes = descricoes;
        this.mensagem = mensagem;
    }

    // Metodo getter para obter a lista de metodologias recomendadas
    public List<String> getMetodologiasRecomendadas() {
        return metodologiasRecomendadas;
    }

    // Metodo setter para definir a lista de metodologias recomendadas
    public void setMetodologiasRecomendadas(List<String> metodologiasRecomendadas) {
        this.metodologiasRecomendadas = metodologiasRecomendadas;
    }

    // Metodo getter para obter o mapa de pontuações
    public Map<String, Integer> getPontuacoes() {
        return pontuacoes;
    }

    // Metodo setter para definir o mapa de pontuações
    public void setPontuacoes(Map<String, Integer> pontuacoes) {
        this.pontuacoes = pontuacoes;
    }

    // Metodo getter para obter a mensagem informativa
    public String getMensagem() {
        return mensagem;
    }

    // Metodo setter para definir a mensagem informativa
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    // Getter para as descrições
    public Map<String, String> getDescricoes() {
        return descricoes;
    }

    // Setter para as descrições
    public void setDescricoes(Map<String, String> descricoes) {
        this.descricoes = descricoes;
    }
}