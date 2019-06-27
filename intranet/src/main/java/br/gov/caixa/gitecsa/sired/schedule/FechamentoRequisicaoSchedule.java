package br.gov.caixa.gitecsa.sired.schedule;

import java.text.SimpleDateFormat;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.schedule.AbstractSchedule;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class FechamentoRequisicaoSchedule extends AbstractSchedule {

	private static final int TRANSACTION_TIMEOUT = 7200;
	
    @Inject
    private ParametroSistemaService parametroService;

    @Inject
    private RequisicaoService requisicaoService;
    
    @Resource
    private UserTransaction transaction;

    @Override
    public void scheduleInitialTask() {
        scheduleTask();
    }

    @Override
    public void scheduleTask() {
    	try {
	        ParametroSistemaVO parametro = this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_NU_INTERVALO_FECHAMENTO);
	
	        TimerConfig config = new TimerConfig();
	        config.setInfo(this.getClass());
	        config.setPersistent(false);
	
	        long interval = this.getInterval(parametro);
	        Timer timer = this.timerService.createSingleActionTimer(interval, config);
	        String agendamento = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(timer.getNextTimeout());
	        logger.info(montarLog("AGENDADO COM SUCESSO PARA: " + agendamento));
    	} catch (DataBaseException e) {
    		e.printStackTrace();
    		this.logger.error(e.getMessage(), e);
    	}
    }

    @Override
    public void doWork() {
        try {
        	this.transaction.setTransactionTimeout(TRANSACTION_TIMEOUT);
            this.transaction.begin();
            int totalRequisicoesFechadas = this.requisicaoService.fecharRequisicoesPendentesAvaliacao();
            this.transaction.commit();
            
            logger.info(montarLog(totalRequisicoesFechadas + " REQUISIÇÕES FECHADAS."));
        } catch (BusinessException|DataBaseException|SystemException|NotSupportedException|SecurityException|IllegalStateException|RollbackException|HeuristicMixedException|HeuristicRollbackException e) {
            this.logger.error(montarLog("ERRO:"));
            this.logger.error(e.getMessage(), e);
        }
    }

    private static String montarLog(String mensagem) {
        return Constantes.LOG_MARCADOR + Util.getTimestamp() + " BATCH DE FECHAMENTO AUTOMÁTICO DE REQUISIÇÃO: " + mensagem + Constantes.LOG_MARCADOR;
    }
}
