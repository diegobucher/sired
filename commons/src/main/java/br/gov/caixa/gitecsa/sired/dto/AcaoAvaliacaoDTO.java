package br.gov.caixa.gitecsa.sired.dto;

import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;

public class AcaoAvaliacaoDTO {

    private String label;
    private SimNaoEnum acao;
    
    public AcaoAvaliacaoDTO(String label, SimNaoEnum acao) {
        this.label = label;
        this.acao = acao;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public SimNaoEnum getAcao() {
        return acao;
    }

    public void setAcao(SimNaoEnum acao) {
        this.acao = acao;
    }

}
