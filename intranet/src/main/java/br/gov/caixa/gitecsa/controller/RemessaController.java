package br.gov.caixa.gitecsa.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseViewCrud;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.service.GrupoService;
import br.gov.caixa.gitecsa.service.OperacaoService;
import br.gov.caixa.gitecsa.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.service.RemessaService;
import br.gov.caixa.gitecsa.service.TramiteRemessaService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentalEnum;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.FeriadoService;
import br.gov.caixa.gitecsa.sired.service.SequencialRemessaService;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class RemessaController extends BaseViewCrud<RemessaDocumentoVO> {

    private static final long serialVersionUID = -1153421337230811748L;

    @Inject
    private RemessaDocumentoService remessaDocumentoService;

    @Inject
    private RemessaService remessaService;

    @Inject
    private GrupoService formularioRequisicaoService;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private OperacaoService operacaoService;

    @Inject
    private TramiteRemessaService tramiteRemessaService;
    
    @Inject
    private FeriadoService feriadoService;

    @Inject
    private SequencialRemessaService sequencialRemessaService;

    private RemessaVO remessa;

    private DocumentoVO documento;

    private List<OperacaoVO> operacoes;

    private List<UnidadeVO> listaUnidades;

    private boolean detalharRequisicao;

    private GrupoVO grupo;

    private String numeroProcesso;

    private String numeroOcorrencia;

    private boolean telaAbertura;

    private List<UnidadeVO> listaUnidadesPorPerfil;

    private String codigoUnidadeFiltro;

    private String nomeUnidadeFiltro;

    private boolean redirecionarCancelar;

    private String anoFragmentacao;

    @Override
    protected RemessaDocumentoVO newInstance() {
        return new RemessaDocumentoVO();
    }

    @Override
    protected AbstractService<RemessaDocumentoVO> getService() {
        return remessaDocumentoService;
    }

    @PostConstruct
    protected void init() throws AppException, SQLException {

        try {

            if (JSFUtil.getSessionMapValue("remessa") != null) {
                this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
                // JsfUtil.setSessionMapValue("remessa", null);
                this.documento = remessa.getDocumento();
            } else if (JSFUtil.getSessionMapValue("documentoRemessa") != null) {
                this.documento = (DocumentoVO) JSFUtil.getSessionMapValue("documentoRemessa");
                JSFUtil.setSessionMapValue("documentoRemessa", null);
            }
            if (Util.isNullOuVazio(JSFUtil.getSessionMapValue("remessaDocumento"))) {
                cadastrarItemRemessa(getDocumento());
                this.definirUnidadeGeradoraPadrao();
                // Long ano = (Long) JsfUtil.getSessionMapValue("anoFragmentacao");
                // anoFragmentacao = ano.toString();
            } else {
                // Edição de remessa documento - chamar metodo de edição
                RemessaDocumentoVO remessaDoc = (RemessaDocumentoVO) JSFUtil.getSessionMapValue("remessaDocumento");
                JSFUtil.setSessionMapValue("remessaDocumento", null);
                setInstance(new RemessaDocumentoVO());
                setInstance(remessaDoc);
                editarRemessaDocumento();
                /*
                 * if (!Util.isNullOuVazio(remessaDoc.getDataFragmentacao())) { Calendar data = Calendar.getInstance();
                 * data.setTime(remessaDoc.getD ); Integer ano = data.get(Calendar.YEAR); anoFragmentacao = ano.toString(); }
                 */
            }

            if (!Util.isNullOuVazio(JSFUtil.getSessionMapValue("origem")) && JSFUtil.getSessionMapValue("origem").toString().equalsIgnoreCase("abertura")) {
                redirecionarCancelar = true;
            } else if (!Util.isNullOuVazio(JSFUtil.getSessionMapValue("origem"))
                    && JSFUtil.getSessionMapValue("origem").toString().equalsIgnoreCase("manutencao")) {
                redirecionarCancelar = true;
            } else {
                redirecionarCancelar = false;
            }
        } catch (Exception e) {
            this.logger.error(e.getMessage(), e);
            this.facesMessager.addMessageError("MA012");
        }
    }

    public void pesquisarUnidadeGeradora() {
        if (!Util.isNullOuVazio(codigoUnidadeFiltro)) {

            try {
                UnidadeVO unidadeFiltro = new UnidadeVO();
                unidadeFiltro.setId(Long.parseLong(codigoUnidadeFiltro));
                List<UnidadeVO> unidades = unidadeService.findByParameters(unidadeFiltro);

                if (!Util.isNullOuVazio(unidades)) {
                    getInstance().setUnidadeGeradora(unidades.get(0));
                    nomeUnidadeFiltro = getInstance().getUnidadeGeradora().getNome().trim();
                } else {
                    facesMessager.addMessageError(MensagemUtils.obterMensagem("MA020"));
                }

            } catch (Exception e) {
                facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
                logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "RemessaController", "pesquisarUnidadeGeradora"));
            }
        } else {
            getInstance().setUnidadeGeradora(null);
            nomeUnidadeFiltro = "";
        }
    }

    public UnidadeVO consultarUnidadeLotacao() throws AppException {
        UnidadeVO unidadeLotacao = null;
        UnidadeVO filtro = new UnidadeVO();
        UsuarioLdap usuario = (UsuarioLdap) JSFUtil.getSessionMapValue("usuario");
        filtro.setId(usuario.getCoUnidade().longValue());
        unidadeLotacao = unidadeService.carregarLazyPropertiesUnidade(filtro);
        if (Util.isNullOuVazio(unidadeLotacao)) {
            throw new AppException();
        }
        return unidadeLotacao;
    }

    /**
     * Define a unidade geradora (unidade filtro e instância) como a unidade do usuário logado. Sempre que for necessário resetar a unidade,
     * deve-se chamar este método.
     */
    public void definirUnidadeGeradoraPadrao() {
        // TODO: Colocar este método em um service ou em uma classe utilitária
        try {

            UnidadeVO unidade = consultarUnidadeLotacao();

            this.codigoUnidadeFiltro = unidade.getId().toString();
            this.nomeUnidadeFiltro = unidade.getNome();

            this.getInstance().setUnidadeGeradora(unidade);

        } catch (AppException e) {
            facesMessager.addMessageError(e.getMessage());
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), "RequisicaoController", "definirUnidadeGeradoraPadrao"));
        }
    }

    public void cadastrarItemRemessa(DocumentoVO documentoVO) throws AppException {
        try {
            setInstance(new RemessaDocumentoVO());
            documento = documentoVO;
            JSFUtil.setSessionMapValue("mostrarTextAreaMensagem", !Util.isNull(this.getDocumento().getMensagem()));
            if (documento.getIcAtivo().equals(AtivoInativoEnum.INATIVO)) {
                showDialog("modalAvisoDocumento");
                documento.setMensagem(documento.getMensagem().replace("\n", "<br/>"));
            } else {
                if (remessa == null) {
                    telaAbertura = true;
                    remessa = new RemessaVO();
                    remessa.setDocumento(documentoVO);
                    remessa.setRemessaDocumentos(new ArrayList<RemessaDocumentoVO>());
                }

                getInstance().setRemessa(remessa);

//                if (remessa.getRemessaDocumentos().size() == 0) {
//                    getInstance().setNuItem(1);
//                } else {
//                    getInstance().setNuItem(calculaProximoItemRemessa(remessa));
//                }

                if (this.getInstance().getUnidadeGeradora() == null) {
                    this.definirUnidadeGeradoraPadrao();
                }

                grupo = new GrupoVO();
                grupo = formularioRequisicaoService.obterGrupo(documento);

                if (Util.isNullOuVazio(operacoes)) {
                    operacoes = operacaoService.findAll();
                }
            }
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
        }
    }

    /**
     * Calcula o valor do próximo item da requisição a ser incluído.
     * 
     * @param remessaVO
     * @return
     */
    public Integer calculaProximoItemRemessa(RemessaVO remessaVO) {

        Integer nuItem = 1;
//        for (RemessaDocumentoVO remDoc : remessaVO.getRemessaDocumentos()) {
//            if (remDoc.getNuItem() > nuItem) {
//                nuItem = remDoc.getNuItem();
//            }
//        }
        nuItem++;
        return nuItem;
    }

    public void editarRemessaDocumento() throws AppException {

        documento = getInstance().getRemessa().getDocumento();
        codigoUnidadeFiltro = getInstance().getUnidadeGeradora().getId().toString();
        nomeUnidadeFiltro = getInstance().getUnidadeGeradora().getNome().trim();

        grupo = formularioRequisicaoService.obterGrupo(documento);

        if (grupo != null) {
            this.recuperarCamposDinamicos(getInstance());
        }

        try {
            if (Util.isNullOuVazio(operacoes)) {
                operacoes = operacaoService.findAll();
            }

            JSFUtil.setSessionMapValue("mostrarTextAreaMensagem", !Util.isNull(this.getDocumento().getMensagem()));
        } catch (Exception e) {
            facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
        }
    }

    public void gravar() {
        try {
            boolean inclusao = false;
            this.validarCamposObrigatorios(getInstance());
            this.salvarCamposDinamicos(getInstance());

            String msgSucesso = "MS025";
            if (getInstance().getId() == null) {
                msgSucesso = "MS013";
                inclusao = true;
            }

            this.remessa.setDocumento(getDocumento());
            this.remessa.setUnidadeSolicitante(consultarUnidadeLotacao());

            // Caso a remessa esteja vindo da sessão
            if (remessa.getId() == null) {
                // Antes de salvar a remessa verifica se o documento já foi
                // cadastrado para a mesma.
                // Valida os dados da remessa de documento antes de salvar a
                // requisição.
                remessaDocumentoService.validaDados(getInstance());
                remessa.setCodigoUsuarioAbertura(JSFUtil.getUsuario().getNuMatricula());
                remessa.setDataHoraAbertura(new Date());
                remessaService.save(this.remessa);

                TramiteRemessaVO tramiteAtual = tramiteRemessaService.salvarTramiteRemessaRascunho(this.remessa);
                remessa.setTramiteRemessaAtual(tramiteAtual);
                remessaService.update(this.remessa);
                msgSucesso = "MS014";
            }

            getInstance().setRemessa(this.remessa);
            
//            RemessaDocumentoVO remessaComplemento = remessaDocumentoService.obterRemessaDocumentoComplemento(getInstance());
//            if(remessaComplemento != null){
//              getInstance().setIcComplemento(SimNaoEnum.SIM);
//            } else{
//              getInstance().setIcComplemento(SimNaoEnum.NAO);
//            }

            /*
             * IMPORTANTE: Atualiza a data de fragmentação do item. Isso é necessário pois se o usuário apagar o campo Data Fim, o sistema
             * não consegue capturar o evento na tela para atualizar a data de fragmentação. O sistema só consegue capturar o evento do
             * usuário selecionando uma data.
             */
            // atualizarDataFragmentacao();

            getInstance().setCodigoUsuarioUltimaAlteracao(JSFUtil.getUsuario().getNuMatricula());
            this.validarDataGeracao(getInstance());
            
            //TODO - Adicionar o numero de documento....
            if (this.getInstance().getDocumento() == null) {
              this.getInstance().setDocumento(this.documento);
            }

            //TODO - Adicionar a geração do numero de remessa....
            if (TipoDocumentalEnum.REMESSA_MOVIMENTO_DIARIO_TIPO_C.equals(this.getInstance().getDocumento().getTipoDocumental())) {              
              if (this.getInstance().getCodigoRemessa() == null) {
                this.getInstance().setCodigoRemessa(sequencialRemessaService.generate(this.getInstance().getUnidadeGeradora()));
              }
            }
            
            remessaDocumentoService.saveOrUpdate(getInstance());

            if (Util.isNullOuVazio(remessa.getRemessaDocumentos())) {
                this.remessa.setRemessaDocumentos(new ArrayList<RemessaDocumentoVO>());
            }
            if (inclusao) {
                this.remessa.getRemessaDocumentos().add(getInstance());
            }

            // Adicionando requisação a sessão
            JSFUtil.setSessionMapValue("remessa", this.remessa);
            JSFUtil.addSuccessMessage(MensagemUtils.obterMensagem(msgSucesso));
//            if(remessaComplemento != null){
//              JSFUtil.addSuccessMessage(MensagemUtils.obterMensagem("MI036", remessaComplemento.getNuItem(), remessaComplemento.getRemessa().getId()));
//            }
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            JSFUtil.setSessionMapValue("nomeFiltro", null);
            FacesContext.getCurrentInstance().getExternalContext().redirect("edita-remessa");

            // desativando o redirecionamento pelo metodo cancelar que ocorre ao
            // fechar o modal
            redirecionarCancelar = false;

            updateComponentes("formRemessa");

            this.definirUnidadeGeradoraPadrao();

        } catch (BusinessException e) {
            for (String message : e.getErroList()) {
                facesMessager.addMessageError(message);
            }
        } catch (RequiredException e) {
            for (String message : e.getErroList()) {
                facesMessager.addMessageError(message);
            }
        } catch (AppException e) {
            facesMessager.addMessageError(e.getMessage());
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), "RemessaController", "gravar"));
        } catch (IOException e) {
            facesMessager.addMessageError(e.getMessage());
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), "RemessaController", "gravar"));
        }
    }
    
    private void validarDataGeracao(RemessaDocumentoVO remessaDocumento) throws BusinessException, DataBaseException {
        if (!ObjectUtils.isNullOrEmpty(remessaDocumento.getDataGeracao()) && 
        		!this.feriadoService.isDataUtil(remessaDocumento.getDataGeracao(), remessaDocumento.getUnidadeGeradora())) {
            // FIXME: #OS_278 - Pendente: Incluir mensagem 
            List<String> error = new ArrayList<String>();
            error.add("Data de Geração inválida. A data deve ser um dia útil.");
        	throw new BusinessException(error);
        }
    }

    public void calcularDataFragmentacao(GrupoCampoVO campo) {

        /*
         * if (campo.getCampo().getNome().equals(TipoCampoEnum.DT_FIM.toString())) { if (campo.getValorData() != null) { Calendar
         * dataFragmentacao = Calendar.getInstance(); dataFragmentacao.setTime(campo.getValorData()); int prazo =
         * remessa.getDocumento().getPrazoFragmentacao().intValue(); dataFragmentacao.set(Calendar.YEAR, dataFragmentacao.get(Calendar.YEAR)
         * + prazo); getInstance().setDataFragmentacao(dataFragmentacao.getTime()); } else { getInstance().setDataFragmentacao(null); } }
         */
    }

    public void atualizarDataFragmentacao() {
        /*
         * if (getInstance().getDataFim() != null && remessa.getDocumento().getPrazoFragmentacao() != null) { Calendar dataFragmentacao =
         * Calendar.getInstance(); dataFragmentacao.setTime(getInstance().getDataFim()); int prazo =
         * remessa.getDocumento().getPrazoFragmentacao().intValue(); dataFragmentacao.set(Calendar.YEAR, dataFragmentacao.get(Calendar.YEAR)
         * + prazo); getInstance().setDataFragmentacao(dataFragmentacao.getTime()); } else { getInstance().setDataFragmentacao(null); }
         */
    }

    public boolean isContemMascara(String mascara) {
        return (mascara.contains(".") || mascara.contains("/") || mascara.contains("-")) && !mascara.contains("0,00");
    }

    public boolean isMascaraNumerica(String mascara) {
        if (isContemMascara(mascara))
            return false;

        return mascara.matches("^[0-9]{1,}$");
    }

    public boolean isMascaraMoeda(String mascara) {
        return !isContemMascara(mascara) && mascara.contains("0,00");
    }

    public void validarCamposObrigatorios(RemessaDocumentoVO vo) throws RequiredException {
        List<String> camposObrigatorios = new ArrayList<String>();

        if (Util.isNullOuVazio(vo.getUnidadeGeradora())) {
            camposObrigatorios.add(MensagemUtils.obterMensagem("MA001", "geral.label.unidadeGeradora"));
        }

        // Campos dinâmicos
        if (!ObjectUtils.isNullOrEmpty(grupo)) {
            for (GrupoCampoVO grupoCampo : grupo.getGrupoCampos()) {
                if (grupoCampo.getCampoObrigatorio().equals(SimNaoEnum.SIM)) {
                    if (!grupoCampo.getCampo().getNome().equals("NU_OPERACAO_A11") && ObjectUtils.isNullOrEmpty(grupoCampo.getValor())
                            && ObjectUtils.isNullOrEmpty(grupoCampo.getValorData())) {
                        camposObrigatorios.add(MensagemUtils.obterMensagem("MA001",
                                StringUtils.defaultIfEmpty(grupoCampo.getLegenda(), grupoCampo.getCampo().getDescricao())));
                    }
                }
            }
        }

        // if (!Util.isNull(grupo)) {
        // for (GrupoCampoVO elm : grupo.getGrupoCampos()) {
        // for (TipoCampoEnum campo : Arrays.asList(TipoCampoEnum.values())) {
        // if (elm.getCampo().getNome().equals(campo.toString())) {
        // if (elm.getCampoObrigatorio().equals(SimNaoEnum.SIM) &&
        // Util.isNull(elm.getValor())) {
        //
        // if (!campo.equals(TipoCampoEnum.DT_INICIO) &&
        // !campo.equals(TipoCampoEnum.DT_FIM)) {
        // if (Util.isNull(elm.getLegenda())) {
        // camposObrigatorios.add(MensagemUtil.obterMensagem("MA001",
        // elm.getCampo().getDescricao()));
        // } else {
        // camposObrigatorios.add(MensagemUtil.obterMensagem("MA001",
        // elm.getLegenda()));
        // }
        // }
        //
        // if (campo.equals(TipoCampoEnum.DT_INICIO) ||
        // campo.equals(TipoCampoEnum.DT_FIM)) {
        // if (Util.isNull(elm.getValorData())) {
        // if (Util.isNull(elm.getLegenda())) {
        // camposObrigatorios.add(MensagemUtil.obterMensagem("MA001",
        // elm.getCampo().getDescricao()));
        // } else {
        // camposObrigatorios.add(MensagemUtil.obterMensagem("MA001",
        // elm.getLegenda()));
        // }
        // }
        // }
        // }
        // }
        // }
        // }
        // }

        if (!camposObrigatorios.isEmpty()) {
            throw new RequiredException(camposObrigatorios);
        }
    }

    private void salvarCamposDinamicos(RemessaDocumentoVO vo) throws AppException {
        if (grupo == null)
            return;

        try {
            GrupoCamposHelper.setValorCamposDinamicos(vo, grupo.getGrupoCampos());
        } catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
            throw new AppException(e.getMessage(), e);
        }
    }

    private void recuperarCamposDinamicos(RemessaDocumentoVO vo) throws AppException {
        if (grupo == null) {
            return;
        }

        try {
            GrupoCamposHelper.getValorCamposDinamicos(vo, grupo.getGrupoCampos());
        } catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
            throw new AppException(e.getMessage(), e);
        }
    }

    @Override
    public void limparForm() {

        getInstance().setUnidadeGeradora(null);
//        getInstance().setIcLoterico(SimNaoEnum.NAO);
//        getInstance().setIcMovimentoParcial(SimNaoEnum.NAO);

        if (!Util.isNull(this.grupo)) {
            for (GrupoCampoVO item : this.grupo.getGrupoCampos()) {
                item.setValor(null);
                item.setValorData(null);
            }
        }

        this.definirUnidadeGeradoraPadrao();
    }

    public void cancelar() throws AppException {
        if (this.redirecionarCancelar) {
            try {
                if (!Util.isNullOuVazio(getInstance().getRemessa()) && !Util.isNullOuVazio(getInstance().getRemessa().getId())) {
                    FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                    FacesContext.getCurrentInstance().getExternalContext().redirect("edita-remessa");
                } else {
                    FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
                    FacesContext.getCurrentInstance().getExternalContext().redirect("abertura");
                }

            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
    }

    public void limpar() {

    }

    public String calculaTamanhoCampo(String legenda, String descricao, int tamanhoCampo, SimNaoEnum obrigatorio) {
        int tamanhoLabel = (!Util.isNullOuVazio(legenda) ? legenda.length() : descricao.length());
        if (tamanhoLabel < tamanhoCampo) {
            return "width: " + (tamanhoCampo * 10) + "px";
        } else {
            return "width: " + (tamanhoLabel * 6) + "px";
        }
    }

    public List<GrupoCampoVO> getListGrupoCampo() {
        if (!ObjectUtils.isNullOrEmpty(grupo) && !ObjectUtils.isNullOrEmpty(grupo.getGrupoCampos())) {
            return CollectionUtils.asSortedList(grupo.getGrupoCampos());
        }

        return new ArrayList<GrupoCampoVO>();
    }

    // MÉT0D0S DE ACESSO
    public DocumentoVO getDocumento() {
        return documento;
    }

    public RemessaVO getRemessa() {
        return remessa;
    }

    public void setRemessa(RemessaVO remessa) {
        this.remessa = remessa;
    }

    public void setDocumento(DocumentoVO documento) {
        this.documento = documento;
    }

    public List<UnidadeVO> getListaUnidades() {
        return listaUnidades;
    }

    public void setListaUnidades(List<UnidadeVO> listaUnidades) {
        this.listaUnidades = listaUnidades;
    }

    public GrupoVO getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoVO grupo) {
        this.grupo = grupo;
    }

    public String getNumeroProcesso() {
        return numeroProcesso;
    }

    public void setNumeroProcesso(String numeroProcesso) {
        this.numeroProcesso = numeroProcesso;
    }

    public String getNumeroOcorrencia() {
        return numeroOcorrencia;
    }

    public void setNumeroOcorrencia(String numeroOcorrencia) {
        this.numeroOcorrencia = numeroOcorrencia;
    }

    public List<OperacaoVO> getOperacoes() {
        return operacoes;
    }

    public void setOperacoes(List<OperacaoVO> operacoes) {
        this.operacoes = operacoes;
    }

    public boolean isTelaAbertura() {
        return telaAbertura;
    }

    public boolean isDetalharRequisicao() {
        return detalharRequisicao;
    }

    public String getCodigoUnidadeFiltro() {
        return codigoUnidadeFiltro;
    }

    public void setCodigoUnidadeFiltro(String codigoUnidadeFiltro) {
        this.codigoUnidadeFiltro = codigoUnidadeFiltro;
    }

    public String getNomeUnidadeFiltro() {
        return nomeUnidadeFiltro;
    }

    public void setNomeUnidadeFiltro(String nomeUnidadeFiltro) {
        this.nomeUnidadeFiltro = nomeUnidadeFiltro;
    }

    public List<UnidadeVO> getListaUnidadesPorPerfil() {
        return listaUnidadesPorPerfil;
    }

    public void setListaUnidadesPorPerfil(List<UnidadeVO> listaUnidadesPorPerfil) {
        this.listaUnidadesPorPerfil = listaUnidadesPorPerfil;
    }

    public String getAnoFragmentacao() {
        return anoFragmentacao;
    }

    public void setAnoFragmentacao(String anoFragmentacao) {
        this.anoFragmentacao = anoFragmentacao;
    }

    public SimNaoEnum[] getListSimNao() {
        return SimNaoEnum.values();
    }

}
