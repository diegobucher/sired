package br.gov.caixa.gitecsa.sired.dto;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class CodigoRequisicaoDTO {

    private static final int NUM_CARACTERES_SEQUENCIAL = 5;

    private UnidadeVO unidade;

    private Integer nuAno;

    private Integer nuRequisicao;

    public UnidadeVO getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeVO unidade) {
        this.unidade = unidade;
    }

    public Integer getNuAno() {
        return nuAno;
    }

    public void setNuAno(Integer nuAno) {
        this.nuAno = nuAno;
    }

    public Integer getNuRequisicao() {
        return nuRequisicao;
    }

    public void setNuRequisicao(Integer nuRequisicao) {
        this.nuRequisicao = nuRequisicao;
    }

    @Override
    public String toString() {

        return String.format("%s%s%s", this.getUnidade().getId(), this.getNuAno(),
                StringUtils.leftPad(this.getNuRequisicao().toString(), NUM_CARACTERES_SEQUENCIAL, "0"));
    }

}
