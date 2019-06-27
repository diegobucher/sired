package br.gov.caixa.gitecsa.service.mail;

import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;

import org.apache.commons.mail.EmailException;
import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.schedule.AbstractSchedule;
import br.gov.caixa.gitecsa.sired.enumerator.AtivoInativoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.UnidadeParametroSistemaEnum;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

@Singleton
@Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class EnviaEmailTimerService extends AbstractSchedule {
  @Inject
  private EnviaEmailRequisicaoService enviaEmailRequisicaoService;

  @Inject
  private EnviaEmailRemessaService enviaEmailRemessaService;

  @Inject
  private ParametroSistemaService parametroSistemaService;

  @Inject
  private Logger logger;

  @Override
  public void scheduleInitialTask() {
    try {
      TimerConfig config = new TimerConfig();
      config.setPersistent(false);

      Timer timer;
      ParametroSistemaVO parametro = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_NU_HORA_ENVIO_EMAIL);
      if (parametro.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.HORAS)) {
        ScheduleExpression exp = this.getScheduleExpressionForHours(parametro.getVlParametroSistema());
        timer = this.timerService.createCalendarTimer(exp, config);
      } else {
        long interval = this.getInterval(parametro);
        timer = this.timerService.createSingleActionTimer(interval, config);
      }

      logger.info(EnviaEmailService.getLogAgendado(timer.getNextTimeout()));
    } catch (DataBaseException e) {
      e.printStackTrace();
      this.logger.error(e.getMessage(), e);
    }
  }

  @Override
  public void scheduleTask() {
    try {
      ParametroSistemaVO parametro = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_INTERVALO_ENVIO_EMAIL);
      long interval = this.getInterval(parametro);

      TimerConfig config = new TimerConfig();
      config.setPersistent(false);

      Timer timer = this.timerService.createSingleActionTimer(interval, config);

      logger.info(EnviaEmailService.getLogAgendado(timer.getNextTimeout()));
    } catch (DataBaseException e) {
      this.logger.error(e.getMessage(), e);
    }
  }

  @Override
  public void doWork() {
    logger.info(EnviaEmailService.getLogInicio());

    ParametroSistemaVO parametroSistemaVO;

    try {

      parametroSistemaVO = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_ENVIAR_EMAIL_SOLICITANTE);
      if (Long.valueOf(parametroSistemaVO.getVlParametroSistema()).equals(AtivoInativoEnum.ATIVO.getValue())) {
        enviaEmailSolicitante();
      } else {
        logger.info(EnviaEmailService.getLogSolicitanteEnvioDesativado());
      }

      parametroSistemaVO = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_ENVIAR_EMAIL_BASE);
      if (Long.valueOf(parametroSistemaVO.getVlParametroSistema()).equals(AtivoInativoEnum.ATIVO.getValue())) {
        enviaEmailBase();
      } else {
        logger.info(EnviaEmailService.getLogBaseEnvioDesativado());
      }

      parametroSistemaVO = this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_ENVIAR_EMAIL_TERCEIRIZADA);
      if (Long.valueOf(parametroSistemaVO.getVlParametroSistema()).equals(AtivoInativoEnum.ATIVO.getValue())) {
        enviaEmailTerceirizada();
      } else {
        logger.info(EnviaEmailService.getLogTerceirizadaEnvioDesativado());
      }
    } catch (DataBaseException e) {
      this.logger.error(e.getMessage(), e);
    }
  }

  private void enviaEmailSolicitante() {
    try {
      enviaEmailRequisicaoService.enviaEmailSolicitante(this.parametroSistemaService);
    } catch (EmailException e) {
      this.logger.error(EnviaEmailService.getLogErroEnvioEmail());
      this.logger.error(e);
    } catch (Exception e) {
      this.logger.error(EnviaEmailService.getLogErroInesperado());
      this.logger.error(e);
      System.err.println(
              "**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
      e.printStackTrace();
      System.err.println(
              "**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
    }
    try {
      enviaEmailRemessaService.enviaEmailSolicitante(this.parametroSistemaService);
    } catch (EmailException e) {
      System.err.println(
          "**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
      e.printStackTrace();
      System.err.println(
          "**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
      this.logger.error(EnviaEmailService.getLogErroEnvioEmail());
      this.logger.error(e);
    } catch (Exception e) {
			System.err
					.println("**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
			e.printStackTrace();
			System.err
					.println("**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
      this.logger.error(EnviaEmailService.getLogErroInesperado());
      this.logger.error(e);
    }
  }

  private void enviaEmailBase() {
    try {
      enviaEmailRequisicaoService.enviaEmailBase(this.parametroSistemaService);
    } catch (EmailException e) {
      this.logger.error(EnviaEmailService.getLogErroEnvioEmail());
      this.logger.error(e);
    } catch (Exception e) {
		System.err.println(
		        "**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
		e.printStackTrace();
		System.err.println(
		        "**-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-***-*----*-***-*-*-*-*-*-*-*");
      this.logger.error(EnviaEmailService.getLogErroInesperado());
      this.logger.error(e);
    }
    try {
      enviaEmailRemessaService.enviaEmailBase(this.parametroSistemaService);
    } catch (EmailException e) {
      this.logger.error(EnviaEmailService.getLogErroEnvioEmail());
      this.logger.error(e);
    } catch (Exception e) {
      this.logger.error(EnviaEmailService.getLogErroInesperado());
      this.logger.error(e);
      // TODO: remover o e.printStackTrace();
      e.printStackTrace();
    }
  }

  private void enviaEmailTerceirizada() {
    try {
      enviaEmailRequisicaoService.enviaEmailTerceirizada(this.parametroSistemaService);
    } catch (EmailException e) {
      this.logger.error(EnviaEmailService.getLogErroEnvioEmail());
      this.logger.error(e);
    } catch (Exception e) {
      this.logger.error(EnviaEmailService.getLogErroInesperado());
      this.logger.error(e);
      // TODO: remover o e.printStackTrace();
      e.printStackTrace();
    }
    try {
      enviaEmailRemessaService.enviaEmailTerceirizada(this.parametroSistemaService);
    } catch (EmailException e) {
      this.logger.error(EnviaEmailService.getLogErroEnvioEmail());
      this.logger.error(e);
    } catch (Exception e) {
      this.logger.error(EnviaEmailService.getLogErroInesperado());
      this.logger.error(e);
      // TODO: remover o e.printStackTrace();
      e.printStackTrace();
    }
  }
}
