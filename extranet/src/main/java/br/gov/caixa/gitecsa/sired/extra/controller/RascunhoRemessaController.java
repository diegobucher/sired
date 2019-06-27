package br.gov.caixa.gitecsa.sired.extra.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.service.RemessaService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.extra.service.RemessaMovimentoDiarioService;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RascunhoRemessaController implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = 5732371421970887959L;

  @Inject
  protected FacesMensager facesMessager;

  @Inject
  protected transient Logger logger;

  @Inject
  private UnidadeService unidadeService;

  @Inject
  private RemessaService remessaService;

  @Inject
  private RemessaMovimentoDiarioService remessaMovimentoDiarioService;

  private RemessaVO remessa;

  private RemessaMovimentoDiarioVO movimentoDiario;

  private UnidadeVO unidadeGeradora;

  private String codigoUnidadeFiltro;

  private String nomeUnidadeFiltro;

  private List<MovimentoDiarioRemessaCDTO> dataMovimentosList;
  
  private List<RemessaDocumentoVO> listaDocumentos;

  @PostConstruct
  protected void init() {
    try {
      this.limparCampos();
      if (JSFUtil.getSessionMapValue("remessa") != null) {
        this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
        this.remessa = remessaService.obterRemessaComMovimentosDiarios((Long) this.remessa.getId());
        this.unidadeGeradora = this.remessa.getUnidadeSolicitante();
        this.carregarAgrupamentoMovimentosPorDia();
        JSFUtil.setSessionMapValue("remessa", null);
      } else {
        FacesContext.getCurrentInstance().getExternalContext().redirect("mantem-remessa");
      }
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
    }
  }

  private void carregarAgrupamentoMovimentosPorDia() {
    this.dataMovimentosList = remessaService.obterAgrupamentoDeItensDeRemessaPorDiaUnidade(this.remessa);
  }
  
  private List<RemessaMovimentoDiarioVO> filtrarListaMovimentosCarregados(MovimentoDiarioRemessaCDTO movimentoDiarioRemessaCDTO) {
    List<RemessaMovimentoDiarioVO> listaMovimentoDiario = new ArrayList<RemessaMovimentoDiarioVO>();
    List<RemessaMovimentoDiarioVO> listaAux = new ArrayList<RemessaMovimentoDiarioVO>();
    listaAux.addAll( movimentoDiarioRemessaCDTO.getRemessaMovDiarioList());
    listaMovimentoDiario = listaAux;
    Iterator<RemessaMovimentoDiarioVO> itr = listaMovimentoDiario.iterator();
    
    while(itr.hasNext()) {
      RemessaMovimentoDiarioVO remessaMovAux = itr.next();
      if(remessaMovAux.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
        itr.remove();
      }
    }

    return listaMovimentoDiario;
  }

  private void limparCampos() {
    this.movimentoDiario = null;
    this.codigoUnidadeFiltro = null;
    this.nomeUnidadeFiltro = null;
    this.remessa = null;
    this.dataMovimentosList = new ArrayList<>();
  }

  public void pesquisarUnidadeGeradora() {
    try {
      if (StringUtils.isBlank(codigoUnidadeFiltro)) {
        this.movimentoDiario.setUnidadeGeradora(null);
        this.nomeUnidadeFiltro = "";
      } else {
        UnidadeVO unidadeFiltro = new UnidadeVO();
        unidadeFiltro.setId(Long.parseLong(codigoUnidadeFiltro));
        List<UnidadeVO> unidades = unidadeService.findByParameters(unidadeFiltro);

        if (!Util.isNullOuVazio(unidades)) {
          this.movimentoDiario.setUnidadeGeradora(unidades.get(0));
          this.nomeUnidadeFiltro = this.movimentoDiario.getUnidadeGeradora().getNome().trim();
        } else {
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
        }
      }
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "RemessaController", "pesquisarUnidadeGeradora"));
    }
  }

  public String incluirMovimento() {
    JSFUtil.setSessionMapValue("remessa", this.remessa);
    return "/paginas/remessa/aberturaMovDiario.xhtml?faces-redirect=true";
  }

  public String excluirItemRemessa(RemessaMovimentoDiarioVO item) {
    this.movimentoDiario = item;
    return null;
  }

  public String confirmarExclusaoItemRemessa() throws Exception {
    RemessaMovimentoDiarioVO remessaMovimentoPai = new RemessaMovimentoDiarioVO();
    remessaMovimentoPai = this.movimentoDiario.getNumeroRemessaTipoC();
    if(remessaMovimentoPai == null) {
      remessaMovimentoDiarioService.excluirItemMovimentoDiarioPorId(this.movimentoDiario);
    }else {
      remessaMovimentoPai.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
      remessaMovimentoDiarioService.salvarItemMovimentoDiario(remessaMovimentoPai);
      remessaMovimentoDiarioService.excluirItemMovimentoDiarioPorId(this.movimentoDiario);
    }
    
    List<RemessaMovimentoDiarioVO> listMovimentoDiario = this.remessa.getMovimentosDiarioList();
    Iterator<RemessaMovimentoDiarioVO> iterator = listMovimentoDiario.iterator();
    while (iterator.hasNext()) {
      RemessaMovimentoDiarioVO movimentoDiario = iterator.next();
      if (this.movimentoDiario.getId().equals(movimentoDiario.getId())) {
        iterator.remove();
        movimentoDiario.setRemessa(null);
        this.movimentoDiario = null;
      }
    }
    
    int contador = 0;
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listMovimentoDiario) {
      if(remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA) || 
          remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
        contador++;
      }
    }
    if(contador == 0) {
      RemessaVO remessaVO = new RemessaVO();
      remessaVO = remessaService.obterRemessaComListaDocumentos((long)this.remessa.getId());
      remessaService.recebidaRemessaDocumento(remessaVO);
    }
    
    carregarAgrupamentoMovimentosPorDia();
    facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS016"));
    return null;
  }

  public String cancelarExclusaoItemRemessa() {
    this.movimentoDiario = null;
    return null;
  }

  public String salvarRascunho() {
    try {
      if (this.remessa.getLacre() == null) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.lacreFiltro"));
        return null;
      }
      recarregarListadeRemessasParaPersistir();
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS046"));
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
      return null;
    }
  }

  private void recarregarListadeRemessasParaPersistir() {
    Long lacre = this.remessa.getLacre();
    this.remessa = remessaService.obterRemessaComMovimentosDiarios((Long) this.remessa.getId());
    this.remessa.setLacre(lacre);
  }

  public String prepareConcluirRemessa() {
    try {
      if (this.remessa.getLacre() == null) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.lacreFiltro"));
        return null;
      }
      recarregarListadeRemessasParaPersistir();
      RequestContext.getCurrentInstance().execute("modalConfirmar.show()");
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
      return null;
    }
  }
  
  public String concluirRemessa() {
    MensagemUtils.setKeepMessages(true);
    try {
      if (this.remessa.getMovimentosDiarioList().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA067"));
        return null;
      }
      recarregarListadeRemessasParaPersistir();
      alterarSituacaoRemessaParaAlterada(this.remessa);
      remessaService.concluirRemessaMovimentoDiarioTipoC(this.remessa);
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS030", this.remessa.getId()));
      return "/paginas/remessa/consulta.xhtml?faces-redirect=true";
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
      return null;
    }
  }
  
  private void alterarSituacaoRemessaParaAlterada(RemessaVO remessaVO) {
    List<RemessaMovimentoDiarioVO> listMovimentoDiario = remessaVO.getMovimentosDiarioList();
    Iterator<RemessaMovimentoDiarioVO> iterator = listMovimentoDiario.iterator();
    
    while (iterator.hasNext()) {
      RemessaMovimentoDiarioVO movimentoDiario = iterator.next();
      if (movimentoDiario.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA)) {
        movimentoDiario.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA); 
        remessaMovimentoDiarioService.salvarItemMovimentoDiario(movimentoDiario);
      }
    }
   
 }

  public String prepareCancelarCorrecaoRemessa() {
    try {
      if(!this.remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.RECEBIDA.getId())) {
        RequestContext.getCurrentInstance().execute("modalCorrigir.show()");
      }else {
        return "/paginas/remessa/consulta.xhtml?faces-redirect=true";
      }
      return null;
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
      return null;
    }
  }

  public String cancelarCorrecao() {
    MensagemUtils.setKeepMessages(true);
    try {
      if (this.remessa.getMovimentosDiarioList().isEmpty()) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA067"));
        return null;
      }
      removeAlteracoes(this.remessa);
      facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS031", this.remessa.getId()));
      return "/paginas/remessa/consulta.xhtml?faces-redirect=true";
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
      return null;
    }
  }
  
  private void removeAlteracoes(RemessaVO remessaVO) throws Exception {
    List<RemessaMovimentoDiarioVO> listMovimentoDiario = remessaVO.getMovimentosDiarioList();
    Iterator<RemessaMovimentoDiarioVO> iterator = listMovimentoDiario.iterator();
    
    while (iterator.hasNext()) {
      RemessaMovimentoDiarioVO movimentoDiario = iterator.next();
      if (movimentoDiario.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA)) {
        remessaMovimentoDiarioService.excluirItemMovimentoDiarioPorId(movimentoDiario);
      }else if(movimentoDiario.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
        movimentoDiario.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
        remessaMovimentoDiarioService.salvarItemMovimentoDiario(movimentoDiario);
      }else if (movimentoDiario.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
        remessaMovimentoDiarioService.excluirItemMovimentoDiarioPorId(movimentoDiario);
      }
    }
    carregarAgrupamentoMovimentosPorDia();
    remessaVO = remessaService.getById(this.remessa.getId());
    remessaService.recebidaRemessaDocumento(remessaVO);
  }

  public String editarRemessa(MovimentoDiarioRemessaCDTO itemRemessaDTO) {
    JSFUtil.setSessionMapValue("remessa", this.remessa);
    JSFUtil.setSessionMapValue("itemRemessa", itemRemessaDTO.getRemessaMovDiarioList().get(0));
    return "/paginas/remessa/aberturaMovDiario.xhtml?faces-redirect=true";
  }

  public String obterNomeUnidadeComData(MovimentoDiarioRemessaCDTO itemRemessaDTO) {
    String titulo = "";
    UnidadeVO unidade = unidadeService.getById(itemRemessaDTO.getUnidade());
    titulo += unidade.getDescricaoCompleta();
    titulo += " - DATA MOVIMENTO: " + itemRemessaDTO.getDataFormatada();
    return titulo;
  }

  /**
   * Mostra o componente do widgetvar especificado
   * @param widgetvar
   */
  protected void showDialog(String widgetvar) {
    RequestContext.getCurrentInstance().execute(widgetvar + ".show()");
  }

  public int sortByString(Object msg1, Object msg2) {
    return ((String) msg1).compareToIgnoreCase(((String) msg2));
  }

  /**
   * @return the codigoUnidadeFiltro
   */
  public String getCodigoUnidadeFiltro() {
    return codigoUnidadeFiltro;
  }

  /**
   * @param codigoUnidadeFiltro the codigoUnidadeFiltro to set
   */
  public void setCodigoUnidadeFiltro(String codigoUnidadeFiltro) {
    this.codigoUnidadeFiltro = (String) this.remessa.getUnidadeSolicitante().getId();
  }

  /**
   * @return the nomeUnidadeFiltro
   */
  public String getNomeUnidadeFiltro() {
    return nomeUnidadeFiltro;
  }

  /**
   * @param nomeUnidadeFiltro the nomeUnidadeFiltro to set
   */
  public void setNomeUnidadeFiltro(String nomeUnidadeFiltro) {
    this.nomeUnidadeFiltro = nomeUnidadeFiltro;
  }

  /**
   * @return the movimentoDiario
   */
  public RemessaMovimentoDiarioVO getMovimentoDiario() {
    return movimentoDiario;
  }

  /**
   * @param movimentoDiario the movimentoDiario to set
   */
  public void setMovimentoDiario(RemessaMovimentoDiarioVO movimentoDiario) {
    this.movimentoDiario = movimentoDiario;
  }

  /**
   * @return the remessa
   */
  public RemessaVO getRemessa() {
    return remessa;
  }

  /**
   * @param remessa the remessa to set
   */
  public void setRemessa(RemessaVO remessa) {
    this.remessa = remessa;
  }

  /**
   * @return the unidadeGeradora
   */
  public UnidadeVO getUnidadeGeradora() {
    return unidadeGeradora;
  }

  /**
   * @param unidadeGeradora the unidadeGeradora to set
   */
  public void setUnidadeGeradora(UnidadeVO unidadeGeradora) {
    this.unidadeGeradora = unidadeGeradora;
  }

  /**
   * @return the dataMovimentosList
   */
  public List<MovimentoDiarioRemessaCDTO> getDataMovimentosList() {
    return dataMovimentosList;
  }

  /**
   * @param dataMovimentosList the dataMovimentosList to set
   */
  public void setDataMovimentosList(List<MovimentoDiarioRemessaCDTO> dataMovimentosList) {
    this.dataMovimentosList = dataMovimentosList;
  }

  public boolean exibeBotaoExcluir(RemessaMovimentoDiarioVO remessaDocumento) {
    if(remessaDocumento.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO) || 
       remessaDocumento.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
      return false;
    }else {
    return true;
      }
  }
 
  public boolean exibeBotaoAlterarRemessa() {
    List<RemessaMovimentoDiarioVO> listMovimentoDiario = this.remessa.getMovimentosDiarioList();
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listMovimentoDiario) {
      if(remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA) || 
          remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
        return true;
      }
    }
    return false;
  }
}
