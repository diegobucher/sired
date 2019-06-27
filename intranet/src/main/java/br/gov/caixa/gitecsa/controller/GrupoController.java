package br.gov.caixa.gitecsa.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.OptimisticLockException;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.DualListModel;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsultaCRUD;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.DocumentoService;
import br.gov.caixa.gitecsa.service.GrupoService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.util.JavaScriptUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;

@Named
@ViewScoped
public class GrupoController extends BaseConsultaCRUD<GrupoVO> {

    private static final String TELA_GRUPO_CAMPO = "grupo-campo?faces-redirect=true";

    private static final String TELA_GRUPO = "grupo?faces-redirect=true";

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private GrupoService grupoService;

    @Inject
    private DocumentoService documentoService;

    private DualListModel<DocumentoVO> dualListGrupoDocumento;

    private List<GrupoVO> listaGrupo;

    private static String NOME_RELATORIO = "RELATÓRIO DE GRUPO DE DOCUMENTOS";

    private boolean faseEditar;

    private List<GrupoVO> listaFiltro;

    private List<CampoVO> listaCampos;

    private List<Integer> listaOrdem;

    private TipoSolicitacaoEnum[] listaTipoSolicitacao;

    private AtivoInativoEnum[] listaSituacaoGrupo;

    @PostConstruct
    protected void init() throws AppException {
        try {
            if ((contextoController.getObject() != null) && (contextoController.getObject() instanceof GrupoVO)) {
                setInstance((GrupoVO) contextoController.getObject());
    
                contextoController.setObject(null);
    
                obterListaCampos();
            } else {
                obterLista();
            }
    
            verificaFaseFormulario();
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
    }

    @Override
    protected GrupoVO newInstance() {
        return new GrupoVO();
    }

    private void verificaFaseFormulario() {
        if (!Util.isNullOuVazio(getInstance().getId())) {
            faseEditar = true;
        } else {
            faseEditar = false;
        }
    }

    private void obterListaCampos() throws AppException {
        listaCampos = grupoService.findTodosCampo(getInstance());
        listaOrdem = getOrdem();
    }

    private void obterLista() {
        setLista(grupoService.findAll());

        TipoSolicitacaoEnum[] tiposSolicitacao = { TipoSolicitacaoEnum.REQUISICAO, TipoSolicitacaoEnum.REMESSA };

        setListaTipoSolicitacao(tiposSolicitacao);

        setListaSituacaoGrupo(AtivoInativoEnum.values());

        listaFiltro = getLista();
    }

    @Override
    public void limparFiltro() {
        super.limparFiltro();

        contextoController.limpar();

        contextoController.setObjectFilter(null);
    }

    public void salvar() throws AppException {
        // getInstance().setDocumentos(new ArrayList<DocumentoVO>());

        getInstance().setDocumentos(new HashSet<DocumentoVO>(dualListGrupoDocumento.getTarget()));
        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        getInstance().setCodigoUsuarioAlteracao(usuario.getNuMatricula());
        getInstance().setDtUltimaAlteracao(new Date());
        
		// if (ObjectUtils.isNullOrEmpty(getInstance().getVersaoFormulario())) {
		// getInstance().setVersaoFormulario(1);
		// }

        super.save();

        for (DocumentoVO doc : getInstance().getDocumentos()) {
            doc.setGrupo(getInstance());
            documentoService.update(doc);
        }

        for (DocumentoVO doc : dualListGrupoDocumento.getSource()) {
            doc.setGrupo(null);
            documentoService.update(doc);
        }

        setInstanceFilter(null);

        reConsultar();

        obterLista();
    }

    public void iniciarDualList() {
        List<DocumentoVO> target = new ArrayList<DocumentoVO>();
        List<DocumentoVO> documentosAssociadoSomeGroup = documentoService.findAssociadoGrupo();
        List<DocumentoVO> documentos = documentoService.findAll();
        List<DocumentoVO> source = getDocumentoNaoAssociados(documentos, documentosAssociadoSomeGroup);
        dualListGrupoDocumento = new DualListModel<DocumentoVO>(source, target);
    }

    private List<DocumentoVO> getDocumentoAssociados(List<DocumentoVO> documentos, List<DocumentoVO> documentosCadastrados) {
        List<DocumentoVO> target = new ArrayList<DocumentoVO>();
        boolean encontrado = false;
        for (DocumentoVO doc : documentos) {
            encontrado = false;
            for (DocumentoVO grupoDoc : documentosCadastrados) {
                if (doc.getId().equals(grupoDoc.getId())) {
                    encontrado = true;
                    break;
                }
            }
            if (encontrado) {
                target.add(doc);
            }
        }
        return target;
    }

    private List<DocumentoVO> getDocumentoNaoAssociados(List<DocumentoVO> documentos, List<DocumentoVO> documentosAssociadoSomeGroup) {
        List<DocumentoVO> source = new ArrayList<DocumentoVO>();
        boolean encontrado = false;
        for (DocumentoVO doc : documentos) {
            encontrado = false;
            for (DocumentoVO grupoDoc : documentosAssociadoSomeGroup) {
                if (doc.getId().equals(grupoDoc.getId())) {
                    encontrado = true;
                    break;
                }
            }
            if (!encontrado) {
                source.add(doc);
            }
        }
        return source;
    }

    @Override
    public void edita(GrupoVO vo) throws AppException {
        faseEditar = true;

        DocumentoVO filter = new DocumentoVO();
        filter.setGrupo(vo);

        disableAssociacao = false;
        desabilitarPesquisarAssociacao = false;

        List<DocumentoVO> documentosCadastrados = documentoService.findByGrupo(filter);
        List<DocumentoVO> documentos = documentoService.findAll();
        List<DocumentoVO> documentosAssociadoSomeGroup = documentoService.findAssociadoGrupo();

        List<DocumentoVO> source = getDocumentoNaoAssociados(documentos, documentosAssociadoSomeGroup);
        List<DocumentoVO> target = getDocumentoAssociados(documentos, documentosCadastrados);
        dualListGrupoDocumento = new DualListModel<DocumentoVO>(source, target);

        super.edita(vo);
        JavaScriptUtils.showModal("modalCadastro");
    }

    public void preExportar(Object document) {
        RelatorioSiredUtil relatorio = new RelatorioSiredUtil(NOME_RELATORIO, document);

        relatorio.preExportar();
    }

    public void excluir() throws AppException {
        if (super.delete()) {
            obterLista();

            updateComponentes(DATA_TABLE_CRUD);
        }
    }

    private void limparForm() {
        setInstance(new GrupoVO());

        iniciarDualList();
    }

    public void novo() {
        limparForm();

        faseEditar = false;
    }

    public List<CampoVO> getListaCampos() {
        return listaCampos;
    }

    public void setListaCampos(List<CampoVO> listaCampos) {
        this.listaCampos = listaCampos;
    }

    public void ordenarDualList() {
        Collections.sort(dualListGrupoDocumento.getTarget(), new Comparator<DocumentoVO>() {
            @Override
            public int compare(DocumentoVO o1, DocumentoVO o2) {
                return o1.getNome().compareToIgnoreCase(o2.getNome());
            }
        });

        Collections.sort(dualListGrupoDocumento.getSource(), new Comparator<DocumentoVO>() {
            @Override
            public int compare(DocumentoVO o1, DocumentoVO o2) {
                return o1.getNome().compareToIgnoreCase(o2.getNome());
            }
        });
    }

    private List<Integer> getOrdem() {
        List<Integer> ordens = new ArrayList<Integer>();

        for (int i = 0; i < getListaCampos().size(); i++) {
            ordens.add(i + 1);
        }

        return ordens;
    }

    public List<Integer> getOrdem(CampoVO campoAtual) {
        List<Integer> ordens = new ArrayList<Integer>();

        int count = 0;

        for (int i = 0; i < getListaCampos().size(); i++) {
            CampoVO campo = getListaCampos().get(i);
            if (campo.isCheckBox()) {
                ordens.add(++count);
            }
        }

        for (int i = 0; i < getListaCampos().size(); i++) {
            CampoVO campo = getListaCampos().get(i);
            if (campo.getOrdem() != null && (campoAtual.getOrdem() == null || campoAtual.getOrdem() != campo.getOrdem())) {
                ordens.remove(campo.getOrdem());
            }
        }

        return ordens;
    }

    public String vincularCampo(GrupoVO grupo) {
        contextoController.setObject(grupo);
        return TELA_GRUPO_CAMPO;
    }

    public String voltar() {
        return TELA_GRUPO;
    }

    public String salvarCampos() {
        MensagemUtils.setKeepMessages(true);
        try {
            grupoService.salvarCamposVinculados(getInstance(), listaCampos);
            facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS043", "Formulário"));
        } catch (OptimisticLockException e) {
            // FIXME: #294 Necessário criar msg no bundle
            facesMessager.addMessageError("Um outro usuário realizou uma alteração neste formulário. Favor recomeçar a operação.");
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "salvarCampos", "GrupoCampo"));
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
            logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "salvarCampos", "GrupoCampo"));
        }
        
        return TELA_GRUPO;
    }

    public void limparCampos() {
        for (CampoVO campo : listaCampos) {
            campo.setCheckBox(false);
            campo.setLegenda(null);
            campo.setLegenda(null);
            campo.setMensagem(null);
            campo.setObrigatorio(false);
            campo.setOrdem(null);
        }
    }

    public void limparCampo(CampoVO campo) {
        if (campo.isCheckBox()) {
            campo.setOrdem(null);
            campo.setMensagem(null);
            campo.setLegenda(null);
            campo.setObrigatorio(true);
        }
    }

    @Override
    protected AbstractService<GrupoVO> getService() {
        return grupoService;
    }

    public boolean isFaseEditar() {
        return faseEditar;
    }

    public void setFaseEditar(boolean faseEditar) {
        this.faseEditar = faseEditar;
    }

    public List<GrupoVO> getListaFiltro() {
        return listaFiltro;
    }

    public void setListaFiltro(List<GrupoVO> listaFiltro) {
        this.listaFiltro = listaFiltro;
    }

    public List<GrupoVO> getListaGrupo() {
        return listaGrupo;
    }

    public void setListaGrupo(List<GrupoVO> listaGrupo) {
        this.listaGrupo = listaGrupo;
    }

    public DualListModel<DocumentoVO> getDualListGrupoDocumento() {
        return dualListGrupoDocumento;
    }

    public void setDualListGrupoDocumento(DualListModel<DocumentoVO> dualListGrupoDocumento) {
        this.dualListGrupoDocumento = dualListGrupoDocumento;
    }

    public TipoSolicitacaoEnum[] getListaTipoSolicitacao() {
        return listaTipoSolicitacao;
    }

    public void setListaTipoSolicitacao(TipoSolicitacaoEnum[] listaTipoSolicitacao) {
        this.listaTipoSolicitacao = listaTipoSolicitacao;
    }

    public List<Integer> getListaOrdem() {
        return listaOrdem;
    }

    public void setListaOrdem(List<Integer> listaOrdem) {
        this.listaOrdem = listaOrdem;
    }

    public AtivoInativoEnum[] getListaSituacaoGrupo() {
        return AtivoInativoEnum.values();
    }

    public void setListaSituacaoGrupo(AtivoInativoEnum[] listaSituacaoGrupo) {
        this.listaSituacaoGrupo = listaSituacaoGrupo;
    }

}
