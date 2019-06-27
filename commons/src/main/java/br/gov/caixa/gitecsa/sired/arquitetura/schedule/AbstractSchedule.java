package br.gov.caixa.gitecsa.sired.arquitetura.schedule;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;

public abstract class AbstractSchedule {

    public static final long MILLISECOND = 1000;

    public static final long MINUTES_IN_MILLISECONDS = 60000;

    public static final long HOURS_IN_MILLISECONDS = 3600000;

    public static final long DAYS_IN_MILLISECONDS = 86400000;

    @Resource
    protected TimerService timerService;

    @Inject
    protected transient Logger logger;

    @PostConstruct
    public void start() {
        this.cancelAllTimers();
        this.scheduleInitialTask();
    }

    @Timeout
    public void execute(Timer timer) {
        this.doWork();
        timer.cancel();
        this.scheduleTask();
    }

    public void cancelAllTimers() {
        for (Timer timer : timerService.getTimers()) {
            timer.cancel();
        }
    }

    public Long getInterval(ParametroSistemaVO parametro) {

        long delay = 1;

        switch (parametro.getIcUnidadeParametroSistema()) {
        case SEGUNDOS:
            delay = MILLISECOND;
            break;
        case MINUTOS:
            delay = MINUTES_IN_MILLISECONDS;
            break;
        case HORAS:
            delay = HOURS_IN_MILLISECONDS;
            break;
        default:
            delay = DAYS_IN_MILLISECONDS;
            break;
        }

        return Long.valueOf(parametro.getVlParametroSistema()) * delay;
    }

    /**
     * Caso o agendamento seja para uma hora específica é necessário criar o objeto Schedule pois pode representar uma hora futura
     * no dia atual ou no próximo dia.
     * 
     * @param horaHHMM
     * @return
     */
    public ScheduleExpression getScheduleExpressionForHours(String horaHHMM) {
        ScheduleExpression exp = new ScheduleExpression();
        String[] vHora = horaHHMM.split(":");
        exp.hour(vHora[0]);
        if (vHora.length > 1) {
            exp.minute(vHora[1]);
        }
        return exp;
    }

    public abstract void doWork();

    public abstract void scheduleTask();

    public abstract void scheduleInitialTask();
}
