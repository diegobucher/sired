package br.gov.caixa.gitecsa.sired.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;

public class RequestUtils {

    private static final String ERROR_HOST_UNKNOWN = "Host desconhecido";

    public static String getParameter(String param) {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return request.getParameter(param);
    }

    public static Map<String, String[]> getParameters() {
        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        return request.getParameterMap();
    }

    public static StreamedContent download(File file, String filename) throws BusinessException {
        try {
            return new DefaultStreamedContent(new FileInputStream(file), FileUtils.getMimeType(file), filename);
        } catch (FileNotFoundException e) {
            throw new BusinessException(MensagemUtils.obterMensagem("MI028"));
        } catch (IOException e) {
            throw new BusinessException(MensagemUtils.obterMensagem("MI028"));
        }
    }

    public static String getHostName() {

        try {

            HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
            InetAddress address = Inet4Address.getByName(request.getRemoteHost());
            String hostname = address.getHostName();

            return hostname;

        } catch (Exception e) {
            return ERROR_HOST_UNKNOWN;
        }
    }

    public static Object getSessionValue(String key) {
        if (ObjectUtils.isNullOrEmpty(FacesContext.getCurrentInstance())) {
            return null;
        }
        Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
        return (!ObjectUtils.isNullOrEmpty(sessionMap)) ? sessionMap.get(key) : null;
    }

    public static void setSessionValue(String key, Object value) {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, value);
    }
    
    public static Object getFlashData(String key) {
        Object value = RequestUtils.getSessionValue(key);
        RequestUtils.unsetSessionValue(key);
        return value;
    }
    
    public static void unsetSessionValue(String key) {
        if (!ObjectUtils.isNullOrEmpty(FacesContext.getCurrentInstance())) {
            Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
            if (!ObjectUtils.isNullOrEmpty(sessionMap)) {
                sessionMap.remove(key);
            }
        }
    }
    
    public static String getViewId() {
        return FacesContext.getCurrentInstance().getViewRoot().getViewId();
    }
    
    public static HttpServletResponse getServletResponse() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();

        return response;
    }   
}
