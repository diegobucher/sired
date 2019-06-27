package br.gov.caixa.gitecsa.sired.schedule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.NoMoreTimeoutsException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.UserTransaction;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.schedule.AbstractSchedule;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.siico.service.FeriadoSiicoService;
import br.gov.caixa.gitecsa.sired.siico.service.MunicipioSiicoService;
import br.gov.caixa.gitecsa.sired.siico.service.TipoUnidadeSiicoService;
import br.gov.caixa.gitecsa.sired.siico.service.UnidadeSiicoService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class TabelasBasicasSchedule extends AbstractSchedule {

    private static final int TRANSACTION_TIMEOUT = 7200;

    private static final String REGEX_HORARIO = "\\d{2}:\\d{2}";

    @Inject
    private ParametroSistemaService parametroService;
    
    @Inject
    private TipoUnidadeSiicoService tipoUnidadeSiicoService;
    
    @Inject
    private MunicipioSiicoService municipioSiicoService;
    
    @Inject
    private FeriadoSiicoService feriadoSiicoService;
    
    @Inject
    private UnidadeSiicoService unidadeSiicoService;
    
    @Resource
    private UserTransaction transaction;
    
    private Date dhExecucao;
    
    private ParametroSistemaVO nuHoraAgendamento;
    
    private ParametroSistemaVO nuIntervaloAgendamento;
    
    @Override
    public void scheduleInitialTask() {
        
        try {
        	this.nuHoraAgendamento = this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_NU_HORA_ATUALIZA_TABELA);
            this.nuIntervaloAgendamento = this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_NU_INTERVALO_ATUALIZA_TABELA);
            
            if (Pattern.matches(REGEX_HORARIO, this.nuHoraAgendamento.getVlParametroSistema())) {
                
                ScheduleExpression expression = this.getScheduleExpressionForHours(this.nuHoraAgendamento.getVlParametroSistema());
                
                TimerConfig config = new TimerConfig();
                config.setInfo(this.getClass());
                config.setPersistent(false);

                Timer timer = this.timerService.createCalendarTimer(expression, config);
                this.dhExecucao = timer.getNextTimeout();
                
                String agendamento = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(this.dhExecucao);
                logger.info(montarLog("ATUALIZAÇÃO DAS TABELAS BÁSICAS AGENDADO COM SUCESSO PARA: " + agendamento));
            } else {
                logger.info(montarLog("PARAMETRO SISTEMA INVALIDO. O HORARIO DE ATUALIZACAO DEVE ESTAR NO FORMATO \"HH:mm\""));
            }
        } catch (DataBaseException e) {
        	e.printStackTrace();
        	this.logger.error(e.getMessage(), e);
        }
    }

    @Override
	public void scheduleTask() {

		TimerConfig config = new TimerConfig();
		config.setInfo(this.getClass());
		config.setPersistent(false);

		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(false);
		calendar.setTime(this.dhExecucao);
		calendar.add(Calendar.DAY_OF_MONTH, Integer.valueOf(this.nuIntervaloAgendamento.getVlParametroSistema()));

		Timer timer = this.timerService.createSingleActionTimer(calendar.getTime(), config);
		try {
			this.dhExecucao = timer.getNextTimeout();
			String agendamento = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(this.dhExecucao);
			logger.info(montarLog("ATUALIZAÇÃO DAS TABELAS BÁSICAS AGENDADO COM SUCESSO PARA: " + agendamento));
		} catch (NoMoreTimeoutsException e) {
			calendar.add(Calendar.DAY_OF_MONTH, Integer.valueOf(this.nuIntervaloAgendamento.getVlParametroSistema()));
			logger.info(montarLog("************************* NoMoreTimeoutsException - ATUALIZAÇÃO DAS TABELAS BÁSICAS SWERÁ AGENDADA PARA: "
					+ calendar.getTime()));
			this.dhExecucao = timer.getNextTimeout();
			String agendamento = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(this.dhExecucao);
			logger.info(montarLog("ATUALIZAÇÃO DAS TABELAS BÁSICAS AGENDADO COM SUCESSO PARA: " + agendamento));
		}
	}

    @Override
    public void doWork() {
        this.syncTabelaTipoUnidade();
        this.syncTabelaMunicipio();
        this.syncTabelaUnidade();
        this.syncTabelaFeriado();
    }
    
    private void syncTabelaTipoUnidade() {
        try {
            logger.info(montarLog("ATUALIZANDO TIPO DE UNIDADE"));
            
            this.transaction.setTransactionTimeout(TRANSACTION_TIMEOUT);
            this.transaction.begin();
            String resumo = this.tipoUnidadeSiicoService.sincronizar();
            this.transaction.commit();
            
            logger.info(montarLog(resumo));
            logger.info(montarLog("ATUALIZACAO DO TIPO DE UNIDADE CONCLUIDA"));
        } catch (Exception e) {
            try {
                this.transaction.rollback();
            } catch (Exception e1) {
                this.logger.error(montarLog("ERRO:"));
                this.logger.error(e1.getMessage(), e1);
            }
            this.logger.error(montarLog("ERRO:"));
            this.logger.error(e.getMessage(), e);
        }
    }
    
    private void syncTabelaMunicipio() {
        try {
            logger.info(montarLog("ATUALIZANDO MUNICIPIO"));
            
            this.transaction.setTransactionTimeout(TRANSACTION_TIMEOUT);
            this.transaction.begin();
            String resumo = this.municipioSiicoService.sincronizar();
            this.transaction.commit();
            
            logger.info(montarLog(resumo));
            logger.info(montarLog("ATUALIZACAO DE MUNICIPIO CONCLUIDA"));
        } catch (Exception e) {
            try {
                this.transaction.rollback();
            } catch (Exception e1) {
                this.logger.error(montarLog("ERRO:"));
                this.logger.error(e1.getMessage(), e1);
            }
            this.logger.error(montarLog("ERRO:"));
            this.logger.error(e.getMessage(), e);
        }
    }
    
    private void syncTabelaUnidade() {
        try {
            logger.info(montarLog("ATUALIZANDO UNIDADE"));
            
            this.transaction.setTransactionTimeout(TRANSACTION_TIMEOUT);
            this.transaction.begin();
            String resumo = this.unidadeSiicoService.sincronizar();
            this.transaction.commit();
            
            logger.info(montarLog(resumo));
            logger.info(montarLog("ATUALIZACAO DE UNIDADE CONCLUIDA"));
        } catch (Exception e) {
            try {
                this.transaction.rollback();
            } catch (Exception e1) {
                this.logger.error(montarLog("ERRO:"));
                this.logger.error(e1.getMessage(), e1);
            }
            this.logger.error(montarLog("ERRO:"));
            this.logger.error(e.getMessage(), e);
        }
    }
    
    private void syncTabelaFeriado() {
        try {
            logger.info(montarLog("ATUALIZANDO FERIADO"));
            
            this.transaction.setTransactionTimeout(TRANSACTION_TIMEOUT);
            this.transaction.begin();
            String resumo = this.feriadoSiicoService.sincronizar();
            this.transaction.commit();
            
            logger.info(montarLog(resumo));
            logger.info(montarLog("ATUALIZACAO DE FERIADO CONCLUIDA"));
        } catch (Exception e) {
            try {
                this.transaction.rollback();
            } catch (Exception e1) {
                this.logger.error(montarLog("ERRO:"));
                this.logger.error(e1.getMessage(), e1);
            }
            this.logger.error(montarLog("ERRO:"));
            this.logger.error(e.getMessage(), e);
        }
    }

    private static String montarLog(String mensagem) {
        return Constantes.LOG_MARCADOR + Util.getTimestamp() + " BATCH DE ATUALIZAÇÃO DAS TABELAS BÁSICAS: " + mensagem + Constantes.LOG_MARCADOR;
    }
}
