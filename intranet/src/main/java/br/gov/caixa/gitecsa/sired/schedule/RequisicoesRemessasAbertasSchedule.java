package br.gov.caixa.gitecsa.sired.schedule;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
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
import javax.mail.MessagingException;
import javax.transaction.UserTransaction;

import org.apache.commons.mail.EmailException;

import br.gov.caixa.gitecsa.service.mail.EnviaEmailRemessaService;
import br.gov.caixa.gitecsa.service.mail.EnviaEmailRequisicaoService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.schedule.AbstractSchedule;
import br.gov.caixa.gitecsa.sired.dto.RemessasAbertasDTO;
import br.gov.caixa.gitecsa.sired.dto.RequisicaoDTO;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Startup
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class RequisicoesRemessasAbertasSchedule extends AbstractSchedule {

  private static final int TRANSACTION_TIMEOUT = 7200;

  private static final String REGEX_HORARIO = "\\d{2}:\\d{2}";

  @Inject
  private ParametroSistemaService parametroService;

  @Inject
  private RequisicaoService requisicaoService;

  @Inject
  private br.gov.caixa.gitecsa.sired.service.RemessaService remessaService;

  @Inject
  private EnviaEmailRequisicaoService enviaEmailRequisicaoService;

  @Inject
  private EnviaEmailRemessaService enviaEmailRemessaService;

  @Resource
  private UserTransaction transaction;

  private Date dhExecucao;

  private ParametroSistemaVO nuHoraAgendamento;

  private ParametroSistemaVO nuIntervaloAgendamento;

  private static final String AGENDAMENTO_COM_SUCESSO = "ENVIO EMAIL REQUISIÇÕES E REMESSAS ABERTAS AGENDADO COM SUCESSO PARA: ";

  private static final String SDF_DD_MM_YYYY = "dd/MM/yyyy HH:mm:ss";

  @Override
  public void scheduleInitialTask() {

    try {
      this.nuHoraAgendamento = this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_NU_HOR_JOB_REQ_REM_ABERTA);
      this.nuIntervaloAgendamento =
          this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_NU_INTERVALO_JOB_REQ_REM_ABERTA);

      if (Pattern.matches(REGEX_HORARIO, this.nuHoraAgendamento.getVlParametroSistema())) {

        ScheduleExpression expression = this.getScheduleExpressionForHours(this.nuHoraAgendamento.getVlParametroSistema());

        TimerConfig config = new TimerConfig();
        config.setInfo(this.getClass());
        config.setPersistent(false);

        Timer timer = this.timerService.createCalendarTimer(expression, config);
        this.dhExecucao = timer.getNextTimeout();

        String agendamento = new SimpleDateFormat(SDF_DD_MM_YYYY).format(this.dhExecucao);
        logger.info(montarLog(AGENDAMENTO_COM_SUCESSO + agendamento));
      } else {
        logger.info(montarLog("PARAMETRO SISTEMA INVALIDO. O HORARIO DE ATUALIZACAO DEVE ESTAR NO FORMATO \"HH:mm\""));
      }
    } catch (DataBaseException e) {
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
      String agendamento = new SimpleDateFormat(SDF_DD_MM_YYYY).format(this.dhExecucao);
      logger.info(montarLog(AGENDAMENTO_COM_SUCESSO + agendamento));
    } catch (NoMoreTimeoutsException e) {
      calendar.add(Calendar.DAY_OF_MONTH, Integer.valueOf(this.nuIntervaloAgendamento.getVlParametroSistema()));
      logger.info(montarLog(
          "************************* NoMoreTimeoutsException - ENVIO DE EMAIL REQUISIÇÕES E REMESSAS ABERTAS AGENDADO PARA: "
              + calendar.getTime()));
      this.dhExecucao = timer.getNextTimeout();
      String agendamento = new SimpleDateFormat(SDF_DD_MM_YYYY).format(this.dhExecucao);
      logger.info(montarLog(AGENDAMENTO_COM_SUCESSO + agendamento));
    }
  }

  @Override
  public void doWork() {
    Calendar cal = Calendar.getInstance();
    cal.setTime(new Date());
    cal.set(Calendar.AM_PM, 0);
    cal.set(Calendar.HOUR, 00);
    cal.set(Calendar.MINUTE, 00);
    cal.set(Calendar.SECOND, 00);
    Date hoje = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    Map<UnidadeVO, RequisicaoDTO> requisicoesList = requisicaoService.pesquisaAbertasHoje(cal.getTime(), hoje);
    if (!requisicoesList.isEmpty()) {
      try {
        enviaEmailRequisicaoService.enviaEmailRequisicoesAbertas(requisicoesList, this.parametroService);
        logger.info(montarLog(" FORAM ENVIADAS " + requisicoesList.size() + " REQUISIÇÕES ABERTAS."));
      } catch (
          EmailException
          | AppException
          | MessagingException
          | IOException e) {
        this.logger.error(e.getMessage(), e);
      }
    } else {
      logger.info(montarLog(" NÃO FORAM ENCONTRADAS REQUISIÇÕES ABERTAS PARA O DIA: " + sdf.format(hoje)));
    }

    Map<UnidadeVO, RemessasAbertasDTO> remessasList = remessaService.pesquisaAbertasHoje(cal.getTime(), hoje);

    if (!remessasList.isEmpty()) {
      try {
        enviaEmailRemessaService.enviaEmailRemessasAbertas(remessasList, this.parametroService);
        logger.info(montarLog(" FORAM ENVIADAS " + remessasList.size() + " REMESSAS ABERTAS."));
      } catch (
          EmailException
          | IOException
          | AppException e) {
        this.logger.error(e.getMessage(), e);
      }
    }else {
      logger.info(montarLog(" NÃO FORAM ENCONTRADAS REMESSAS ABERTAS PARA O DIA: " + sdf.format(hoje)));
    }
  }

  private static String montarLog(String mensagem) {
    return Constantes.LOG_MARCADOR + Util.getTimestamp() + mensagem + Constantes.LOG_MARCADOR;
  }
}
