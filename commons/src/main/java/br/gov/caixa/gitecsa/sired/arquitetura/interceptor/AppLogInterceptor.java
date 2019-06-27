package br.gov.caixa.gitecsa.sired.arquitetura.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

public class AppLogInterceptor {

    @Inject
    private transient Logger logger;
    
    private static final String UNKNOWN_ERROR = "MA012";

    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        context.getParameters();
        Object returnObject = null;

        try {
            returnObject = context.proceed();
        } catch (BusinessException e) {
            throw e;
        } catch (RequiredException e) {
            throw e;
        } catch (DataBaseException e) {
            throw e;
        } catch (Exception e) {
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), context.getTarget().getClass().getSimpleName(), context
                    .getMethod().getName()));

            List<String> listErro = new ArrayList<String>();
            listErro.add(MensagemUtils.obterMensagem(UNKNOWN_ERROR));
            throw new BusinessException(listErro);
        }

        return returnObject;
    }

    @AroundTimeout
    public Object interceptTimeout(InvocationContext context) throws Exception {
        context.getParameters();
        Object returnObject = null;

        try {
            returnObject = context.proceed();
        } catch (BusinessException e) {
            throw e;
        } catch (RequiredException e) {
            throw e;
        } catch (RuntimeException e) {
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), context.getTarget().getClass().getSimpleName(), context
                    .getMethod().getName()));
            
            List<String> listErro = new ArrayList<String>();
            listErro.add(MensagemUtils.obterMensagem(UNKNOWN_ERROR));
            throw new BusinessException(listErro);
        } catch (Exception e) {
            logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), context.getTarget().getClass().getSimpleName(), context
                    .getMethod().getName()));

            List<String> listErro = new ArrayList<String>();
            listErro.add(MensagemUtils.obterMensagem(UNKNOWN_ERROR));
            throw new BusinessException(listErro);
        }

        return returnObject;
    }

}
