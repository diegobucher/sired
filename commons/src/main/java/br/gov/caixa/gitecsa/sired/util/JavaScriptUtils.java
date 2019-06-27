package br.gov.caixa.gitecsa.sired.util;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;

import org.apache.commons.lang.NotImplementedException;
import org.primefaces.component.fileupload.FileUpload;
import org.primefaces.context.RequestContext;

public class JavaScriptUtils {
    
    public static void showModal(String widgetVar) {
        RequestContext.getCurrentInstance().execute(widgetVar + ".show()");
    }
    
    public static void hideModal(String widgetVar) {
        RequestContext.getCurrentInstance().execute(widgetVar + ".hide()");
    }
    
    public static void execute(String script) {
        RequestContext.getCurrentInstance().execute(script);
    }
    
    public static void update(String id) {
        RequestContext.getCurrentInstance().update(id);
    }
    
    public static void update(String... idList) {
        for (String  id : idList) {
            update(id);            
        }
    }
    
    public static void hideLoading() {
        execute("hideStatus();");
    }
    
    public static UIComponent findComponentById(String id) {
        return FacesContext.getCurrentInstance().getViewRoot().findComponent(id);
    }
    
    public static void enableComponents(String...idList) {
        for (String id : idList) {
            UIComponent c = JavaScriptUtils.findComponentById(id);
            if (!ObjectUtils.isNullOrEmpty(c)) {
                if (c instanceof HtmlInputText) {
                    ((HtmlInputText)c).setDisabled(false);
                } else if (c instanceof HtmlSelectOneMenu) {
                    ((HtmlSelectOneMenu)c).setDisabled(false);
                } else if (c instanceof FileUpload) {
                    ((FileUpload)c).setDisabled(false);
                } else if (c instanceof HtmlCommandLink) {
                    ((HtmlCommandLink)c).setDisabled(false);    
                } else {
                    throw new NotImplementedException();
                }
            }
        }
    }
    
    public static void disableComponents(String...idList) {
        for (String id : idList) {
            UIComponent c = JavaScriptUtils.findComponentById(id);
            if (c instanceof HtmlInputText) {
                ((HtmlInputText)c).setDisabled(true);
            } else if (c instanceof HtmlSelectOneMenu) {
                ((HtmlSelectOneMenu)c).setDisabled(true);
            } else if (c instanceof FileUpload) {
                ((FileUpload)c).setDisabled(true);
            } else {
                throw new NotImplementedException();
            }
        }
    }
}
