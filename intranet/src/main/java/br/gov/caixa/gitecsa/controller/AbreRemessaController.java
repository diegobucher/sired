package br.gov.caixa.gitecsa.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.DocumentoService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class AbreRemessaController extends BaseConsultaCRUD<DocumentoVO> {
  
    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private DocumentoService service;

    private DocumentoVO documento;

    private String nomeFiltro;

    private List<DocumentoVO> lista;

    private Boolean pesquisaRealizada;

    @Inject
    private UnidadeService unidadeService;

    @Override
    protected DocumentoVO newInstance() {
        return new DocumentoVO();
    }

    @Override
    protected AbstractService<DocumentoVO> getService() {
        return service;
    }

    @PostConstruct
    protected void init() throws AppException {
        try {
            JSFUtil.setSessionMapValue("remessa", null);
            pesquisaRealizada = false;
            nomeFiltro = null;
            lista = null;
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
    }

    @Override
    public void limparFiltro() {
        super.limparFiltro();
        contextoController.limpar();
        contextoController.setObjectFilter(null);
    }

    /**
     * Localiza o documento pesquisado
     */
    public void localizar() {

        if (!Util.isNullOuVazio(nomeFiltro)) {
            pesquisaRealizada = true;
            DocumentoVO filtro = new DocumentoVO();
            GrupoVO grupo = new GrupoVO();
            grupo.setTipoSolicitacao(TipoSolicitacaoEnum.REMESSA);
            filtro.setGrupo(grupo);
            filtro.setNome(nomeFiltro.toUpperCase());

            try {
                lista = service.findByParameters(filtro);

                List<DocumentoVO> documentoVOs = new ArrayList<DocumentoVO>();

                for (DocumentoVO documento : lista) {
                    if (!documentoVOs.contains(documento)) {
                        documentoVOs.add(documento);
                    }
                }

                lista = documentoVOs;

                // Caso não retorne conteudo
                if (lista.size() == 0) {
                    this.pesquisaRealizada = false;
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MI017"));
                }

                limparFiltro();
            } catch (AppException e) {
                this.pesquisaRealizada = false;
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "AbreRemessa", "localizar"));
            }
        } else {
            this.pesquisaRealizada = false;
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "MA059"));
        }
    }

    public void localizarDocumento(String nomeFiltro) {
        this.nomeFiltro = nomeFiltro;
        this.localizar();
    }

    /**
     * Verifica se a unidade de lotação do usuário é uma área-meio e se for, se a mesma possui permissão para solicitar o documento.
     * 
     * @param documento
     * @return
     */
    public boolean unidadeNaoPodeSolicitarDocumento(DocumentoVO documento) {
    	return false;
//        try {
//
//            UnidadeVO unidadeLotacao = null;
//            UnidadeVO filtro = new UnidadeVO();
//            UsuarioLdap usuario = (UsuarioLdap) JSFUtil.getSessionMapValue("usuario");
//            filtro.setId(usuario.getCoUnidade().longValue());
//            unidadeLotacao = unidadeService.carregarLazyPropertiesUnidade(filtro);
//            if (!ObjectUtils.isNullOrEmpty(unidadeLotacao) && !unidadeLotacao.getTipoUnidade().getIndicadorUnidade().equals("PV")) {
//                List<DocumentoVO> documentos = unidadeService.consultaDocumentoPorUnidadeGrupo(usuario.getCoUnidade().longValue());
//
//                if (Util.isNullOuVazio(documentos)) {
//                    return false;
//                } else if (documentos.contains(documento)) {
//                    return false;
//                }
//            } else {
//                return false;
//            }
//        } catch (Exception e) {
//            /**
//             * Não é necessário escrever exceção no log pois o interceptor já interceptou a exceção e escreveu no log.
//             */
//            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
//        }
//
//        return true;
    }

    private boolean grupoInativo(DocumentoVO documento) {
        return AtivoInativoEnum.INATIVO.equals(documento.getGrupo().getSituacao());
    }

    public void abrirRemessa(DocumentoVO documento) throws IOException {
        setDocumento(documento);
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
            JSFUtil.setSessionMapValue("mostrarTextAreaMensagem", !Util.isNull(this.getDocumento().getMensagem()));
            FacesContext.getCurrentInstance().getExternalContext().redirect("abertura2");
        }
    }

    public void abrirMovimentoDiario() {
        try {
            pesquisaRealizada = true;
            lista = service.buscarRemessaMovimentoDiario();
            if (!lista.isEmpty()) {
                abrirRemessa(lista.get(0));
            }
        } catch (IOException e) {

            this.pesquisaRealizada = false;
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "AbreRemessa", "abrirMovimentoDiario"));
        }
    }

    public void setService(DocumentoService service) {
        this.service = service;
    }

    public String getNomeFiltro() {
        return nomeFiltro;
    }

    public void setNomeFiltro(String nomeFiltro) {
        this.nomeFiltro = nomeFiltro;
    }

    public List<DocumentoVO> getLista() {
        return lista;
    }

    public void setLista(List<DocumentoVO> lista) {
        this.lista = lista;
    }

    public Boolean getPesquisaRealizada() {
        return pesquisaRealizada;
    }

    public void setPesquisaRealizada(Boolean pesquisaRealizada) {
        this.pesquisaRealizada = pesquisaRealizada;
    }

    public DocumentoVO getDocumento() {
        return documento;
    }

    public String getMsgPersonalidada() {
        return MensagemUtils.obterMensagem("MA037");
    }

    public void setDocumento(DocumentoVO documento) {
        this.documento = documento;
    }

}
