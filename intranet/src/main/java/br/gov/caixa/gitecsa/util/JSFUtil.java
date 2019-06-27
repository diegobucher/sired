package br.gov.caixa.gitecsa.util;

import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;
import org.primefaces.webapp.MultipartRequest;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;

public class JSFUtil {

    // MANIPULAÇÃO DE MENSAGENS
    public static void addErrorMessage(Exception ex, String defaultMsg) {
        String msg = ex.getLocalizedMessage();
        if (msg != null && msg.length() > 0) {
            addErrorMessage(msg);
        } else {
            addErrorMessage(defaultMsg);
        }
    }

    public static void addErrorMessages(List<String> messages) {
        for (String message : messages) {
            addErrorMessage(message);
        }
    }

    public static void clearErrorMessage() {
        Iterator<FacesMessage> msgIterator = FacesContext.getCurrentInstance().getMessages();

        while (msgIterator.hasNext()) {
            msgIterator.next();
            msgIterator.remove();
        }
    }

    public static void addErrorMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
        getContext().addMessage(null, facesMsg);
    }

    public static void addSuccessMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext.getCurrentInstance().addMessage("Sucesso", facesMsg);
    }

    public static void addWarnMessage(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
        FacesContext.getCurrentInstance().addMessage("Mensagem", facesMsg);
    }

    public static void addSuccessMessage(String msg, String fiedlId) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
        FacesContext.getCurrentInstance().addMessage(fiedlId, facesMsg);
    }

    public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }

    public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
        String theId = JSFUtil.getRequestParameter(requestParameterName);
        return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
    }

    public static void setListenerValidadao(Boolean validacao, String message) {
        RequestContext context = RequestContext.getCurrentInstance();

        if (!validacao) {
            JSFUtil.addErrorMessage(message);
        }
        context.addCallbackParam("validacao", validacao);

    }

    public static String getIP() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ip = request.getLocalAddr();
        return ip;
    }

    public static Flash flashScope() {
        return (FacesContext.getCurrentInstance().getExternalContext().getFlash());
    }

    public static Object getSessionMapValue(String key) {
        if (getContext() != null) {
            return getContext().getExternalContext().getSessionMap().get(key);
        } else {
            return null;
        }
    }

    public static void setSessionMapValue(String key, Object value) {
        getContext().getExternalContext().getSessionMap().put(key, value);
    }

    public static Object getApplicationMapValue(String key) {
        return getContext().getExternalContext().getApplicationMap().get(key);
    }

    public static void setApplicationMapValue(String key, Object value) {
        getContext().getExternalContext().getApplicationMap().put(key, value);
    }

    public static String getRequestParameter(String name) {
        return (String) getContext().getExternalContext().getRequestParameterMap().get(name);
    }

    public static HttpServletResponse getServletResponse() {
        FacesContext context = getContext();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        return response;
    }

    public static HttpSession getSession() {
        FacesContext context = getContext();

        return (HttpSession) context.getExternalContext().getSession(false);
    }

    public static FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    /**
     * Finds our MultipartRequestServletWrapper in case application contains other RequestWrappers
     */
    public static MultipartRequest getMultipartRequestInChain() {
        Object request = getContext().getExternalContext().getRequest();
        while (request instanceof ServletRequestWrapper) {
            if (request instanceof MultipartRequest) {
                return (MultipartRequest) request;
            } else {
                request = ((ServletRequestWrapper) request).getRequest();
            }
        }
        return null;
    }

    public static UsuarioLdap getUsuario() {
        // TODO: C131528 - Verificar uso.
        if (getSessionMapValue("usuario") != null)
            return (UsuarioLdap) getSessionMapValue("usuario");
        return null;
    }

    @SuppressWarnings("unchecked")
    public static boolean isPerfil(String vlr) {
        // TODO: C131528 - Verificar uso.
        List<String> perfilUsuario = (List<String>) getSessionMapValue("perfisUsuario");
        for (String perfil : perfilUsuario) {
            if (perfil.trim().toUpperCase().equalsIgnoreCase(vlr.trim().toLowerCase())) {
                return true;
            }
        }
        /*
         * UsuarioLdap ldap = getUsuario(); for (String tmp : ldap.getListaGruposLdap()) {
         * if(tmp.trim().toUpperCase().equals(vlr.trim().toUpperCase())) return true; }
         */
        return false;
    }

}
