package br.gov.caixa.gitecsa.sired.dto;

import java.util.Date;

import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;
import br.gov.caixa.gitecsa.sired.vo.MotivoAvaliacaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;


public class FiltroRequisicaoDTO {

    private String numeroRequisicoes;

    private String matriculaUsuario;

    private UnidadeVO unidadeSolicitante;

    private Date dataInicio;

    private Date dataFim;

    private SituacaoRequisicaoEnum situacao;

    private MotivoAvaliacaoVO motivo;
    
    private BaseVO base;
    
    private EmpresaVO empresa;
        
    public String getNumeroRequisicoes() {
        return numeroRequisicoes;
    }

    public void setNumeroRequisicoes(final String numeroRequisicoes) {
        this.numeroRequisicoes = numeroRequisicoes;
    }

    public String getMatriculaUsuario() {
        return matriculaUsuario;
    }

    public void setMatriculaUsuario(final String matriculaUsuario) {
        this.matriculaUsuario = matriculaUsuario;
    }

    public UnidadeVO getUnidadeSolicitante() {
        return unidadeSolicitante;
    }

    public void setUnidadeSolicitante(final UnidadeVO unidadeSolicitante) {
        this.unidadeSolicitante = unidadeSolicitante;
    }

    public Date getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(final Date dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Date getDataFim() {
        return dataFim;
    }

    public void setDataFim(final Date dataFim) {
        this.dataFim = dataFim;
    }
    
    public SituacaoRequisicaoEnum getSituacao() {
        return situacao;
    }

    public void setSituacao(final SituacaoRequisicaoEnum situacao) {
        this.situacao = situacao;
    }

    public MotivoAvaliacaoVO getMotivo() {
        return motivo;
    }

    public void setMotivo(final MotivoAvaliacaoVO motivo) {
        this.motivo = motivo;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(final BaseVO base) {
        this.base = base;
    }

    public EmpresaVO getEmpresa() {
        return empresa;
    }

    public void setEmpresa(final EmpresaVO empresa) {
        this.empresa = empresa;
    }

}
