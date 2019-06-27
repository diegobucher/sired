package br.gov.caixa.gitecsa.arquitetura.controller;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;

import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.util.JSFUtil;
import br.gov.caixa.gitecsa.util.ReportUtils;

/**
 * Classe base para qualquer outro controller
 */
public abstract class AppBaseController extends Util implements Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    @Inject
    protected FacesMensager facesMessager;

    @Inject
    protected transient Logger logger;

    protected String facesRedirect = "?faces-redirect=true";

    /**
     * Construtor default
     */
    public AppBaseController() {
        super();
    }

    /**
     * Esconde o componente do widgetvar especificado
     * 
     * @param widgetvar
     */
    protected void hideDialog(String widgetvar) {
        RequestContext.getCurrentInstance().execute(widgetvar + ".hide()");
    }

    /**
     * Método para ordenar string ignorando utilizado no sortFuction
     * 
     * @param msg1
     * @param msg2
     * @return
     */
    public int sortByString(Object msg1, Object msg2) {
        return ((String) msg1).compareToIgnoreCase(((String) msg2));
    }

    /**
     * Método para ordenar integer ignorando utilizado no sortFuction
     * 
     * @param msg1
     * @param msg2
     * @return
     */
    public int sortByInteger(Object msg1, Object msg2) {
        return ((Integer) msg1).compareTo((Integer) msg2);
    }

    /**
     * Método para ordenar double ignorando utilizado no sortFuction
     * 
     * @param msg1
     * @param msg2
     * @return
     */
    public int sortByDouble(Object msg1, Object msg2) {
        return ((Double) msg1).compareTo((Double) msg2);
    }

    /**
     * Mostra o componente do widgetvar especificado
     * 
     * @param widgetvar
     */
    protected void showDialog(String widgetvar) {
        RequestContext.getCurrentInstance().execute(widgetvar + ".show()");
    }

    /**
     * Faz update nos componentes dos respectivos ids passados como parâmetro
     * 
     * @param idComponente
     */
    protected void updateComponentes(String... idComponente) {
        for (String id : idComponente) {
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add(id);
        }
    }

    public String getRequiredMessage(String label) {
        return MensagemUtils.obterMensagem("MA001", MensagemUtils.obterMensagem(label));
    }

    public void giveFocus(String pIdComponente) {
        RequestContext.getCurrentInstance().execute("giveFocus('" + pIdComponente + "')");
    }

    protected String includeRedirect(String url) {
        String formatedUrl = url.concat(facesRedirect);
        return formatedUrl;
    }

    protected String getRootErrorMessage(Exception e) {
        String errorMessage = MensagemUtils.obterMensagem("geral.crud.rootErrorMessage");
        if (e == null) {
            // This shouldn't happen, but return the default messages
            return errorMessage;
        }

        // JsfUtil.addErrorMessage(errorMessage);

        // Start with the exception and recurse to find the root cause
        Throwable t = e;
        while (t != null) {
            // Get the message from the Throwable class instance
            errorMessage = t.getLocalizedMessage();
            t = t.getCause();
        }
        // This is the root cause message
        return errorMessage;
    }

    protected String createViolationResponse(Set<ConstraintViolation<?>> violations) {
        /*
         * logger.fine("Validation completed. violations found: " + violations.size());
         */

        Map<String, String> responseObj = new HashMap<String, String>();

        for (ConstraintViolation<?> violation : violations) {
            responseObj.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        return responseObj.toString();
    }

    protected void logErroMessage() {
        /*
         * logger.severe("--------------------------------------------"); logger.severe(
         * "Ocorreu um erro ao Persistir o registro, verifique o log do servidor." );
         * logger.severe("--------------------------------------------");
         */
    }

    // =======================================================================================================
    // Métodos Relatório
    // =======================================================================================================
    protected void escreveRelatorio(byte[] relatorio, String nome, boolean download) {
        try {
            this.escreveRelatorioResponse(relatorio, nome, ReportUtils.REL_TIPO_PDF, "application/pdf", download);
        } catch (Exception e) {
            getRootErrorMessage(e);
        }
    }

    protected void escreveRelatorio(byte[] relatorio, String nome, String tipo, String contentType, boolean download) {
        try {
            this.escreveRelatorioResponse(relatorio, nome, tipo, contentType, download);
        } catch (Exception e) {
            getRootErrorMessage(e);
        }
    }

    protected void escreveRelatorio(byte[] relatorio, String nome, String contentType, boolean download) throws IOException {
        try {
            this.escreveRelatorioResponse(relatorio, nome, null, contentType, download);
        } catch (Exception e) {
            getRootErrorMessage(e);
        }
    }

    private void escreveRelatorioResponse(byte[] relatorio, String nome, String tipo, String contentType, boolean download) throws IOException {
        try {
            String contentDisposition = download ? "attachment;filename=" : "inline;filename=";
            String nomeCompleto;

            if (tipo != null) {
                nomeCompleto = contentDisposition + "\"" + nome + "." + tipo.toLowerCase() + "\"";
            } else {
                nomeCompleto = contentDisposition + "\"" + nome + "\"";
            }

            JSFUtil.getServletResponse().setContentType(contentType);
            JSFUtil.getServletResponse().setHeader("Content-Disposition", nomeCompleto);
            JSFUtil.getServletResponse().setContentLength(relatorio.length);
            JSFUtil.getServletResponse().getOutputStream().write(relatorio);
        } catch (IOException e) {
            throw e;
        } finally {
            JSFUtil.getServletResponse().getOutputStream().flush();
            JSFUtil.getServletResponse().getOutputStream().close();
            JSFUtil.getContext().responseComplete();
        }
    }
}
