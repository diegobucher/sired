package br.gov.caixa.gitecsa.sired.dto;


public class ResumoAtendimentoRequisicaoDTO {
    
    private String base;
    
    private Integer numAbertas;
    
    private Integer numFechadas;
    
    private Integer numRecuperadas;
    
    private Integer numNaoLocalizadas;
    
    private Integer numOutras;
    
    public ResumoAtendimentoRequisicaoDTO() {
        this.numAbertas = 0;
        this.numFechadas = 0;
        this.numRecuperadas = 0;
        this.numNaoLocalizadas = 0;
        this.numOutras = 0;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public Integer getNumAbertas() {
        return numAbertas;
    }

    public void setNumAbertas(Integer numAbertas) {
        this.numAbertas = numAbertas;
    }

    public Integer getNumFechadas() {
        return numFechadas;
    }

    public void setNumFechadas(Integer numFechadas) {
        this.numFechadas = numFechadas;
    }

    public Integer getNumRecuperadas() {
        return numRecuperadas;
    }

    public void setNumRecuperadas(Integer numRecuperadas) {
        this.numRecuperadas = numRecuperadas;
    }

    public Integer getNumNaoLocalizadas() {
        return numNaoLocalizadas;
    }

    public void setNumNaoLocalizadas(Integer numNaoLocalizadas) {
        this.numNaoLocalizadas = numNaoLocalizadas;
    }

    public Integer getNumOutras() {
        return numOutras;
    }

    public void setNumOutras(Integer numOutras) {
        this.numOutras = numOutras;
    }
}
