package br.gov.caixa.gitecsa.exceptionhandler;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.interceptor.AppLogInterceptor;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.util.JSFUtil;

public class SiredExceptionHandler extends ExceptionHandlerWrapper {

    @Inject
    private transient Logger logger = Logger.getLogger(AppLogInterceptor.class);

    private ExceptionHandler wrapped;

    SiredExceptionHandler(ExceptionHandler exception) {
        this.wrapped = exception;
    }

    @Override
    public ExceptionHandler getWrapped() {
        return wrapped;
    }

    /**
     * Metodo criado pela ISL de Goiania para sobrescrever o escalonador de exceções do JSF e assim interceptar as exceções que ocorrem no
     * lado cliente da cliente.
     * 
     * @author Elielson Alterado por João Teixeira em 21/01/2015.
     */
    @Override
    public void handle() throws FacesException {

        final Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator();
        while (i.hasNext()) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();

            // get the exception from context
            Throwable t = context.getException();

            // Escrever o log de erro e exibir mensagem para o usuário
            try {

                Throwable rootCause = t;
                String mensagem = LogUtils.getDescricaoPadraoPilhaExcecaoLog(rootCause);
                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }
                if (!ObjectUtils.isNullOrEmpty(rootCause) && rootCause.getStackTrace().length > 0) {
                    String nomeFuncionalidade = rootCause.getStackTrace()[0].getClassName();
                    String descricaoEvento = rootCause.getStackTrace()[0].getMethodName();

                    logger.error(LogUtils.getMensagemPadraoLog(mensagem, nomeFuncionalidade, descricaoEvento));
                }

                // Enviando o erro padrão para ser escrito numa JSF message
                JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MA012"));
            } finally {
                // Removendo o erro da lista
                i.remove();
            }
        }
        getWrapped().handle();
    }

}
