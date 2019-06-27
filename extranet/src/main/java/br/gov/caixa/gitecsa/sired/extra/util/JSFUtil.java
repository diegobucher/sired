package br.gov.caixa.gitecsa.sired.extra.util;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;

public class JSFUtil {

    private static final String NOME_USUARIO = "usuario";

    public static FacesContext getContext() {
        return FacesContext.getCurrentInstance();
    }

    public static void setSessionValue(String key, Object value) {
        HttpServletRequest request = (HttpServletRequest) getContext().getExternalContext().getRequest();
        request.getSession().setAttribute(key, value);
    }

    public static Object getSessionValue(String key) {
        HttpServletRequest request = (HttpServletRequest) getContext().getExternalContext().getRequest();
        return request.getSession().getAttribute(key);
    }

    public static UsuarioLdap getUsuario() {
        HttpServletRequest request = (HttpServletRequest) getContext().getExternalContext().getRequest();
        return (UsuarioLdap) request.getSession().getAttribute(NOME_USUARIO);
    }

    public static boolean isPerfil(String vlr) {
        UsuarioLdap ldap = getUsuario();

        for (String tmp : ldap.getListaGruposLdap()) {
            if (tmp.trim().toUpperCase().equals(vlr.trim().toUpperCase())) {
                return true;
            }
        }

        return false;
    }

}
