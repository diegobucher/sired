package br.gov.caixa.gitecsa.dto;

import java.util.List;

import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class ConsultaRequisicaoDTO {

    private RequisicaoVO requisicao;

    private BaseVO base;

    private AvaliacaoRequisicaoVO avaliacaoRequisicao;

    private List<RequisicaoDocumentoVO> requisicaoDocumentos;

    public AvaliacaoRequisicaoVO getAvaliacaoRequisicao() {
        return avaliacaoRequisicao;
    }

    public void setAvaliacaoRequisicao(AvaliacaoRequisicaoVO avaliacaoRequisicao) {
        this.avaliacaoRequisicao = avaliacaoRequisicao;
    }

    public RequisicaoVO getRequisicao() {
        return requisicao;
    }

    public void setRequisicao(RequisicaoVO requisicao) {
        this.requisicao = requisicao;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public List<RequisicaoDocumentoVO> getRequisicaoDocumentos() {
        return requisicaoDocumentos;
    }

    public void setRequisicaoDocumentos(List<RequisicaoDocumentoVO> requisicaoDocumentos) {
        this.requisicaoDocumentos = requisicaoDocumentos;
    }

}
