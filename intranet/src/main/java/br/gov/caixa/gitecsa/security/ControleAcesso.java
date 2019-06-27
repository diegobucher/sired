package br.gov.caixa.gitecsa.security;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.NameNotFoundException;
import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import br.gov.caixa.gitecsa.arquitetura.controller.ContextoController;
import br.gov.caixa.gitecsa.ldap.AutenticadorLdap;
import br.gov.caixa.gitecsa.ldap.usuario.Credentials;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.security.dto.MenuItemVertical;
import br.gov.caixa.gitecsa.service.FuncionalidadeService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.siico.service.FuncoesGerenciaisService;
import br.gov.caixa.gitecsa.sired.siico.vo.ViewFuncoesGerenciaisVO;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.FuncionalidadeVO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.PerfilVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;
import br.gov.caixa.gitecsa.util.MenuItem;
import br.gov.caixa.gitecsa.util.MenuUtil;

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

    private UsuarioLdap usuario;

    @Inject
    private static Logger logger = Logger.getLogger(ControleAcesso.class);

    @Inject
    private FuncionalidadeService serviceFuncionalidade;

    @Inject
    private ContextoController contextoController;

    @Inject
    private UnidadeService unidadeService;
    
    @Inject
    private ParametroSistemaService parametroSistemaService;
    
    @Inject
    private FuncoesGerenciaisService funcoesGerenciaisService;
    
    private static final String PAGINA_INICIAL = "requisicao/consulta";
    private static final String MENU_INICIAL = "Requisição";

    private Credentials credentials;

    private String matriculaUsuario;

    private MenuUtil menu = new MenuUtil();

    private String linkMenu;

    private Integer perfilConsulta;

    private List<MenuItemVertical> menuVerticalSistema;

    private String nomeMenu;

    private void getDiaDaSemana() {
        JSFUtil.setSessionMapValue("dataDiaExtenso", Util.formatData(Calendar.getInstance().getTime(), "EEEE, dd/MM/yyyy HH:mm"));
    }

    public ControleAcesso() {
        credentials = new Credentials();
        getDiaDaSemana();
    }

    public boolean validarCampos() {

        if (Util.isNullOuVazio(credentials) || Util.isNullOuVazio(credentials.getLogin()) || Util.isNullOuVazio(credentials.getSenha())) {

            if (Util.isNullOuVazio(credentials.getLogin()) || "_______".equals(credentials.getLogin())) {
                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA001", MensagemUtils.obterMensagem("geral.label.usuario")));
            }
            if (Util.isNullOuVazio(credentials.getSenha())) {
                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA001", MensagemUtils.obterMensagem("login.label.senha")));
            }

            if (!Util.isNullOuVazio(credentials.getSenha())) {
                credentials.setSenha("");
            }
            return false;

        } else {
            return true;
        }

    }

    public void limparCampos() {
        if (FacesContext.getCurrentInstance().getMessageList() == null || FacesContext.getCurrentInstance().getMessageList().size() == 0) {
            usuario = new UsuarioLdap();
            credentials = new Credentials();
        }
    }

    public void limparSessao() {
        HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();

        response.resetBuffer();

        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    }

	private boolean usuarioEhFuncionarioCaixa(String matricula) {
		if (matricula.toUpperCase().startsWith("C")) {
			return true;
		}
		return false;
	}
	
    private List<String> obtemPerfilUsuario() throws AppException {
    	
    	List<PerfilVO> perfis = new ArrayList<>();
    	List<String> perfisSigla = new ArrayList<>();
        List<String> descricaoPerfis = new ArrayList<>();
        
        //-- Admin
        perfisSigla = this.isUsuarioAdministrador(descricaoPerfis, perfis);
        
        if(!Util.isNullOuVazio(perfisSigla)) {
          return perfisSigla;
        }
        
        //-- Gestor
        perfisSigla = this.isUsuarioGestor(descricaoPerfis, perfis);
        
        if(!Util.isNullOuVazio(perfisSigla)) {
          return perfisSigla;
        }
        
        //-- Gerente
        perfisSigla = this.isUsuarioGerente(descricaoPerfis, perfis);
        
        if(!Util.isNullOuVazio(perfisSigla)) {
          return perfisSigla;
        }
        
        if (!Util.isNullOuVazio(usuario.getListaGruposLdap())) {
            perfis = serviceFuncionalidade.consultarPerfis(usuario.getListaGruposLdap());

            if (Util.isNullOuVazio(perfis)) {
                Collections.sort(usuario.getListaGruposLdap());
                perfis = serviceFuncionalidade.consultarTransacoes(usuario.getListaGruposLdap());
            }
        }
        
        if (!Util.isNullOuVazio(perfis)) {

            for (PerfilVO perfilVO : perfis) {
                descricaoPerfis.add(perfilVO.getDescricao());
                perfisSigla.add(perfilVO.getSigla().trim().toUpperCase());
            }
            usuario.setListaGruposLdap(descricaoPerfis);
        } else if (usuarioEhFuncionarioCaixa(usuario.getNuMatricula())) { // acesso com perfil padrão.

			perfis = serviceFuncionalidade.consultarGruposPadrao();
			if (!Util.isNullOuVazio(perfis)) {

				for (PerfilVO perfilVO : perfis) {
					descricaoPerfis.add(perfilVO.getDescricao());
					perfisSigla.add(perfilVO.getSigla().trim().toUpperCase());
				}
				usuario.setListaGruposLdap(descricaoPerfis);
			}
		}
        return perfisSigla;
    }

    private List<String> isUsuarioGestor(List<String> descricaoPerfis, List<PerfilVO> perfis) throws DataBaseException {
      ParametroSistemaVO parametro = parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_UNIDADE_MATRICULA_GESTOR);
      if(!Util.isNullOuVazio(parametro)) {
        String valor = parametro.getVlParametroSistema();
        if(valor.indexOf(';') != -1) {
          List<String> perfisSigla = new ArrayList<>();
          String[] gestores = valor.split(";", -1);
          for (String string : gestores) {
            if(string.contains(usuario.getCoUnidade().toString()) &&  string.toUpperCase().contains(usuario.getNuMatricula().toUpperCase())) {
              perfis = serviceFuncionalidade.consultarGrupoGestor();
              descricaoPerfis = atribuiDescricaoSiglaPerfil(descricaoPerfis, perfis, perfisSigla);
              usuario.setListaGruposLdap(descricaoPerfis);
              return perfisSigla;
            }
          }
        }
      }
      return new ArrayList<>();
    }

    private List<String> atribuiDescricaoSiglaPerfil(List<String> descricaoPerfis, List<PerfilVO> perfis, List<String> perfisSigla) {
      if(!Util.isNullOuVazio(perfis)) {
        for (PerfilVO perfilVO : perfis) {
          descricaoPerfis.add(perfilVO.getDescricao());
          perfisSigla.add(perfilVO.getDescricao());
        }
        return descricaoPerfis;
      }else {
        return new ArrayList<>();
      }
    }

    private List<String> isUsuarioAdministrador(List<String> descricaoPerfis, List<PerfilVO> perfis) throws DataBaseException {
      ParametroSistemaVO parametro = parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_UNIDADE_MATRICULA_ADM);
      if(!Util.isNullOuVazio(parametro)) {
        String valor = parametro.getVlParametroSistema();
        if(valor.indexOf(';') != -1) {
          List<String> perfisSigla = new ArrayList<>();
          String[] administradores = valor.split(";", -1);
          for (String string : administradores) {
            if(string.contains(usuario.getCoUnidade().toString()) &&  string.toUpperCase().contains(usuario.getNuMatricula().toUpperCase())) {
              perfis = serviceFuncionalidade.consultarGrupoAdmin();
              atribuiDescricaoSiglaPerfil(descricaoPerfis, perfis, perfisSigla);
              usuario.setListaGruposLdap(descricaoPerfis);
              return perfisSigla;
            }
          }
        }
      }
      return new ArrayList<>();
    }

    private List<String> isUsuarioGerente(List<String> descricaoPerfis, List<PerfilVO> perfis)
        throws DataBaseException {
      //CASO O USUARIO TENHA FUNCAO GERENCIAL NO LDAP, ELE ACESSA COMO GERENTE
      List<String> perfisSigla = new ArrayList<>();
      
      if (usuario.getNuFuncao() == null) {
    	  return new ArrayList<>();
      }
      
      ViewFuncoesGerenciaisVO funcao = funcoesGerenciaisService.findById(usuario.getNuFuncao().longValue());
      
      if(!Util.isNullOuVazio(funcao)) {
        List<String> perfilGerente = new ArrayList<>();
        perfilGerente.add(Constantes.PERFIL_GERENTE_LOGIN);
        perfis = serviceFuncionalidade.consultarPerfis(perfilGerente);
        for (PerfilVO perfilVO : perfis) {
          descricaoPerfis.add(perfilVO.getDescricao());
          perfisSigla.add(perfilVO.getSigla().trim().toUpperCase());
        }
        usuario.setListaGruposLdap(descricaoPerfis);
        return perfisSigla;
      }
      return new ArrayList<>();
    }

    public String getSiglaPerfisLdapAsString(List<PerfilVO> perfis) {
        String siglas = null;
        if (!Util.isNullOuVazio(perfis)) {

            siglas = perfis.get(0).getSigla().trim().toUpperCase();

            for (int i = 1; i < perfis.size(); i++) {
                siglas = siglas + "," + perfis.get(i).getSigla().trim().toUpperCase();
            }

        }
        return siglas;
    }
    
	private UsuarioLdap instanciaUsuarioAmbienteDesenvolvimento(AutenticadorLdap autenticador) throws Exception {
		// Obtem o usuario do LDAP no servidor LDAP DEV
		UsuarioLdap usuario;
		if (credentials.getLogin().length() == 7) {
			usuario = autenticador.autenticaUsuarioLdapCaixa(credentials, "ldap://IFR7562LX019.ldap.caixa:489",
					"SIWMC", "ldap_cetec");
			if (ObjectUtils.isNullOrEmpty(usuario.getListaGruposLdapAsString())
					|| "AVANCADO".equals(usuario.getListaGruposLdapAsString())
					|| "CONSULTA".equals(usuario.getListaGruposLdapAsString())
					|| "EMISSOR".equals(usuario.getListaGruposLdapAsString())
					|| "FORCONSU".equals(usuario.getListaGruposLdapAsString())
					|| "MASTER".equals(usuario.getListaGruposLdapAsString())) {
				List<String> geral = usuario.getListaGruposLdap();
				if (ObjectUtils.isNullOrEmpty(geral)) {
					// coloca o perfil padrão GERAL.
					geral = new ArrayList<String>();
					geral.add("GERAL");
					usuario.setListaGruposLdap(geral);
				}
			}
		} else {
			// cria um usuário fake e coloca o perfil padrão GERAL.
			usuario = new UsuarioLdap();
			usuario.setCidade("SALVADOR");
			usuario.setCoUnidade(7470);
			usuario.setDescription("ASSISTENTE JUNIOR");
			usuario.setEmail("C131528@mail.caixa");
			usuario.setEstadoLotacao("BA");
			usuario.setGivenName("DEV");
			List<String> geral = new ArrayList<String>();
			geral.add("GESTOR");
			usuario.setListaGruposLdap(geral);
			usuario.setNomeLotacao("GITEC");
			usuario.setNomeUsuario("GITEC DEV");
			usuario.setNoUsuario("GITEC DEV");
			usuario.setNuMatricula("C131528");
			usuario.setSobreNomeUsuario("GITEC DEV");
			usuario.setSiglaUnidade("GITEC");
		}
		return usuario;
	}

    public String autenticarLdap() {
        if (validarCampos()) {
            try {
                AutenticadorLdap autenticador = new AutenticadorLdap();

				if (this.parametroSistemaService.ehAmbienteDesenvolvimento()) {
					usuario = instanciaUsuarioAmbienteDesenvolvimento(autenticador);
				} else {
					// Obtem o usuario do LDAP
					usuario = autenticador.autenticaUsuarioLdapCaixa(credentials,
							System.getProperty(Constantes.INTRANET_URL_LDAP),
							System.getProperty(Constantes.INTRANET_NOME_SISTEMA),
							System.getProperty(Constantes.INTRANET_SECURITY_DOMAIN));
				}             

                if (!Util.isNullOuVazio(usuario) && !Util.isNullOuVazio(usuario.getNuMatricula())) {

                    logger.info("*-*-*-*-*-*-" + usuario.getNuMatricula() + " || " + usuario.getListaGruposLdapAsString());

                    // Identifica o perfil do usuário logado
                    List<String> siglasPerfis = this.obtemPerfilUsuario();
                    
                    if (!Util.isNullOuVazio(usuario.getListaGruposLdapAsString())) {
                        // Seta Matrícula do usuário
                        this.setMatriculaUsuario(credentials.getLogin());

                        // Monta o menu horizontal(pai), de acordo com o perfil
                        // do usuário
                        try {
                            montarMenuBarraHorizontal(usuario.getListaGruposLdapAsString());
                        } catch (AppException e) { // não existe grupo/perfil cadastrado no sistema para o usuário.
                            logger.error(e.getMessage());
                            JSFUtil.addErrorMessage(e.getMessage());
                            limparCampos();
                            RequestContext.getCurrentInstance().execute("hideStatus();");
                            return "login";
                        }

                        UnidadeVO unidade = unidadeService.getById(Long.valueOf(usuario.getCoUnidade()));
                        if (ObjectUtils.isNullOrEmpty(unidade)) { // erro ao recuperar a unidade do usuário na base do SIRED
                            String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA - UNIDADE NÃO ENCONTRADA NO SIRED: " + usuario.getCoUnidade(),
                                    "ControleAcesso", "Login");
                            logger.error(mensagemLog);
                            JSFUtil.addErrorMessage(mensagemLog);
                            limparCampos();
                            RequestContext.getCurrentInstance().execute("hideStatus();");
                            return "login";
                        }

                        usuario.setNoUnidade(unidade.getNome());

                        // Setando dados do usuario na sessão
                        JSFUtil.setSessionMapValue("loggedUser", credentials.getLogin());
                        JSFUtil.setSessionMapValue("loggedUserPassword", credentials.getSenha());
                        JSFUtil.setSessionMapValue("perfisUsuario", siglasPerfis);
                        JSFUtil.setSessionMapValue("loggedMatricula", getUsuario().getNuMatricula());
                        JSFUtil.setSessionMapValue("usuario", getUsuario());

                        // Redireciona sistema para a tela padrão
                        return redirecionarParaTelaPadrao();
                    } else {
                        String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO - NENHUM GRUPO/PERFIL ENCONTRADO", "ControleAcesso", "Login");
                        logger.error(mensagemLog);
                        JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA039"));
                        limparCampos();
                        RequestContext.getCurrentInstance().execute("hideStatus();");
                        return "login";
                    }

                } else {
                    throw new AppException(LogUtils.getMensagemPadraoLog("NÃO FOI POSSÍVEL OBTER O USUÁRIO DO LDAP", "ControleAcesso", "Login",
                            credentials.getLogin()));
                }

            } catch (CommunicationException e) {

                String nomeLdap = System.getProperty(Constantes.INTRANET_URL_LDAP);

                String ipLdap = nomeLdap.substring(nomeLdap.indexOf("//") + 2);
                String portaLdap = ipLdap.substring(ipLdap.indexOf(":") + 1);
                ipLdap = ipLdap.substring(0, ipLdap.indexOf(":"));

                String mensagem = "ERRO - NAO FOI POSSIVEL CONECTAR O SERVIDOR LDAP - DADOS:" + "IP_SERVIDOR LDAP: " + ipLdap + " PORTA DO SERVIDOR LDAP: "
                        + portaLdap;

                String mensagemLog = LogUtils.getMensagemPadraoLog(mensagem, "ControleAcesso", "Login", credentials.getLogin());
                logger.error(mensagemLog);

                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA052"));
                limparCampos();
                RequestContext.getCurrentInstance().execute("hideStatus();");
                return "login";

            } catch (NameNotFoundException e) {
                String nomeLdap = System.getProperty(Constantes.INTRANET_URL_LDAP);

                String ipLdap = nomeLdap.substring(nomeLdap.indexOf("//") + 2);
                ipLdap = ipLdap.substring(0, ipLdap.indexOf(":"));

                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO - GRUPO NÃO EXISTE", "ControleAcesso", "Login",
                        credentials.getLogin());
                logger.error(mensagemLog);

                limparCampos();
                RequestContext.getCurrentInstance().execute("hideStatus();");
                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA053", ipLdap, System.getProperty(Constantes.INTRANET_NOME_SISTEMA)));
                return "login";
            } catch (FailedLoginException e) {

                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO - USUARIO OU SENHA INVALIDOS", "ControleAcesso", "Login",
                        credentials.getLogin());
                logger.error(mensagemLog);
                limparCampos();

                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA002"));
                RequestContext.getCurrentInstance().execute("hideStatus();");
                return "login";

            } catch (AuthenticationException e) {

                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO - USUARIO OU SENHA INVALIDOS", "ControleAcesso", "Login",
                        credentials.getLogin());
                logger.error(mensagemLog);

                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA002"));
                limparCampos();
                RequestContext.getCurrentInstance().execute("hideStatus();");
                return "login";
            } catch (DataBaseException e) {
                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO", "ControleAcesso", "Login", credentials.getLogin());
                logger.error(mensagemLog);

                RequestContext.getCurrentInstance().execute("hideStatus();");
                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA012"));
                limparCampos();
                return "login";
            } catch (AppException e) {
                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO", "ControleAcesso", "Login", credentials.getLogin());
                logger.error(mensagemLog);

                RequestContext.getCurrentInstance().execute("hideStatus();");
                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA012"));
                limparCampos();
                return "login";

            } catch (Exception e) {
                String mensagemLog = LogUtils.getMensagemPadraoLog("FALHA DE AUTENTICAÇÃO", "ControleAcesso", "Login", credentials.getLogin());
                logger.error(mensagemLog);

                limparCampos();
                RequestContext.getCurrentInstance().execute("hideStatus();");
                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA012"));
                e.printStackTrace();
                return "login";
            }
        }

        RequestContext.getCurrentInstance().execute("hideStatus();");
        return "login";
    }

    /**
     * Método encerra a sessão e redireciona para página de login
     * 
     * @return
     */
    public String logout() {
        limparSessao();
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
        // return "/home.xhtml?faces-redirect=true";
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
    private void montarMenuBarraHorizontal(String listaGrupos) throws AppException {
        List<FuncionalidadeVO> listaFuncionalidade = null;

        listaFuncionalidade = serviceFuncionalidade.consultarMenuPaiPorGrupo(listaGrupos);

        if (!Util.isNullOuVazio(listaFuncionalidade)) {

            MenuItem menuItem = null;

            for (FuncionalidadeVO menuVO : listaFuncionalidade) {
                menuItem = obtemMenuElement(menuVO);

                if (menuVO.getNome().contains(MENU_INICIAL)) {
                    menuItem.setStyleClass("atual");
                }

                boolean podeInserir = true;
                if (!Util.isNullOuVazio(menu) && !menu.getItens().isEmpty()) {

                    for (MenuItem mItem : menu.getItens()) {
                        if (menuItem.getId().equals(mItem.getId())) {
                            podeInserir = false;
                        }
                    }
                }

                if (podeInserir) {
                    menu.getItens().add(menuItem);

                    List<FuncionalidadeVO> listaFilhos = montarMenuVertical((Long) menuVO.getId());

                    for (FuncionalidadeVO menuVOFilho : listaFilhos) {
                        MenuItem child = obtemMenuElement(menuVOFilho);
                        menuItem.subItens.add(child);
                    }
                }
            }
        } else { // perfil(s) não cadastrados na base.
            throw new AppException(MensagemUtils.obterMensagem("MA050", listaGrupos));
        }
    }

    /**
     * Monta o menu vertical(filho), de acordo com o perfil do usuário
     * 
     * @param pLista
     * @return
     * @throws Exception
     */
    private List<FuncionalidadeVO> montarMenuVertical(Long pCodigoPai) {

        List<FuncionalidadeVO> lista = null;

        if (!Util.isNullOuVazio(usuario.getListaGruposLdapAsString())) {

            try {
                lista = serviceFuncionalidade.consultarFuncionalidadeSistemaFilhoPorGrupo(usuario.getListaGruposLdapAsString(), pCodigoPai.shortValue());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return lista;
    }

    @SuppressWarnings("unchecked")
    public boolean isPerfil(String vlr) {
        List<String> perfilUsuario = (List<String>) JSFUtil.getSessionMapValue("perfisUsuario");

        for (String perfil : perfilUsuario) {
            if (perfil.trim().toUpperCase().equalsIgnoreCase(vlr.trim().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private MenuItem obtemMenuElement(FuncionalidadeVO menuVO) {
        MenuItem menuItem = new MenuItem();

        menuItem.setId("_" + menuVO.getId() + "_");

        menuItem.setNome(menuVO.getNome());

        menuItem.setLink(menuVO.getLink());

        return menuItem;
    }

    public String redirecionarPaginaLogin() {
        return "loginPage";
    }

    public String abrirMenu(String id) {
        FuncionalidadeVO menu = new FuncionalidadeVO();

        try {
            menu = serviceFuncionalidade.getById(Long.parseLong(id.replaceAll("_", "")));
            nomeMenu = menu.getNome();

            if (menu.getFuncionalidadePai() != null) {

                for (MenuItem itemPai : this.menu.getItens()) {

                    Boolean itemPaiSelecionado = false;

                    for (MenuItem item : itemPai.getSubItens()) {

                        if (item.getId().contains(id.toString())) {
                            item.setStyleClass(MenuUtil.MENU_ITEM_ATIVO_CLASS);
                            setLinkMenu(item.getLink());
                            itemPaiSelecionado = true;
                        } else {
                            item.setStyleClass(MenuUtil.MENU_ITEM_INATIVO_CLASS);
                        }
                    }

                    String itemPaiClass = (itemPaiSelecionado) ? MenuUtil.MENU_ITEM_ATIVO_CLASS : MenuUtil.MENU_ITEM_INATIVO_CLASS;
                    itemPai.setStyleClass(itemPaiClass);
                }
            } else {
                for (MenuItem item : this.menu.getItens()) {

                    if (item.getId().contains(id.toString())) {
                        item.setStyleClass(MenuUtil.MENU_ITEM_ATIVO_CLASS);
                        setLinkMenu(item.getLink());
                    } else {
                        item.setStyleClass(MenuUtil.MENU_ITEM_INATIVO_CLASS);
                    }
                }
            }

        } catch (Exception e) {
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

    public String getMatriculaUsuario() {
        return matriculaUsuario;
    }

    public void setMatriculaUsuario(String matriculaUsuario) {
        this.matriculaUsuario = matriculaUsuario;
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

    public MenuUtil getMenu() {
        return menu;
    }

    public void setMenu(MenuUtil menu) {
        this.menu = menu;
    }

}
