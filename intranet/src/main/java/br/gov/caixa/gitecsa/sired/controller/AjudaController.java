package br.gov.caixa.gitecsa.sired.controller;

import java.io.File;
import java.io.Serializable;
import java.net.URL;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;

import br.gov.caixa.gitecsa.sired.util.LogUtils;

@Named
@ViewScoped
public class AjudaController implements Serializable {
    
    private static final long serialVersionUID = 1596233292193754872L;
    
    private static final String RESOURCES_PATH = "resources";

    private static final String MANUAL_INTRANET = "SIRED_Intranet_Manual_Usuario.pdf";
    
    private final transient Logger logger = LogUtils.getLogger(AjudaController.class.getName());

    public void downloadManual() {
        
        try {
            
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            
            HttpServletRequest request = ((HttpServletRequest) context.getRequest());
            String manual = RESOURCES_PATH + "/" + MANUAL_INTRANET;
            
            if (new File(context.getRealPath(manual)).exists()) {            
                URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath() + "/" + manual);
                context.redirect(url.toString());
            }
            
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
