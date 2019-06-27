package br.gov.caixa.gitecsa.sired.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.exporter.ExportarModeloGrupoCampoRemessa;
import br.gov.caixa.gitecsa.sired.service.DocumentoService;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class AberturaRemessaController implements Serializable {

  /** Serial. */
  private static final long serialVersionUID = -6201686189545287588L;

  @Inject
  protected FacesMensager facesMessager;

  @Inject
  protected transient Logger logger;

  @Inject
  private DocumentoService documentoService;

  @Inject
  private UnidadeService unidadeService;

  private DocumentoVO documento;
  
  private RemessaVO remessa;

  private String nomeFiltro;

  private List<DocumentoVO> lista;

  private Boolean pesquisaRealizada;
  
  private Boolean modoEdicaoRemessaAB;
  
  @Inject
  private ParametroSistemaService parametroSistemaService;
  
  @PostConstruct
  protected void init() throws AppException {
    try {
      this.modoEdicaoRemessaAB = Boolean.FALSE;
      if (JSFUtil.getSessionMapValue("modoEdicaoRemessaAB") != null && JSFUtil.getSessionMapValue("remessa") != null) {
        this.modoEdicaoRemessaAB = (Boolean) JSFUtil.getSessionMapValue("modoEdicaoRemessaAB");
        this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
        JSFUtil.setSessionMapValue("modoEdicaoRemessaAB", null);
      }
      limparCampos();
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
      facesMessager.addMessageError("MA012");
    }
  }

  private void limparCampos() {
    JSFUtil.setSessionMapValue("remessa", null);
    this.pesquisaRealizada = false;
    this.nomeFiltro = null;
    this.lista = null;
  }

  /**
   * Localiza o documento pesquisado
   */
  public String localizar() {
    this.pesquisaRealizada = Boolean.TRUE;
    try {
      if (Util.isNullOuVazio(nomeFiltro)) {
        this.pesquisaRealizada = false;
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "MA059"));
        return null;
      }
      this.lista = documentoService.obterListaDocumentosPorFiltro(nomeFiltro);
      if (lista.isEmpty()) {
        this.pesquisaRealizada = false;
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MI017"));
        return null;
      }
      return null;
    } catch (DataBaseException e) {
      this.pesquisaRealizada = false;
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "AbreRemessa", "localizar"));
      return null;
    }

  }

  public String abrirMovimentoDiario() {
    return "/paginas/remessa/aberturaMovDiario.xhtml?faces-redirect=true";  
  }

  public void abrirRemessa(DocumentoVO documento) throws IOException {
    if (this.remessa != null && modoEdicaoRemessaAB) {
      this.documento = documento;
      if (AtivoInativoEnum.INATIVO.equals(documento.getIcAtivo())) {
        showDialog("modalAvisoDocumento");
        if (!Util.isNullOuVazio(documento.getMensagem())) {
          documento.setMensagem(documento.getMensagem().replace("\n", "<br/>"));
        }
      } else if (grupoInativo(documento)) {
        showDialog("modalAvisoDocumento");
      } else if (unidadeNaoPodeSolicitarDocumento(documento)) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA041"));
      } else {
        JSFUtil.setSessionMapValue("documentoRemessa", documento);
        JSFUtil.setSessionMapValue("remessa", this.remessa);
        JSFUtil.setSessionMapValue("novoDocumentoRemessa", Boolean.TRUE);
        FacesContext.getCurrentInstance().getExternalContext().redirect("aberturaDocAB");
      }
    } else {
      this.documento = documento;
      if (AtivoInativoEnum.INATIVO.equals(documento.getIcAtivo())) {
        showDialog("modalAvisoDocumento");
        if (!Util.isNullOuVazio(documento.getMensagem())) {
          documento.setMensagem(documento.getMensagem().replace("\n", "<br/>"));
        }
      } else if (grupoInativo(documento)) {
        showDialog("modalAvisoDocumento");
      } else if (unidadeNaoPodeSolicitarDocumento(documento)) {
        facesMessager.addMessageError(MensagemUtils.obterMensagem("MA041"));
      } else {
        JSFUtil.setSessionMapValue("documentoRemessa", documento);
        JSFUtil.setSessionMapValue("remessa", null);
        JSFUtil.setSessionMapValue("origem", "abertura");
        JSFUtil.setSessionMapValue("nomeFiltro", nomeFiltro);
        JSFUtil.setSessionMapValue("mostrarTextAreaMensagem", !Util.isNull(this.documento.getMensagem()));
        FacesContext.getCurrentInstance().getExternalContext().redirect("aberturaDocAB");
      }
    }
  }

  /**
   * Verifica se a unidade de lotação do usuário é uma área-meio e se for, se a mesma possui permissão para solicitar o
   * documento.
   * @param documento
   * @return
   */
  public boolean unidadeNaoPodeSolicitarDocumento(DocumentoVO documento) {
	  return false;
//    try {
//
//      UnidadeVO unidadeLotacao = null;
//      UnidadeVO filtro = new UnidadeVO();
//      UsuarioLdap usuario = (UsuarioLdap) JSFUtil.getSessionMapValue("usuario");
//      filtro.setId(usuario.getCoUnidade().longValue());
//      unidadeLotacao = unidadeService.carregarLazyPropertiesUnidade(filtro);
//      if (!ObjectUtils.isNullOrEmpty(unidadeLotacao) && !unidadeLotacao.getTipoUnidade().getIndicadorUnidade().equals("PV")) {
//        List<DocumentoVO> documentos = unidadeService.consultaDocumentoPorUnidadeGrupo(usuario.getCoUnidade().longValue());
//
//        if (Util.isNullOuVazio(documentos)) {
//          return false;
//        } else if (documentos.contains(documento)) {
//          return false;
//        }
//      } else {
//        return false;
//      }
//    } catch (Exception e) {
//      /**
//       * Não é necessário escrever exceção no log pois o interceptor já interceptou a exceção e escreveu no log.
//       */
//      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
//    }
//
//    return true;
  }
  
  /**
   * Obtém o modelo de planilha do excel para abertura em lote tags: #modelo #campos #excel 
   */
  public StreamedContent downloadModeloExcel(final DocumentoVO documento) {
      
      try {
          
          Set<GrupoCampoVO> grupoCampos = this.documentoService.getUltimaVersaoGrupoCampoDocumento(documento);
          
          ExportarModeloGrupoCampoRemessa modelo = new ExportarModeloGrupoCampoRemessa();
          modelo.setDocumento(documento);
          modelo.setData(new ArrayList<GrupoCampoVO>(grupoCampos));
          
          int qtdLinhas = Constantes.PADRAO_QDT_LINHAS_MODELO_DOCUMENTO;
          
          try {
              ParametroSistemaVO parametro = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_QTD_LINHAS_MODELO_DOCUMENTO);
              qtdLinhas = Integer.valueOf(parametro.getVlParametroSistema());
          } catch (Exception e) {
      e.printStackTrace();
              this.logger.error(e.getMessage(), e);
          }
          
          modelo.setQtdLinhas(qtdLinhas);
          
          String caminho = System.getProperty(Constantes.CAMINHO_UPLOAD);
          String filename = "ModeloDocumento_" + documento.getNome().toUpperCase().replaceAll("[\\/\\.]", StringUtils.EMPTY);
          
          File relatorio = modelo.export(caminho + filename + ".xls");
  
          return RequestUtils.download(relatorio, relatorio.getName());
      } catch (BusinessException e) {
    e.printStackTrace();
          facesMessager.addMessageError(e.getMessage());
      } catch (FileNotFoundException e) {
    e.printStackTrace();
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MI028"));
      } catch (IOException e) {
    e.printStackTrace();
          facesMessager.addMessageError(MensagemUtils.obterMensagem("MI028"));
      } catch (AppException e) {
    e.printStackTrace();
          facesMessager.addMessageError(e.getMessage());
      }
      
      return null;
  }

  private boolean grupoInativo(DocumentoVO documento) {
    return AtivoInativoEnum.INATIVO.equals(documento.getGrupo().getSituacao());
  }

  /**
   * Mostra o componente do widgetvar especificado
   * @param widgetvar
   */
  protected void showDialog(String widgetvar) {
    RequestContext.getCurrentInstance().execute(widgetvar + ".show()");
  }

  public String getMsgPersonalidada() {
    return MensagemUtils.obterMensagem("MA037");
  }
  
  public int sortByString(Object msg1, Object msg2) {
    return ((String) msg1).compareToIgnoreCase(((String) msg2));
  }

  /**
   * Getters and Setters
   */

  /**
   * @return the documento
   */
  public DocumentoVO getDocumento() {
    return documento;
  }

  /**
   * @param documento the documento to set
   */
  public void setDocumento(DocumentoVO documento) {
    this.documento = documento;
  }

  /**
   * @return the nomeFiltro
   */
  public String getNomeFiltro() {
    return nomeFiltro;
  }

  /**
   * @param nomeFiltro the nomeFiltro to set
   */
  public void setNomeFiltro(String nomeFiltro) {
    this.nomeFiltro = nomeFiltro;
  }

  /**
   * @return the lista
   */
  public List<DocumentoVO> getLista() {
    return lista;
  }

  /**
   * @param lista the lista to set
   */
  public void setLista(List<DocumentoVO> lista) {
    this.lista = lista;
  }

  /**
   * @return the pesquisaRealizada
   */
  public Boolean getPesquisaRealizada() {
    return pesquisaRealizada;
  }

  /**
   * @param pesquisaRealizada the pesquisaRealizada to set
   */
  public void setPesquisaRealizada(Boolean pesquisaRealizada) {
    this.pesquisaRealizada = pesquisaRealizada;
  }

  /**
   * @return the modoEdicaoRemessaAB
   */
  public Boolean getModoEdicaoRemessaAB() {
    return modoEdicaoRemessaAB;
  }

  /**
   * @param modoEdicaoRemessaAB the modoEdicaoRemessaAB to set
   */
  public void setModoEdicaoRemessaAB(Boolean modoEdicaoRemessaAB) {
    this.modoEdicaoRemessaAB = modoEdicaoRemessaAB;
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


}
