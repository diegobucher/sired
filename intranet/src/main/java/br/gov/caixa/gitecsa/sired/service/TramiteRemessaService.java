package br.gov.caixa.gitecsa.sired.service;

import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.dao.TramiteRemessaDAO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless(name = "tramiteServiceRemessa")
public class TramiteRemessaService extends AbstractService<TramiteRemessaVO> {

  private static final long serialVersionUID = -7249953150284621755L;

  @Inject
  private TramiteRemessaDAO dao;

  @Inject
  private SituacaoRemessaService situacaoService;

  @Inject
  private FeriadoService feriadoService;

  @Override
  protected void validaCamposObrigatorios(TramiteRemessaVO entity) {

  }

  @Override
  protected void validaRegras(TramiteRemessaVO entity) {

  }

  @Override
  protected void validaRegrasExcluir(TramiteRemessaVO entity) {

  }

  @Override
  protected GenericDAO<TramiteRemessaVO> getDAO() {
    return this.dao;
  }

  public List<TramiteRemessaVO> pesquisarHistorico(RemessaVO remessa) {
    return this.dao.findByRemessa(remessa);
  }

  public TramiteRemessaVO getRascunho() {

    TramiteRemessaVO tramite = getInstance();
    tramite.setSituacao(this.situacaoService.findByEnum(SituacaoRemessaEnum.RASCUNHO));

    return tramite;
  }

  private static TramiteRemessaVO getInstance() {

    TramiteRemessaVO instance = new TramiteRemessaVO();
    instance.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
    instance.setDataTramiteRemessa(new Date());

    return instance;
  }

  public List<TramiteRemessaVO> consultaTramitesEmailSolicitante() {
    return dao.consultaTramitesEmailSolicitante();
  }
  
  public List<TramiteRemessaVO> consultaTramitesEmailBase() {
    return dao.consultaTramitesEmailBase();
  }

  public List<TramiteRemessaVO> consultaTramitesEmailTerceirizada() {
    return dao.consultaTramitesEmailTerceirizada();
  }

  public String consultaTramiteAbertura(final Long idRequisicao) {
    return dao.consultaTramiteAbertura(idRequisicao);
  }

  public TramiteRemessaVO salvarTramiteRemesssa(RemessaVO remessaVO, SituacaoRemessaVO situacao) throws AppException {
    TramiteRemessaVO vo = new TramiteRemessaVO();
    vo.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
    vo.setDataTramiteRemessa(new Date());
    vo.setRemessa(remessaVO);
    vo.setSituacao(situacao);
    vo = this.salvar(vo);
    return vo;
  }

  public TramiteRemessaVO salvar(TramiteRemessaVO vo) {
    if (vo.getId() != null) {
      return this.dao.update(vo);
    } else {
      return this.dao.save(vo);
    }

  }

  public TramiteRemessaVO salvarTramiteRemessaAberta(RemessaVO remessa) throws AppException {
    SituacaoRemessaVO situacao = situacaoService.findByEnum(SituacaoRemessaEnum.ABERTA);
    return salvarTramiteRemesssa(remessa, situacao);
  }

  public TramiteRemessaVO salvarTramiteRemessaRascunho(RemessaVO remessa) throws Exception {
    SituacaoRemessaVO situacao = situacaoService.findByEnum(SituacaoRemessaEnum.RASCUNHO);
    return salvarTramiteRemesssa(remessa, situacao);
  }
  
  public TramiteRemessaVO salvarTramiteRemessaAgendada(RemessaVO remessa) throws Exception {
    TramiteRemessaVO tramiteTemp = new TramiteRemessaVO();
    tramiteTemp.setCodigoUsuario(JSFUtil.getUsuario().getNuMatricula());
    tramiteTemp.setDataTramiteRemessa(new Date());
    tramiteTemp.setDataAgendamento(feriadoService.findProximaDataUtil(new Date(), 1, remessa.getUnidadeSolicitante()));
    tramiteTemp.setRemessa(remessa);
    tramiteTemp.setSituacao(situacaoService.findByEnum(SituacaoRemessaEnum.AGENDADA));
    return this.save(tramiteTemp);
  }

  public TramiteRemessaVO getBySituacaoRemessa(SituacaoRemessaEnum value, String usuario) {

    TramiteRemessaVO instance = new TramiteRemessaVO();
    instance.setCodigoUsuario(usuario);
    instance.setDataTramiteRemessa(new Date());
    instance.setSituacao(situacaoService.findById(value.getId()));

    return instance;
  }
}
