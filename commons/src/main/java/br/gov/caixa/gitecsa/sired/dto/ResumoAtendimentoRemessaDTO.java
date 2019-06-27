package br.gov.caixa.gitecsa.sired.dto;


public class ResumoAtendimentoRemessaDTO {
    
    private String base;
    
    private Integer numAbertas;
    
    private Integer numFechadas;
    
    private Integer numAgendadas;
    
    private Integer numRecebidas;
    
    private Integer numConferidas;
    
    private Integer numRemessas;
    
    private Integer numItens;
    
    private Integer numRemessasDentroPrazo;
    
    public ResumoAtendimentoRemessaDTO() {
        this.numAbertas = 0;
        this.numFechadas = 0;
        this.numAgendadas = 0;
        this.numRecebidas = 0;
        this.numConferidas = 0;
        this.numRemessas = 0;
        this.numItens = 0;
        this.numRemessasDentroPrazo = 0;
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

    public Integer getNumAgendadas() {
        return numAgendadas;
    }

    public void setNumAgendadas(Integer numAgendadas) {
        this.numAgendadas = numAgendadas;
    }

    public Integer getNumRecebidas() {
        return numRecebidas;
    }

    public void setNumRecebidas(Integer numRecebidas) {
        this.numRecebidas = numRecebidas;
    }

    public Integer getNumConferidas() {
        return numConferidas;
    }

    public void setNumConferidas(Integer numConferidas) {
        this.numConferidas = numConferidas;
    }

    public Integer getNumRemessas() {
        return numRemessas;
    }

    public void setNumRemessas(Integer numRemessas) {
        this.numRemessas = numRemessas;
    }

    public Integer getNumItens() {
        return numItens;
    }

    public void setNumItens(Integer numItens) {
        this.numItens = numItens;
    }

    public Integer getNumRemessasDentroPrazo() {
        return numRemessasDentroPrazo;
    }

    public void setNumRemessasDentroPrazo(Integer numRemessasDentroPrazo) {
        this.numRemessasDentroPrazo = numRemessasDentroPrazo;
    }
}
