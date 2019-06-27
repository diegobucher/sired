package br.gov.caixa.gitecsa.sired.extra.dto;

import java.util.ArrayList;
import java.util.List;

import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SuporteVO;

public class AtendimentoLoteDTO {

    private RequisicaoVO requisicao;

    private OcorrenciaAtendimentoVO ocorrencia;

    private SituacaoRequisicaoVO situacaoRequisicao;

    private SuporteVO suporte;

    private Integer qtdDisponibilizada;

    private String observacao;

    private List<String> mensagensValidacao;

    public RequisicaoVO getRequisicao() {
        return requisicao;
    }

    public void setRequisicao(RequisicaoVO requisicao) {
        this.requisicao = requisicao;
    }

    public OcorrenciaAtendimentoVO getOcorrencia() {
        return ocorrencia;
    }

    public void setOcorrencia(OcorrenciaAtendimentoVO ocorrencia) {
        this.ocorrencia = ocorrencia;
    }

    public SituacaoRequisicaoVO getSituacaoRequisicao() {
        return situacaoRequisicao;
    }

    public void setSituacaoRequisicao(SituacaoRequisicaoVO situacaoRequisicao) {
        this.situacaoRequisicao = situacaoRequisicao;
    }

    public SuporteVO getSuporte() {
        return suporte;
    }

    public void setSuporte(SuporteVO suporte) {
        this.suporte = suporte;
    }

    public Integer getQtdDisponibilizada() {
        return qtdDisponibilizada;
    }

    public void setQtdDisponibilizada(Integer qtdDisponibilizada) {
        this.qtdDisponibilizada = qtdDisponibilizada;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public List<String> getMensagensValidacao() {
        return mensagensValidacao;
    }

    public void adicionarMensagemValidacao(String... mensagens) {

        if (ObjectUtils.isNullOrEmpty(this.mensagensValidacao)) {
            this.mensagensValidacao = new ArrayList<String>();
        }

        for (String m : mensagens) {
            this.mensagensValidacao.add(m);
        }
    }
}
