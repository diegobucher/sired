package br.gov.caixa.gitecsa.sired.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.TramiteRequisicaoDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless(name = "tramiteService")
public class TramiteRequisicaoService extends AbstractService<TramiteRequisicaoVO> {

    private static final long serialVersionUID = -5756580844519809453L;

    @Inject
    private TramiteRequisicaoDAO dao;

    @Inject
    private SituacaoRequisicaoService situacaoService;

    @Override
    protected void validaCamposObrigatorios(TramiteRequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(TramiteRequisicaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(TramiteRequisicaoVO entity) {

    }

    @Override
    protected GenericDAO<TramiteRequisicaoVO> getDAO() {
        return this.dao;
    }

    public List<TramiteRequisicaoVO> pesquisarHistorico(RequisicaoVO requisicao) {
        return this.dao.findByRequisicao(requisicao);
    }

    public TramiteRequisicaoVO getBySituacaoRequisicao(SituacaoRequisicaoEnum value) {
        return this.getBySituacaoRequisicao(value, JSFUtil.getUsuario().getNuMatricula());
    }

    public TramiteRequisicaoVO getBySituacaoRequisicao(SituacaoRequisicaoEnum value, String usuario) {

        TramiteRequisicaoVO instance = new TramiteRequisicaoVO();
        instance.setCodigoUsuario(usuario);
        instance.setDataHora(new Date());
        instance.setSituacaoRequisicao(this.situacaoService.findByEnum(value));

        return instance;
    }

    public List<TramiteRequisicaoVO> consultaTramitesEmailSolicitante() {
        return dao.consultaTramitesEmailSolicitante();
    }

    public List<TramiteRequisicaoVO> consultaTramitesEmailBase() {
        return dao.consultaTramitesEmailBase();
    }

    public List<TramiteRequisicaoVO> consultaTramitesEmailTerceirizada() {
        return dao.consultaTramitesEmailTerceirizada();
    }

    public List<TramiteRequisicaoVO> findAtendimentosRequisicao(RequisicaoVO requisicao) {
        return this.dao.findAtendimentosRequisicao(requisicao);
    }
}
