package br.gov.caixa.gitecsa.interceptor;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.AroundTimeout;
import javax.interceptor.InvocationContext;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.exception.BusinessException;
import br.gov.caixa.gitecsa.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;

public class AppLogInterceptor {

    @Inject
    private transient Logger logger = Logger.getLogger(AppLogInterceptor.class);

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
        } catch (Exception e) {

            logErrorException(e, LogUtils.getNomeFuncionalidade(context.getTarget().getClass().getSimpleName()),
                    LogUtils.getDescricaoOperacao(context.getMethod().getName()));

            List<String> error = new ArrayList<String>();
            error.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            throw new BusinessException(error);
        }

        String nomeFuncao = context.getMethod().getName();
        if (nomeFuncao.startsWith("save") || nomeFuncao.startsWith("salvar") || nomeFuncao.startsWith("update") || nomeFuncao.startsWith("delete")
                || nomeFuncao.startsWith("saveOrUpdate")) {
            logger.info(LogUtils.getMensagemPadraoLogManutencao(LogUtils.getNomeFuncionalidade(context.getTarget().getClass().getSimpleName()),
                    LogUtils.getDescricaoOperacao(context.getMethod().getName())));
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
        } catch (RuntimeException re) {

            logErrorException(re, LogUtils.getNomeFuncionalidade(context.getTarget().getClass().getSimpleName()),
                    LogUtils.getDescricaoOperacao(context.getMethod().getName()));

        } catch (Exception e) {

            logErrorException(e, LogUtils.getNomeFuncionalidade(context.getTarget().getClass().getSimpleName()),
                    LogUtils.getDescricaoOperacao(context.getMethod().getName()));
            List<String> error = new ArrayList<String>();
            error.add(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
            throw new BusinessException(error);
        }

        return returnObject;
    }

    /**
     * Despeja a pilha de exceções/causas de uma Exception no log da aplicação
     */
    public void logErrorException(Exception e, String funcionalidade, String evento) {
        logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), funcionalidade, evento));
    }

}
