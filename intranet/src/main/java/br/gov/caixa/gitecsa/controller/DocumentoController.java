package br.gov.caixa.gitecsa.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.BaseAtendimentoService;
import br.gov.caixa.gitecsa.service.BaseService;
import br.gov.caixa.gitecsa.service.DocumentoService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoAgrupamentoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentalEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;

@Named
@ViewScoped
public class DocumentoController extends BaseConsultaCRUD<DocumentoVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private DocumentoService documentoServive;
    
    @Inject
    private BaseService baseService;
    
    @Inject
    private BaseAtendimentoService baseAtendimentoService;
    
    private TipoDocumentoEnum[] listaTiposDocumento;
    
    private AtivoInativoEnum[] listaSituacaoDocumento;
    
    private TipoAgrupamentoDocumentoEnum[] listaTipoAgrupamento;
    
    private TipoDocumentalEnum[] listaTipoDocumental;

    private static String NOME_RELATORIO = "CADASTRO DE DOCUMENTOS";

    private boolean faseEditar;

    private List<DocumentoVO> listaFiltro;
    
    private BaseVO baseAtendimento = new BaseVO();
    
    private Date dtInicioAtendimento;
    
    private Boolean vinculado;
    
    private Boolean checkDataInicioAtendimento = true;

    @Override
    protected DocumentoVO newInstance() {
        return new DocumentoVO();
    }

    @PostConstruct
    protected void init() throws AppException {
        try {
            obterListaDocumentos();
            verificaFaseFormulario();
            obterListaTipos();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
    }

    private void verificaFaseFormulario() {
        if (!Util.isNullOuVazio(getInstance().getId()))
            faseEditar = true;
        else
            faseEditar = false;
    }

    private void obterListaTipos() {
        setListaTiposDocumento(TipoDocumentoEnum.values());
        setListaSituacaoDocumento(AtivoInativoEnum.values());
        setListaTipoAgrupamento(TipoAgrupamentoDocumentoEnum.values());
        setListaTipoDocumental(TipoDocumentalEnum.values());

    }
    
    private void obterListaDocumentos() {
        setLista(documentoServive.findAllEager());
        listaFiltro = getLista();
    }

    @Override
    /**
     * Limpa o filtro de pesquisa
     */
    public void limparFiltro() {
        super.limparFiltro();
        contextoController.limpar();
        contextoController.setObjectFilter(null);

    }

    /**
     * Salva o documento
     * 
     * @throws AppException
     */
    public String salvar() throws AppException {
      if (StringUtils.isNotBlank(this.getInstance().getNumeroManualNormativo()) && this.getInstance().getNumeroManualNormativo().length() != 5) {
        super.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA066"));
        return null;
      }

      this.getInstance().setNome(this.getInstance().getNome().toUpperCase());
      if (StringUtils.isNotBlank(this.getInstance().getNumeroManualNormativoVersao())) {
        String versao = String.format("%03d", Integer.parseInt(this.getInstance().getNumeroManualNormativoVersao()));   
        this.getInstance().setNumeroManualNormativoVersao(versao);
      }
      
      super.save();

      setInstanceFilter(null);
      reConsultar();

      // //REFAZ A CONSULTA
      obterListaDocumentos();
      return null;

    }

    @Override
    public void edita(DocumentoVO vo) throws AppException {
        faseEditar = true;
        super.edita(vo);

    }

    public void preExportar(Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(NOME_RELATORIO, document);
        relatorio.preExportar();
    }

    public void excluir() throws AppException {
        try {
            if (super.delete()) {
                // REFAZ A CONSULTA
                obterListaDocumentos();
                // ATUALIZA A DATATABLE PARA EXIBIR O QUE FOI CADASTRADO
                updateComponentes(DATA_TABLE_CRUD);

            }
        } catch (EJBTransactionRolledbackException etr) {
            facesMessager.error(MensagemUtils.obterMensagem("MA006"));
        }
    }

    private void limparForm() {
        setInstance(new DocumentoVO());
    }

    public void novo() {
        limparForm();
        getInstance().setTipoAgrupamento(TipoAgrupamentoDocumentoEnum.NENHUM);
        faseEditar = false;
    }
    
    public void vincular(DocumentoVO vo) throws AppException {
        faseEditar = true;
        super.edita(vo);
        
        this.vinculado = false;
        this.checkDataInicioAtendimento = true;
        this.baseAtendimento = null;
        this.dtInicioAtendimento = null;
        
        BaseAtendimentoVO baseDocumento = this.documentoServive.getBaseAtendimentoByDocumento(this.getInstance());
        if (!ObjectUtils.isNullOrEmpty(baseDocumento)) {
            this.vinculado = true;
            this.baseAtendimento = baseDocumento.getBase();
            this.dtInicioAtendimento = baseDocumento.getDtInicioAtendimento();
            this.checkDataInicioAtendimento = (ObjectUtils.isNullOrEmpty(this.dtInicioAtendimento)) ? true : false;
        }
    }
    
    public void gravarVinculacao() throws AppException {
        
        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        BaseAtendimentoVO baseDocumento = new BaseAtendimentoVO(this.getInstance());
        
        baseDocumento.setBase(this.baseAtendimento);
        baseDocumento.setCoUsuario(usuario.getNuMatricula());
        baseDocumento.setDtInicioAtendimento(this.dtInicioAtendimento);
        
        this.baseAtendimentoService.save(baseDocumento);
        
        setInstanceFilter(null);
        reConsultar();

        // //REFAZ A CONSULTA
        obterListaDocumentos();
    }
    
	public void showMensagemSucessoVinculacao() {
        this.facesMessager.info(MensagemUtils.obterMensagem("MS043", "Registro"));
    }
    
    /**
     * Retorna a descrição do Tipo do Documento
     * 
     * @param valor
     * @return String
     */
    public String getDescricaoTipoDocumento(Integer valor) {
        for (TipoDocumentoEnum tipoDocumeno : TipoDocumentoEnum.values())
            if (tipoDocumeno.getValor().equals(valor.toString()))
                return tipoDocumeno.getDescricao();

        return "";
    }
    
    public void ajustarDataInicioAtendimento() {
    	if (this.checkDataInicioAtendimento) {
    		this.dtInicioAtendimento = null;
    	}
    }

    public boolean getMensagemObrigatorio() {
        return documentoServive.isCampoMensagemObrigatoria(getInstance());
    }

    public String getMensagemValidacaoMensagemObrigatoria() {
        return documentoServive.getMensagemValidacaoMensagemObrigatoria();
    }

    @Override
    protected AbstractService<DocumentoVO> getService() {
        return documentoServive;
    }

    public TipoDocumentoEnum[] getListaTiposDocumento() {
        return listaTiposDocumento;
    }

    public void setListaTiposDocumento(TipoDocumentoEnum[] listaTiposDocumento) {
        this.listaTiposDocumento = listaTiposDocumento;
    }

    public AtivoInativoEnum[] getListaSituacaoDocumento() {
        return listaSituacaoDocumento;
    }

    public void setListaSituacaoDocumento(AtivoInativoEnum[] listaSituacaoDocumento) {
        this.listaSituacaoDocumento = listaSituacaoDocumento;
    }

    public boolean isFaseEditar() {
        return faseEditar;
    }

    public void setFaseEditar(boolean faseEditar) {
        this.faseEditar = faseEditar;
    }

    public List<DocumentoVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<DocumentoVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public TipoAgrupamentoDocumentoEnum[] getListaTipoAgrupamento() {
        return listaTipoAgrupamento;
    }

    public void setListaTipoAgrupamento(TipoAgrupamentoDocumentoEnum[] listaTipoAgrupamento) {
        this.listaTipoAgrupamento = listaTipoAgrupamento;
    }
    
    public List<BaseVO> getListBase() {
        return this.baseService.findAll();
    }

    public BaseVO getBaseAtendimento() {
        return baseAtendimento;
    }

    public void setBaseAtendimento(BaseVO baseAtendimento) {
        this.baseAtendimento = baseAtendimento;
    }

    public Date getDtInicioAtendimento() {
        return dtInicioAtendimento;
    }

    public void setDtInicioAtendimento(Date dtInicioAtendimento) {
        this.dtInicioAtendimento = dtInicioAtendimento;
    }

    public Boolean getVinculado() {
        return vinculado;
    }

    public void setVinculado(Boolean vinculado) {
        this.vinculado = vinculado;
    }

	public Boolean getCheckDataInicioAtendimento() {
		return checkDataInicioAtendimento;
	}

	public void setCheckDataInicioAtendimento(Boolean checkDataInicioAtendimento) {
		this.checkDataInicioAtendimento = checkDataInicioAtendimento;
	}

  /**
   * @return the listaTipoDocumental
   */
  public TipoDocumentalEnum[] getListaTipoDocumental() {
    return listaTipoDocumental;
  }

  /**
   * @param listaTipoDocumental the listaTipoDocumental to set
   */
  public void setListaTipoDocumental(TipoDocumentalEnum[] listaTipoDocumental) {
    this.listaTipoDocumental = listaTipoDocumental;
  }
}
