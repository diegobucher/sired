package br.gov.caixa.gitecsa.sired.schedule;

import java.text.SimpleDateFormat;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.inject.Inject;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.schedule.AbstractSchedule;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

@Startup
@Singleton
public class ConfirmacaoAutomaticaRemessaSchedule extends AbstractSchedule {

    @Inject
    private ParametroSistemaService parametroService;

    @Inject
    private RemessaService remessaService;

    @Override
    public void scheduleInitialTask() {
        scheduleTask();
    }

    @Override
    public void scheduleTask() {
    	try {
	        ParametroSistemaVO parametro = this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_NU_INTERVALO_CONFIRMACAO);
	
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
            int totalRemessasConfirmadas = this.remessaService.confirmarRemessasPendentesAlteracao();
            
            int totalRemessasFechadas = this.remessaService.fecharRemessasConferidasConfirmadas();

            logger.info(montarLog(totalRemessasConfirmadas + " REMESSAS COM ALTERAÇÕES CONFIRMADAS."));
            logger.info(montarLog(totalRemessasFechadas + " REMESSAS FECHADAS."));
        } catch (DataBaseException | NumberFormatException | BusinessException e) {
            this.logger.error(montarLog("ERRO:"));
            this.logger.error(e.getMessage(), e);
        }
    }

    private static String montarLog(String mensagem) {
        return Constantes.LOG_MARCADOR + Util.getTimestamp() + " BATCH DE CONFIRMACAO AUTOMATICA DE REMESSA: " + mensagem + Constantes.LOG_MARCADOR;
    }
}
