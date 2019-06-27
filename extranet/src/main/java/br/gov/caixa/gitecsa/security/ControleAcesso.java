package br.gov.caixa.gitecsa.security;

import java.io.Serializable;
import java.util.Calendar;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import br.com.caixa.sicdu.core.ldap.AtributoType;
import br.com.caixa.sicdu.core.ldap.node.UsuarioType;
import br.com.caixa.sicdu.core.ldapsearch.LdapSearch;
import br.gov.caixa.gitecsa.arquitetura.controller.ContextoController;
import br.gov.caixa.gitecsa.ldap.usuario.Credentials;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.security.dto.MenuItemVertical;
import br.gov.caixa.gitecsa.service.EmpresaContratoService;
import br.gov.caixa.gitecsa.service.FuncionalidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.extra.service.EmpresaService;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;
import br.gov.caixa.gitecsa.sired.vo.FuncionalidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

/**
 * Classe de controle de acesso ao LDAP
 * 
 * @author rmotal
 * 
 */
@Named
@SessionScoped
public class ControleAcesso implements Serializable {

    private static final long serialVersionUID = 7444956049460916877L;

    private UsuarioLdap usuario = new UsuarioLdap();

    @Inject
    private static Logger logger = Logger.getLogger(ControleAcesso.class);

    @Inject
    private FuncionalidadeService serviceFuncionalidade;

    @Inject
    private ContextoController contextoController;

    @Inject
    private EmpresaService empresaService;

    @Inject
    private EmpresaContratoService empresaContratoService;
    
	@Inject
	private ParametroSistemaService parametroSistemaService;

    private static final String MENU_ITEM = "menuItem";

    private static final Integer TIPO_ACESSO = 1;

    private static final String PAGINA_INICIAL = "requisicao/consulta";

    private Credentials credentials;

    private MenuModel menuBarraHorizontal = new DefaultMenuModel();

    private String linkMenu;

    private Integer perfilConsulta;

    private List<MenuItemVertical> menuVerticalSistema;

    private String nomeMenu;

    /**
     * Método obtém a data corrente com horas e minutos
     */
    private void getDiaDaSemana() {
        JsfUtil.setSessionMapValue("dataDiaExtenso", Util.formatData(Calendar.getInstance().getTime(), "EEEE, dd/MM/yyyy HH:mm"));
    }

    public ControleAcesso() {
        credentials = new Credentials();
        getDiaDaSemana();
    }

    /**
     * Método valida os campos informados pelo usuário
     */
    public boolean validarCampos() {

        if (Util.isNullOuVazio(credentials) || Util.isNullOuVazio(credentials.getLogin()) || Util.isNullOuVazio(credentials.getSenha())) {

            if (Util.isNullOuVazio(credentials.getLogin()) || "_______".equals(credentials.getLogin())) {
                JsfUtil.addErrorMessage(MensagemUtils.obterMensagem("MA001", MensagemUtils.obterMensagem("login.label.usuario")));
            }
            if (Util.isNullOuVazio(credentials.getSenha())) {
                JsfUtil.addErrorMessage(MensagemUtils.obterMensagem("MA001", MensagemUtils.obterMensagem("login.label.senha")));
            }

            if (!Util.isNullOuVazio(credentials.getSenha())) {
                credentials.setSenha("");
            }
            return false;

        } else {
            return true;
        }

    }

    /**
     * Método limpa os campos informados
     */
    public void limparCampos() {
        if (FacesContext.getCurrentInstance().getMessageList() == null || FacesContext.getCurrentInstance().getMessageList().size() == 0) {
            usuario = new UsuarioLdap();
        }
        credentials = new Credentials();
    }

    private boolean existeEmpresaCadastrada(Long cnpj) {
        EmpresaVO empresaUsuario = empresaService.obterEmpresaCNPJ(cnpj);
        if (!Util.isNullOuVazio(empresaUsuario)) {
            return true;
        }
        return false;
    }

    private List<EmpresaContratoVO> obtemContratosParaAEmpresa(Long cnpj) {
        try {
            EmpresaVO empresaUsuario = empresaService.obterEmpresaCNPJ(cnpj);
            return empresaContratoService.buscarContratos(empresaUsuario);
        } catch (Exception e) {
            // nada a fazer, sinal que a consulta pelo CNPJ não retornou registros.
        }
        return null;
    }

    private String bypassLdapConveniados(UsuarioLdap usuario) throws AppException {
        usuario.setNuCnpj(new Long("34020354000110"));
        usuario.setNomeUsuario("EMPRESA PRESTADORA DA CAIXA");
        usuario.setEmail("empresa@email.com");
        montarMenuBarraHorizontal();
        setAtributosNaSessao();

        if (!existeEmpresaCadastrada(usuario.getNuCnpj())) {
            return exibeErroEmpresaNaoCadastrada(usuario.getNuCnpj().toString());
        } else {
            List<EmpresaContratoVO> contratos = obtemContratosParaAEmpresa(usuario.getNuCnpj());
            if (ObjectUtils.isNullOrEmpty(contratos)) {
                return exibeErroContratoNaoCadastradoParaAEmpresa(usuario.getNuCnpj().toString());
            } else { // seta na sessão o(s) contrato(s) identificados.
                JSFUtil.setSessionMapValue(Constantes.EXTRANET_SESSAO_CONTRATOS, contratos);
            }
        }
        return redirecionarParaTelaPadrao();
    }

    private String exibeErroCNPJAusente() {
        String mensagemLog = MensagemUtils.obterMensagem("ME004");
        logger.error(mensagemLog);
        JsfUtil.addErrorMessage(mensagemLog);
        RequestContext.getCurrentInstance().execute("hideStatus();");
        return "login";
    }

    private String exibeErroEmpresaNaoCadastrada(String cnpj) {
        String mensagemLog = MensagemUtils.obterMensagem("ME005", cnpj);
        logger.error(mensagemLog);
        JsfUtil.addErrorMessage(mensagemLog);
        RequestContext.getCurrentInstance().execute("hideStatus();");
        return "login";
    }

    private String exibeErroContratoNaoCadastradoParaAEmpresa(String cnpj) {
        String mensagemLog = MensagemUtils.obterMensagem("ME022", cnpj);
        logger.error(mensagemLog);
        JsfUtil.addErrorMessage(mensagemLog);
        RequestContext.getCurrentInstance().execute("hideStatus();");
        return "login";
    }

    private void setAtributosNaSessao() {
        JSFUtil.setSessionMapValue("loggedUser", credentials.getLogin());
        // JsfUtil.setSessionMapValue("loggedUserPassword", credentials.getSenha());
        JSFUtil.setSessionMapValue("usuario", getUsuario());
        JSFUtil.setSessionMapValue("loggedMatricula", getUsuario().getEmail());
    }

    /**
     * Método autentica login e senha no LDAP
     * 
     * @return
     */
    public String autenticarLdap() {

        if (validarCampos()) {
            try {
                usuario = new UsuarioLdap();
                
				if (this.parametroSistemaService.ehAmbienteDesenvolvimento()) {
					return bypassLdapConveniados(usuario);
				}

                LdapSearch search = new LdapSearch(System.getProperty(Constantes.URL_LDAP),
                        Integer.parseInt(System.getProperty(Constantes.PORT_SERVIDOR_LDAP)), System.getProperty(Constantes.USER_SERVIDOR_LDAP),
                        System.getProperty(Constantes.APLICATION_SERVIDOR_LDAP), System.getProperty(Constantes.SERVICE_SERVIDOR_LDAP),
                        System.getProperty(Constantes.PASSWORD_SERVIDOR_LDAP));

                if (search.isUsuarioValido(credentials.getLogin(), credentials.getSenha())) {

                    AtributoType type = new AtributoType();
                    type.put(AtributoType.LOGIN, credentials.getLogin());

                    List<UsuarioType> listaUsuario = search.recuperarUsuario(type);
                    for (UsuarioType usuarioType : listaUsuario) {
                        System.out.println("LOGIN: " + usuarioType.getAtributos().get(AtributoType.LOGIN));
                        System.out.println("CNPJ: " + usuarioType.getAtributos().get(AtributoType.CNPJ));
                        System.out.println("NOME COMPLETO: " + usuarioType.getAtributos().get(AtributoType.NOME_COMPLETO));
                        System.out
                                .println(usuarioType.getAtributos().get("pwdAccountLockedTime") == null ? "Usuário Bloqueado: Não" : "Usuário Bloqueado: Sim");
                        System.out.println("ChangedTime: " + usuarioType.getAtributos().get("pwdAccountChangedTime"));

                        usuario.setNomeUsuario(usuarioType.getAtributos().get(AtributoType.NOME_COMPLETO));

                        // caso o LDAP não retorne o CNPJ da empresa, atributo indispensável.
                        if (Util.isNullOuVazio(usuarioType.getAtributos().get(AtributoType.CNPJ))) {
                            return exibeErroCNPJAusente();
                        } else {
                            usuario.setNuCnpj(new Long(usuarioType.getAtributos().get(AtributoType.CNPJ)));
                        }

                        if (!existeEmpresaCadastrada(usuario.getNuCnpj())) {
                            return exibeErroEmpresaNaoCadastrada(usuario.getNuCnpj().toString());
                        }

                        List<EmpresaContratoVO> contratos = obtemContratosParaAEmpresa(usuario.getNuCnpj());
                        if (ObjectUtils.isNullOrEmpty(contratos)) {
                            return exibeErroContratoNaoCadastradoParaAEmpresa(usuario.getNuCnpj().toString());
                        } else { // seta na sessão o(s) contrato(s) identificados.
                            JSFUtil.setSessionMapValue(Constantes.EXTRANET_SESSAO_CONTRATOS, contratos);
                        }
                        usuario.setEmail(usuarioType.getAtributos().get(AtributoType.LOGIN));
                    }

                    montarMenuBarraHorizontal();

                    // UnidadeVO unidade = unidadeService.getById(Long.valueOf(usuario.getCoUnidade()));
                    // usuario.setNoUnidade(unidade.getNome());

                    // seta os atributos a serem utilizados na aplicação na sessão do usuário.
                    setAtributosNaSessao();

                    return redirecionarParaTelaPadrao();
                } else {
                    String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO - USUÁRIO E/OU SENHA INVÁLIDOS.", "ControleAcesso", "Login");
                    logger.error(mensagemLog);
                    JsfUtil.addErrorMessage(MensagemUtils.obterMensagem("login.label.usuarioSenha.invalidos"));
                    limparCampos();
                    RequestContext.getCurrentInstance().execute("hideStatus();");
                    return "login";
                }

            } catch (AppException e) {
                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO", "ControleAcesso", "Login", credentials.getLogin());
                logger.error(mensagemLog);
                logger.error(e.getMessage(), e);

                RequestContext.getCurrentInstance().execute("hideStatus();");
                JsfUtil.addErrorMessage(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
                limparCampos();
                return "login";

            } catch (Exception e) {

                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO", "ControleAcesso", "Login", credentials.getLogin());
                logger.error(mensagemLog);
                logger.error(e.getMessage(), e);

                limparCampos();
                RequestContext.getCurrentInstance().execute("hideStatus();");
                JsfUtil.addErrorMessage(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
                return "login";
            }

        }

        limparCampos();

        RequestContext.getCurrentInstance().execute("hideStatus();");

        return "login";
    }

    /**
     * Método encerra a sessão e redireciona para página de login
     * 
     * @return
     */
    public String logout() {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        response.resetBuffer();

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

        limparCampos();
        RequestContext.getCurrentInstance().execute("hideStatus();");
        return "/login.xhtml?faces-redirect=true";
    }

    /**
     * Redireciona para página de consultar Requisições
     * 
     * @return
     */
    private String redirecionarParaTelaPadrao() {
        RequestContext.getCurrentInstance().execute("hideStatus();");
        return "/paginas/" + PAGINA_INICIAL + ".xhtml?faces-redirect=true";
    }

    /**
     * Redireciona telas de menu
     * 
     * @return
     */
    public String redirecionarTela() {
        contextoController.setCrudMessage(null);
        contextoController.setObject(null);
        contextoController.setObjectFilter(null);
        contextoController.setTelaOrigem(null);

        if (perfilConsulta != null) {
            if (perfilConsulta == 0)
                contextoController.setPerfilConsulta(false);
            else
                contextoController.setPerfilConsulta(true);
        }

        return getLinkMenu() + "?faces-redirect=true";
    }

    /**
     * Monta o menu horizontal
     * 
     * @param pLista
     *            ,
     * @throws Exception
     */
    private void montarMenuBarraHorizontal() throws AppException {

        List<FuncionalidadeVO> listaFuncionalidade = null;

        // Obtém a lista de funcionalidades para o menu horizontal(pai), de
        // acordo com o tipo de acesso da funcionalidade
        listaFuncionalidade = serviceFuncionalidade.consultarMenuPaiPorTipoAcesso(TIPO_ACESSO);

        if (!Util.isNullOuVazio(listaFuncionalidade)) {

            DefaultMenuItem menuItem = null;
            menuBarraHorizontal = new DefaultMenuModel();

            for (FuncionalidadeVO menuVO : listaFuncionalidade) {
                menuItem = new DefaultMenuItem();
                String lNomeModificado = Util.removeAcentos(menuVO.getNome());
                menuItem.setValue(menuVO.getNome());
                menuItem.setId(MENU_ITEM + lNomeModificado + menuVO.getId());
                menuItem.setUpdate("formMenuHorizontal");
                menuItem.setCommand("#{ controleAcesso.abrirMenu(" + menuVO.getId() + ") }");
                menuItem.setParam("link", menuVO.getLink());

                if (menuVO.getLink().contains(PAGINA_INICIAL)) {
                    menuItem.setStyleClass("atual");
                }

                boolean podeInserir = true;
                if (!Util.isNullOuVazio(menuBarraHorizontal) && !menuBarraHorizontal.getElements().isEmpty()) {

                    for (MenuElement menu : menuBarraHorizontal.getElements()) {
                        if (menuItem.getId().equals(menu.getId())) {
                            podeInserir = false;
                        }
                    }
                }

                if (podeInserir) {
                    menuBarraHorizontal.addElement(menuItem);
                }
            }
        }
    }

    public String redirecionarPaginaLogin() {
        return "loginPage";
    }

    public String abrirMenu(Long pCodigoPai) {
        FuncionalidadeVO menu = new FuncionalidadeVO();

        try {
            menu = serviceFuncionalidade.getById(pCodigoPai);
            nomeMenu = menu.getNome();

            for (MenuElement menuElement : this.menuBarraHorizontal.getElements()) {
                DefaultMenuItem item = (DefaultMenuItem) menuElement;

                if (item.getCommand().contains(pCodigoPai.toString())) {
                    item.setStyleClass("atual");
                    setLinkMenu(item.getParams().get("link").toString().replace("[", "").replace("]", ""));
                } else {
                    item.setStyleClass("");
                }
            }

        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            logger.error(e.getMessage(), e);
        }

        return redirecionarTela();
    }

    public UsuarioLdap getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioLdap usuario) {
        this.usuario = usuario;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public MenuModel getMenuBarraHorizontal() {
        return menuBarraHorizontal;
    }

    public void setMenuBarraHorizontal(MenuModel menuBarraHorizontal) {
        this.menuBarraHorizontal = menuBarraHorizontal;
    }

    public String getLinkMenu() {
        return linkMenu;
    }

    public void setLinkMenu(String linkMenu) {
        this.linkMenu = linkMenu;
    }

    public List<MenuItemVertical> getMenuVerticalSistema() {
        return menuVerticalSistema;
    }

    public void setMenuVerticalSistema(List<MenuItemVertical> menuVerticalSistema) {
        this.menuVerticalSistema = menuVerticalSistema;
    }

    public String getNomeMenu() {
        return nomeMenu;
    }

    public void setNomeMenu(String nomeMenu) {
        this.nomeMenu = nomeMenu;
    }

    public Integer getPerfilConsulta() {
        return perfilConsulta;
    }

    public void setPerfilConsulta(Integer perfilConsulta) {
        this.perfilConsulta = perfilConsulta;
    }

}
