package br.gov.caixa.gitecsa.arquitetura.controller;

import java.text.MessageFormat;
import java.util.Iterator;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

/**
 * Classe utilitaria para gerencimento de mensagens <br>
 * do JSF.
 * 
 */
@RequestScoped
public class FacesMensager {

    @Inject
    protected FacesContext facesContext;

    // ************************************************************************************************
    // Metodos de Mensagem
    // ************************************************************************************************

    /**
     * Gera Mensagem de informacao para o usuario.
     * 
     * @param String
     *            - Mensagem a ser exibida.
     */
    public void info(String message) {
        facesContext.addMessage(message, new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    /**
     * Gera Mensagem de informacao para o usuario associando esta mensagem a um
     * componente.
     * 
     * @param UIComponent
     *            - Componente a ser associado a mensagem. String - Mensagem a
     *            ser exibida.
     */
    public void info(UIComponent component, String message) {
        facesContext.addMessage(component.getClientId(facesContext), new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    /**
     * Gera Mensagem de erro para o usuario.
     * 
     * @param String
     *            - Mensagem a ser exibida.
     */
    public void error(String message) {
        facesContext.addMessage(message, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    /**
     * Gera Mensagem de erro para o usuario associando esta mensagem a um
     * componente.
     * 
     * @param UIComponent
     *            - Componente a ser associado a mensagem. String - Mensagem a
     *            ser exibida.
     */
    public void error(UIComponent component, String message) {
        facesContext.addMessage(component.getClientId(facesContext), new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    /**
     * Gera Mensagem de erro fatal para o usuario.
     * 
     * @param String
     *            - Mensagem a ser exibida.
     */
    public void fatal(String message) {
        facesContext.addMessage(message, new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
    }

    /**
     * Gera Mensagem de erro fatal para o usuario associando esta mensagem a um
     * componente.
     * 
     * @param UIComponent
     *            - Componente a ser associado a mensagem. String - Mensagem a
     *            ser exibida.
     */
    public void fatal(UIComponent component, String message) {
        facesContext.addMessage(component.getClientId(facesContext), new FacesMessage(FacesMessage.SEVERITY_FATAL, message, null));
    }

    /**
     * Gera Mensagem de aviso para o usuario.
     * 
     * @param String
     *            - Mensagem a ser exibida.
     */
    public void warn(String message) {
        facesContext.addMessage(message, new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }

    /**
     * Gera Mensagem de aviso para o usuario associando esta mensagem a um
     * componente.
     * 
     * @param UIComponent
     *            - Componente a ser associado a mensagem. String - Mensagem a
     *            ser exibida.
     */
    public void warn(UIComponent component, String message) {
        facesContext.addMessage(component.getClientId(facesContext), new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }

    /**
     * Método que cria uma mensagem de informacao e adiciona no contexto corrente
     * 
     * @param codMensagem
     */
    public void addMessageInfo(String codMensagem) {
        String msg = getMensagem(codMensagem);
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null);
        addMensagemDistinct(msg, facesMessage);
    }

    /**
     * Método que cria uma mensagem de informacao e adiciona no contexto corrente
     * 
     * @param codMensagem
     */
    public void addMessageInfo(String codMensagem, Object[] parametros) {
        String msg = getMensagem(codMensagem);

        String mensagem = MensagemUtils.obterMensagem(msg);
        if (mensagem != null) {
            msg = MessageFormat.format(mensagem, parametros);
        }

        addMessageInfo(msg);
    }

    /**
     * Método que cria uma mensagem de erro e adiciona no contexto corrente
     * 
     * @param msg
     */
    public void addMessageError(Exception e) {
        addMessageError(e.getMessage());
    }

    /**
     * Método que cria uma mensagem de informacao e adiciona no contexto corrente
     * 
     * @param codMensagem
     */
    public void addMessageError(String codMensagem) {
        String msg = getMensagem(codMensagem);
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null);
        addMensagemDistinct(msg, facesMessage);
    }

    /**
     * Método que cria uma mensagem de informacao e adiciona no contexto corrente
     * 
     * @param codMensagem
     */
    public void addMessageError(String codMensagem, Object... parametros) {
        String msg = getMensagem(codMensagem);

        String mensagem = MensagemUtils.obterMensagem(msg);
        if (mensagem != null) {
            msg = MessageFormat.format(mensagem, parametros);
        }

        addMessageError(msg);
    }

    /**
     * Método que cria uma mensagem de alerta e adiciona no contexto corrente
     * 
     * @param codMensagem
     */
    public void addMessageWarn(String codMensagem) {
        String msg = getMensagem(codMensagem);
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, null);
        addMensagemDistinct(msg, facesMessage);
    }

    /**
     * Método que cria uma mensagem de informacao e adiciona no contexto corrente
     * 
     * @param codMensagem
     */
    public void addMessageWarn(String codMensagem, Object... parametros) {
        String msg = getMensagem(codMensagem);

        String mensagem = MensagemUtils.obterMensagem(msg);
        if (mensagem != null) {
            msg = MessageFormat.format(mensagem, parametros);
        }

        addMessageWarn(msg);
    }

    /**
     * Tratamento para verificar se a mensagem já foi inserida no contexto do JSF
     * 
     * @param msg
     * @param facesMessage
     */
    @SuppressWarnings("rawtypes")
    private void addMensagemDistinct(String msg, FacesMessage facesMessage) {
        boolean mensagemNova = true;
        for (Iterator iterator = facesContext.getMessages(); iterator.hasNext();) {
            FacesMessage message = (FacesMessage) iterator.next();

            if (message.getSummary().equals(msg)) {
                mensagemNova = false;
                break;
            }
        }

        // Só permite a inclusão da mensagem no contexto do JSF uma vez.
        if (mensagemNova) {
            facesContext.addMessage(null, facesMessage);
        }
    }

    /**
     * Recupera a mensagem ou do arquivo de propriedades ou a mensagem passada como parâmetro.
     * 
     * @param msg
     *            - {@link String}
     * @return msg tratada - {@link String}
     */
    private static String getMensagem(String msg) {
        // if (msg.startsWith("MN")){
        return MensagemUtils.obterMensagem(msg);
        // }
        // return msg;
    }

    /**
     * Verifica se existe alguma mensagem de erro no {@link FacesMessage}
     * 
     * @return <code>true</code> caso exista, e <code>false</code> caso contrário.
     */
    public boolean possuiMensagemErro() {
        Iterator<FacesMessage> messages = facesContext.getMessages();
        while (messages.hasNext()) {
            FacesMessage msg = messages.next();

            if (msg.getSeverity().equals(FacesMessage.SEVERITY_ERROR)) {
                return true;
            }
        }
        return false;
    }
}
