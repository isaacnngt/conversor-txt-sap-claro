package br.com.guruagil.dto;

public class ImportacaoResultadoDTO {
    private boolean sucesso;
    private String mensagem;
    private Long arquivoId;

    public ImportacaoResultadoDTO() {
    }

    public ImportacaoResultadoDTO(boolean sucesso, String mensagem, Long arquivoId) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.arquivoId = arquivoId;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    //getters e setters

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Long getArquivoId() {
        return arquivoId;
    }

    public void setArquivoId(Long arquivoId) {
        this.arquivoId = arquivoId;
    }
}