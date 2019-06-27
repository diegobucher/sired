package br.gov.caixa.gitecsa.service;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBException;
import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import br.gov.caixa.gitecsa.siico.ViewFeriado;
import br.gov.caixa.gitecsa.siico.ViewMunicipio;
import br.gov.caixa.gitecsa.siico.ViewTipoUnidade;
import br.gov.caixa.gitecsa.siico.ViewUnidade;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.enumerator.UnidadeParametroSistemaEnum;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.FeriadoVO;
import br.gov.caixa.gitecsa.sired.vo.MunicipioVO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.TipoUnidadeVO;
import br.gov.caixa.gitecsa.sired.vo.UFVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Singleton
// @Startup
@TransactionManagement(TransactionManagementType.BEAN)
public class AtualizaTabelasBasicasTimerService implements Serializable {

    private static final long serialVersionUID = 8075135008661965910L;

    private static int NENHUM = 0;
    private static int HORA = 1;
    private static int MINUTO = 2;
    private static int SEGUNDO = 3;
    private static int DIA = 4;
    private static int MES = 5;
    private static int ANO = 6;

    private static Long TIME_SEGUNDO = 1000L;
    private static Long TIME_MINUTO = 60 * TIME_SEGUNDO;
    private static Long TIME_HORA = 60 * TIME_MINUTO;
    private static Long TIME_DIA = 24 * TIME_HORA;
    private static Long TIME_MES = 30 * TIME_DIA;
    private static Long TIME_ANO = 365 * TIME_DIA;

    private static final String NU_HORA_ATUALIZA_TABELA = "NU_HORA_ATUALIZA_TABELA";

    @Inject
    private ParametroSistemaService parametroSistemaService;

    @Resource
    private TimerService timerService;

    @Inject
    private AcessoSiicoService acessoSiicoService;

    @Inject
    private Logger logger;

    // @PersistenceUnit(unitName="SiredLocal")
    private EntityManagerFactory factory;

    private EntityManager entityManager;

    private Timer timer;

    private Long intervalo;

    Map<Long, ViewUnidade> mapUnidadesSiico;
    Map<Long, MunicipioVO> mapMunicipios;
    Map<Long, TipoUnidadeVO> mapTiposUnidade;
    Map<Long, UnidadeVO> mapUnidadesSired;
    Map<Long, UnidadeVO> mapUnidadesParaAlterar;
    List<UnidadeVO> unidadesSired;

    // Contadores Municipios
    private int qtdMunicipiosInserido;
    // Contadores Tipos de unidade
    private int qtdTipoUnidadeInserido;
    private int qtdTipoUnidadeAlterado;
    // Contadores Unidades
    private int qtdUnidadesAlterada;
    private int qtdUnidadesInseridas;
    private int qtdUnidadesDesativadas;
    private int qtdUnidadesSemAlteracao;
    private int qtdUnidadesParaAlteracao;
    // Contadores Feriados
    private int qtdFeriadosAlterados;
    private int qtdFeriadosInseridos;

    @PostConstruct
    public void init() {
        try {
            entityManager = factory.createEntityManager();
            EntityTransaction entityTransaction = entityManager.getTransaction();
            entityTransaction.begin();
            agendarTimeOut();
            intervalo = calculaIntervalo();
            entityTransaction.commit();
            entityManager.close();
            logger.info(FileUtils.SYSTEM_EOL + "BATCH DE ATUALIZAÇÃO DAS TABELAS BÁSICAS AGENDADO COM SUCESSO.");
        } catch (RuntimeException re) {

            logErrorException(re, "AtualizaTabelasBasicasTimerService", "init");
            entityManager.close();
        } catch (Exception e) {
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "init");
            entityManager.close();
        }
    }

    private void agendarTimeOut() throws IllegalArgumentException, IllegalStateException, EJBException, DataBaseException {
        timerService.createCalendarTimer(obterScheduleExpression());
    }

    private ScheduleExpression obterScheduleExpression() throws DataBaseException {

        ScheduleExpression exp = new ScheduleExpression();

        ParametroSistemaVO parametroSistema = this.parametroSistemaService.findByNome(NU_HORA_ATUALIZA_TABELA);

        if (!Util.isNullOuVazio(parametroSistema)) {
            String[] parametros = parametroSistema.getVlParametroSistema().split(":");

            if (!Util.isNullOuVazio(parametros[0])) {
                exp.hour(parametros[0]);
            }

            if (parametros.length > 1 && !Util.isNullOuVazio(parametros[1])) {
                exp.minute(parametros[1]);
            } else {
                exp.minute("00");
            }

            if (parametros.length > 2 && !Util.isNullOuVazio(parametros[2])) {
                exp.second(parametros[2]);
            } else {
                exp.second("00");
            }

            return exp;
        }
        return null;
    }

    private void cancelTimer() {
        if (timerService.getTimers() != null) {
            for (Timer timer : timerService.getTimers()) {
                timer.cancel();
            }
        }
    }

    @Timeout
    public void executarTarefa() {
        entityManager = factory.createEntityManager();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        try {
            if (!entityTransaction.isActive()) {
                entityTransaction.begin();
            }

            mapMunicipios = new HashMap<Long, MunicipioVO>();
            mapUnidadesSiico = new HashMap<Long, ViewUnidade>();
            mapTiposUnidade = new HashMap<Long, TipoUnidadeVO>();
            mapUnidadesSired = new HashMap<Long, UnidadeVO>();
            mapUnidadesParaAlterar = new HashMap<Long, UnidadeVO>();
            cancelTimer();
            TimerConfig config = new TimerConfig();
            config.setPersistent(false);
            intervalo = calculaIntervalo();
            timerService.createIntervalTimer(intervalo, intervalo, config);
            if (entityTransaction.isActive() && !entityTransaction.getRollbackOnly()) {
                entityTransaction.commit();
            }
            atualizarTabelasBasicas();

            logger.info(FileUtils.SYSTEM_EOL + "BATCH DE ATUALIZAÇÃO DAS TABELAS BÁSICAS AGENDADO COM SUCESSO.");
            entityManager.close();
        } catch (Exception e) {
            if (entityTransaction.isActive() && entityTransaction.getRollbackOnly()) {
                entityTransaction.rollback();
            }
            entityManager.close();
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "executarTarefa");
        }
    }

    public Long calculaIntervalo() {

        Long retorno = 0L;

        Criteria criteria = entityManager.unwrap(Session.class).createCriteria(ParametroSistemaVO.class);

        criteria.add(Restrictions.eq("noParametroSistema", "NU_INTERVALO_ATUALIZA_TABELA"));

        ParametroSistemaVO parametroSistema = (ParametroSistemaVO) criteria.uniqueResult();

        if (parametroSistema != null) {
            if ((parametroSistema.getIcUnidadeParametroSistema() != null)
                    && (parametroSistema.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.NENHUM))) {
                retorno = new Long(parametroSistema.getVlParametroSistema());
            } else if (parametroSistema.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.HORAS)) {
                retorno = TIME_HORA * new Long(parametroSistema.getVlParametroSistema());
            } else if (parametroSistema.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.MINUTOS)) {
                retorno = TIME_MINUTO * new Long(parametroSistema.getVlParametroSistema());
            } else if (parametroSistema.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.SEGUNDOS)) {
                retorno = TIME_SEGUNDO * new Long(parametroSistema.getVlParametroSistema());
            } else if (parametroSistema.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.DIAS)) {
                retorno = TIME_DIA * new Long(parametroSistema.getVlParametroSistema());
            } else if (parametroSistema.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.MESES)) {
                retorno = TIME_MES * new Long(parametroSistema.getVlParametroSistema());
            } else if (parametroSistema.getIcUnidadeParametroSistema().equals(UnidadeParametroSistemaEnum.ANOS)) {
                retorno = TIME_ANO * new Long(parametroSistema.getVlParametroSistema());
            }
        }

        return retorno;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void atualizarTabelasBasicas() {

        Date dataHoraInicioAtividade;
        Date dataHoraFimAtividade;

        try {

            // Atualizar Tipos Unidade
            dataHoraInicioAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE TIPOS DE UNIDADE (SIICO)"
                    + FileUtils.SYSTEM_EOL + "MENSAGEM: INÍCIO DA ATIVIDADE" + FileUtils.SYSTEM_EOL + "DATA ÍNICIO: "
                    + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + Constantes.LOG_MARCADOR);

            atualizarTipoUnidades();
            dataHoraFimAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE TIPOS DE UNIDADE (SIICO)"
                    + FileUtils.SYSTEM_EOL + "DATA INICIO: " + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL
                    + "DATA FIM: " + Util.formatData(dataHoraFimAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL + "DURAÇÃO: "
                    + Util.calculaTempoEntreDatas(dataHoraInicioAtividade, dataHoraFimAtividade) + FileUtils.SYSTEM_EOL + "TIPOS DE UNIDADE ALTERADOS: "
                    + getQtdTipoUnidadeAlterado() + FileUtils.SYSTEM_EOL + "TIPOS DE UNIDADE INCLUÍDOS: " + getQtdTipoUnidadeInserido()
                    + Constantes.LOG_MARCADOR);

            // Atualizar Municipios
            dataHoraInicioAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: CARREGAR TABELA DE MUNICÍPIOS (SIICO) "
                    + FileUtils.SYSTEM_EOL + "MENSAGEM: INÍCIO DA ATIVIDADE" + FileUtils.SYSTEM_EOL + "DATA ÍNICIO: "
                    + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + Constantes.LOG_MARCADOR);

            atualizarMunicipios();

            dataHoraFimAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: CARREGAR TABELA DE MUNICÍPIOS (SIICO)"
                    + FileUtils.SYSTEM_EOL + "DATA INICIO: " + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL
                    + "DATA FIM: " + Util.formatData(dataHoraFimAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL + "DURAÇÃO: "
                    + Util.calculaTempoEntreDatas(dataHoraInicioAtividade, dataHoraFimAtividade) + FileUtils.SYSTEM_EOL + "MUNICÍPIOS INCLUÍDOS: "
                    + getQtdMunicipiosInserido() + Constantes.LOG_MARCADOR);

            // Atualizar Unidades
            dataHoraInicioAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE UNIDADES (SIICO)"
                    + FileUtils.SYSTEM_EOL + "MENSAGEM: INÍCIO DA ATIVIDADE" + FileUtils.SYSTEM_EOL + "DATA ÍNICIO: "
                    + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + Constantes.LOG_MARCADOR);

            atualizarUnidades();
            dataHoraFimAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE UNIDADES (SIICO)"
                    + FileUtils.SYSTEM_EOL + "DATA INICIO: " + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL
                    + "DATA FIM: " + Util.formatData(dataHoraFimAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL + "DURAÇÃO: "
                    + Util.calculaTempoEntreDatas(dataHoraInicioAtividade, dataHoraFimAtividade) + FileUtils.SYSTEM_EOL + "UNIDADES SEM ALTERAÇÃO: "
                    + getQtdUnidadesSemAlteracao() + FileUtils.SYSTEM_EOL + "UNIDADES ALTERADAS: " + getQtdUnidadesAlterada() + FileUtils.SYSTEM_EOL
                    + "UNIDADES DESATIVADAS: " + getQtdUnidadesDesativadas() + FileUtils.SYSTEM_EOL + "UNIDADES INCLUÍDAS: " + getQtdUnidadesInseridas()
                    + Constantes.LOG_MARCADOR);

            // Atualizar Feriados
            dataHoraInicioAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE FERIADO (SIICO)"
                    + FileUtils.SYSTEM_EOL + "MENSAGEM: INÍCIO DA ATIVIDADE" + FileUtils.SYSTEM_EOL + "DATA ÍNICIO: "
                    + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + Constantes.LOG_MARCADOR);

            atualizarFeriados();
            dataHoraFimAtividade = new Date();

            logger.info(Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE FERIADOS (SIICO)"
                    + FileUtils.SYSTEM_EOL + "DATA INICIO: " + Util.formatData(dataHoraInicioAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL
                    + "DATA FIM: " + Util.formatData(dataHoraFimAtividade, "dd/MM/yyyy HH:mm:ss") + FileUtils.SYSTEM_EOL + "DURAÇÃO: "
                    + Util.calculaTempoEntreDatas(dataHoraInicioAtividade, dataHoraFimAtividade) + FileUtils.SYSTEM_EOL + "FERIADOS ALTERADOS: "
                    + getQtdFeriadosAlterados() + FileUtils.SYSTEM_EOL + "FERIADOS INCLUÍDOS: " + getQtdFeriadosInseridos() + "" + Constantes.LOG_MARCADOR);

        } catch (RuntimeException re) {
            logErrorException(re, "AtualizaTabelasBasicasTimerService", "atualizarTabelasBasicas");

        } catch (Exception e) {
            logger.info(e.getMessage());
        }
    }

    public void setTimerService(TimerService timerService) {
        this.timerService = timerService;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void atualizarTipoUnidades() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException, Exception {

        qtdTipoUnidadeAlterado = 0;
        qtdTipoUnidadeInserido = 0;
        String mensagemLog = "";
        EntityTransaction entityTransaction = entityManager.getTransaction();
        if (!entityTransaction.isActive()) {
            entityTransaction.begin();
        }

        List<ViewTipoUnidade> tipoUnidades = new ArrayList<ViewTipoUnidade>();
        try {
            tipoUnidades = consultaListaTipoUnidades();
        } catch (Exception e) {
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "consultaListaTipoUnidades");
            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE TIPOS DE UNIDADE (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIICO" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            throw new Exception();
        }

        if (Util.isNullOuVazio(tipoUnidades)) {
            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE TIPOS DE UNIDADE (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FONTE DE DADOS INCONSISTENTE - VIEW VAZIA" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            throw new Exception();
        }

        TipoUnidadeVO tipoUnidade = new TipoUnidadeVO();

        try {
            for (ViewTipoUnidade viewTipoUnidade : tipoUnidades) {

                tipoUnidade = entityManager.find(TipoUnidadeVO.class, viewTipoUnidade.getNuTipoUnidade());

                if (tipoUnidade != null) {

                    TipoUnidadeVO tpNew = new TipoUnidadeVO();
                    // atualiza os tipo de dados
                    tpNew = preencherNovoTipoUnidade(viewTipoUnidade);

                    if (!tpNew.equalsViewSiico(tipoUnidade)) {
                        preencherTipoUnidade(tipoUnidade, viewTipoUnidade);
                        entityManager.merge(tipoUnidade);
                        qtdTipoUnidadeAlterado++;
                    }
                } else {
                    // insere novo tipo de dado
                    tipoUnidade = new TipoUnidadeVO();
                    tipoUnidade = preencherNovoTipoUnidade(viewTipoUnidade);
                    entityManager.persist(tipoUnidade);
                    qtdTipoUnidadeInserido++;
                }
                mapTiposUnidade.put((Long) tipoUnidade.getId(), tipoUnidade);

            }
            entityTransaction.commit();
        } catch (Exception e) {
            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE TIPOS DE UNIDADE (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIRED" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "atualizarTipoUnidades");
            if (entityTransaction.isActive() && entityTransaction.getRollbackOnly()) {
                entityTransaction.rollback();
            }
            throw new Exception();
        }

    }

    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void atualizarUnidades() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException, Exception {

        qtdUnidadesAlterada = 0;
        qtdUnidadesDesativadas = 0;
        qtdUnidadesInseridas = 0;
        qtdUnidadesSemAlteracao = 0;
        qtdUnidadesParaAlteracao = 0;
        unidadesSired = new ArrayList<UnidadeVO>();
        String mensagemLog = "";
        EntityTransaction entityTransaction = entityManager.getTransaction();
        if (!entityTransaction.isActive()) {
            entityTransaction.begin();
        }

        List<ViewUnidade> unidades = new ArrayList<ViewUnidade>();

        try {

            unidades = consultaListaUnidades();

        } catch (Exception e) {

            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE UNIDADES (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIICO" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "consultaListaUnidades");
            throw new Exception();
        }

        if (Util.isNullOuVazio(unidades)) {
            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE UNIDADES (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FONTE DE DADOS INCONSISTENTE - VIEW VAZIA" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            throw new Exception();
        }

        UnidadeVO unidade = null;

        try {
            unidadesSired = consultarUnidadesSired();
            for (ViewUnidade viewUnidade : unidades) {

                unidade = entityManager.find(UnidadeVO.class, viewUnidade.getNuUnidade());

                if (unidade != null) {
                    UnidadeVO unidadeNova = new UnidadeVO();
                    unidadeNova = preencherNovaUnidade(viewUnidade);
                    if (!unidadeNova.equalsViewSiico(unidade)) {
                        preencherUnidade(unidade, viewUnidade);
                        entityManager.merge(unidade);
                        qtdUnidadesAlterada++;
                    } else {
                        qtdUnidadesSemAlteracao++;
                    }
                } else {
                    unidade = preencherNovaUnidade(viewUnidade);
                    entityManager.persist(unidade);
                    qtdUnidadesInseridas++;
                }
                mapUnidadesSired.put((Long) unidade.getId(), unidade);
            }
        } catch (Exception e) {

            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE UNIDADES (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIRED" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "atualizarUnidades");
            if (entityTransaction.isActive() && entityTransaction.getRollbackOnly()) {
                entityTransaction.rollback();
            }
            throw new Exception();
        }

        // Percorrer map de unidades a Alterar para atualizar unidade
        // subordinada.
        try {
            for (Map.Entry<Long, UnidadeVO> par : mapUnidadesParaAlterar.entrySet()) {
                unidade = entityManager.find(UnidadeVO.class, par.getKey());
                ViewUnidade view = mapUnidadesSiico.get(par.getKey());
                unidade.setUnidadeVinculadora(mapUnidadesSired.get(view.getNuUnidadeSub()));
                entityManager.merge(unidade);
            }
        } catch (Exception e) {

            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE UNIDADES (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIRED" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "atualizarUnidades");
            if (entityTransaction.isActive() && entityTransaction.getRollbackOnly()) {
                entityTransaction.rollback();
            }
            throw new Exception();
        }

        try {
            // verificar unidades a serem desativadas, ou seja, existem no SIRED
            // mas não existem mais no SIICO.
            Criteria criteria = entityManager.unwrap(Session.class).createCriteria(UnidadeVO.class);

            List<UnidadeVO> unidadesSired = criteria.list();

            for (UnidadeVO unidadeVO : unidadesSired) {
                // Se não encontrar a unidade no SIICO então deve desativar essa
                // unidade no SIRED
                if ((!mapUnidadesSiico.containsKey(unidadeVO.getId())) && !unidadeVO.getIcAtivo().equals(0)) {
                    unidadeVO.setIcAtivo(0);
                    entityManager.merge(unidadeVO);
                    qtdUnidadesDesativadas++;
                }
            }
            entityTransaction.commit();
        } catch (Exception e) {

            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE UNIDADES (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIRED" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "atualizarUnidades");
            if (entityTransaction.isActive() && entityTransaction.getRollbackOnly()) {
                entityTransaction.rollback();
            }
            throw new Exception();
        }

    }

    @SuppressWarnings("unchecked")
    @TransactionAttribute(TransactionAttributeType.NEVER)
    private List<UnidadeVO> consultarUnidadesSired() throws Exception {
        StringBuilder hql = new StringBuilder();

        hql.append(" SELECT unidade FROM UnidadeVO unidade ");
        Query query = entityManager.createQuery(hql.toString(), UnidadeVO.class);

        return query.getResultList();
    }

    private void preencherTipoUnidade(TipoUnidadeVO tipoUnidade, ViewTipoUnidade viewTipoUnidade) {
        tipoUnidade.setDescricao(viewTipoUnidade.getDeTipoUnidade());
        tipoUnidade.setIndicadorUnidade(viewTipoUnidade.getIcUnidade());
        tipoUnidade.setSiglaTipoUnidade(viewTipoUnidade.getSgTipoUnidade());
    }

    private TipoUnidadeVO preencherNovoTipoUnidade(ViewTipoUnidade viewTipoUnidade) {
        TipoUnidadeVO tipoUnidade = new TipoUnidadeVO();
        tipoUnidade.setId(viewTipoUnidade.getNuTipoUnidade());
        tipoUnidade.setDescricao(viewTipoUnidade.getDeTipoUnidade());
        tipoUnidade.setIndicadorUnidade(viewTipoUnidade.getIcUnidade());
        tipoUnidade.setSiglaTipoUnidade(viewTipoUnidade.getSgTipoUnidade());

        return tipoUnidade;
    }

    private void preencherUnidade(UnidadeVO unidade, ViewUnidade viewUnidade) {
        unidade.setNome(viewUnidade.getNoUnidade());
        unidade.setSiglaUnidade(viewUnidade.getSgUnidade());
        unidade.setSiglaLocalizacao(viewUnidade.getSgLocalizacao());
        unidade.setIcAtivo(1);// colocar sempre ativo

        if (!Util.isNullOuVazio(viewUnidade.getNuTpUnidade()) && mapTiposUnidade.containsKey(viewUnidade.getNuTpUnidade())) {
            unidade.setTipoUnidade(mapTiposUnidade.get(viewUnidade.getNuTpUnidade()));
        }

        UnidadeVO unidadePai = new UnidadeVO();
        if (!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub())) {
            unidadePai.setId(viewUnidade.getNuUnidadeSub());
        }
        if (!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub()) && !Util.isNullOuVazio(unidadesSired)
                && mapUnidadesSiico.containsKey(viewUnidade.getNuUnidadeSub()) && unidadesSired.contains(unidadePai)) {

            int index = unidadesSired.indexOf(unidadePai);
            unidade.setUnidadeVinculadora(unidadesSired.get(index));

        } else if (!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub()) && mapUnidadesSiico.containsKey(viewUnidade.getNuUnidadeSub())
                && mapUnidadesSired.containsKey(viewUnidade.getNuUnidadeSub())) {
            unidade.setUnidadeVinculadora(mapUnidadesSired.get(viewUnidade.getNuUnidadeSub()));
        } else if (((!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub())) && Util.isNullOuVazio(unidade.getUnidadeVinculadora()))
                || ((!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub())) && (!viewUnidade.getNuUnidadeSub().equals(unidade.getUnidadeVinculadora().getId())))) {
            if (!mapUnidadesParaAlterar.containsKey(viewUnidade.getNuUnidadeSub())) {
                mapUnidadesParaAlterar.put((Long) unidade.getId(), unidade);
            }
        }

        if (!Util.isNullOuVazio(viewUnidade.getUf())) {
            unidade.setUf(new UFVO());
            unidade.getUf().setId(viewUnidade.getUf());
        }

        if (!Util.isNullOuVazio(viewUnidade.getNuLocalidade()) && mapMunicipios.containsKey(viewUnidade.getNuLocalidade())) {
            unidade.setMunicipio(new MunicipioVO());
            unidade.getMunicipio().setId(viewUnidade.getNuLocalidade());
        }

    }

    private UnidadeVO preencherNovaUnidade(ViewUnidade viewUnidade) {

        UnidadeVO unidade = new UnidadeVO();
        unidade.setId(viewUnidade.getNuUnidade());
        unidade.setIcAtivo(1); // colocar sempre ativo
        unidade.setNome(viewUnidade.getNoUnidade());
        unidade.setSiglaUnidade(viewUnidade.getSgUnidade()); // pode vir nulo
        unidade.setSiglaLocalizacao(viewUnidade.getSgLocalizacao()); // pode vir
                                                                     // nulo

        if (!Util.isNullOuVazio(viewUnidade.getNuTpUnidade()) && mapTiposUnidade.containsKey(viewUnidade.getNuTpUnidade())) {
            unidade.setTipoUnidade(mapTiposUnidade.get(viewUnidade.getNuTpUnidade()));
        }

        if (!Util.isNullOuVazio(viewUnidade.getUf())) {
            unidade.setUf(new UFVO());
            unidade.getUf().setId(viewUnidade.getUf());
        }

        if (!Util.isNullOuVazio(viewUnidade.getNuLocalidade()) && mapMunicipios.containsKey(viewUnidade.getNuLocalidade())) {
            unidade.setMunicipio(new MunicipioVO());
            unidade.setMunicipio(mapMunicipios.get(viewUnidade.getNuLocalidade()));
        }

        UnidadeVO unidadePai = new UnidadeVO();
        if (!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub())) {
            unidadePai.setId(viewUnidade.getNuUnidadeSub());
        }
        if (!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub()) && !Util.isNullOuVazio(unidadesSired)
                && mapUnidadesSiico.containsKey(viewUnidade.getNuUnidadeSub()) && unidadesSired.contains(unidadePai)) {

            int index = unidadesSired.indexOf(unidadePai);
            unidade.setUnidadeVinculadora(unidadesSired.get(index));

        } else if (!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub()) && mapUnidadesSiico.containsKey(viewUnidade.getNuUnidadeSub())
                && mapUnidadesSired.containsKey(viewUnidade.getNuUnidadeSub())) {
            unidade.setUnidadeVinculadora(mapUnidadesSired.get(viewUnidade.getNuUnidadeSub()));
        } else if (!Util.isNullOuVazio(viewUnidade.getNuUnidadeSub())) {
            if (!mapUnidadesParaAlterar.containsKey(viewUnidade.getNuUnidadeSub())) {
                mapUnidadesParaAlterar.put((Long) unidade.getId(), unidade);
            }
        }

        return unidade;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    private void atualizarFeriados() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException, Exception {

        List<ViewFeriado> feriados = new ArrayList<ViewFeriado>();
        qtdFeriadosAlterados = 0;
        qtdFeriadosInseridos = 0;
        String mensagemLog = "";
        EntityTransaction entityTransaction = entityManager.getTransaction();
        if (!entityTransaction.isActive()) {
            entityTransaction.begin();
        }

        try {

            feriados = consultaListaFeriados();

        } catch (Exception e) {

            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE FERIADOS (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIICO" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "consultaListaFeriados");
            throw new Exception();
        }

        if (Util.isNullOuVazio(feriados)) {
            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE FERIADOS (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FONTE DE DADOS INCONSISTENTE - VIEW VAZIA" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            throw new Exception();
        }

        FeriadoVO feriado = null;

        try {
            for (ViewFeriado viewFeriado : feriados) {

                feriado = entityManager.find(FeriadoVO.class, viewFeriado.getNuFeriado());

                if (feriado != null) {
                    FeriadoVO feriadoNovo = new FeriadoVO();
                    feriadoNovo = preencherNovoFeriado(viewFeriado);
                    if (!feriadoNovo.equalsViewSiico(feriado)) {
                        preencherFeriado(feriado, viewFeriado);
                        entityManager.merge(feriado);
                        qtdFeriadosAlterados++;
                    }
                } else {
                    feriado = preencherNovoFeriado(viewFeriado);
                    entityManager.persist(feriado);
                    qtdFeriadosInseridos++;
                }
            }
            entityTransaction.commit();

        } catch (Exception e) {

            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: ATUALIZAR TABELA DE FERIADOS (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIRED" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "atualizarFeriados");
            if (entityTransaction.isActive() && !entityTransaction.getRollbackOnly()) {
                entityTransaction.rollback();
            }
            throw new Exception();
        }
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void persistirFeriado(ViewFeriado viewFeriado) {
        FeriadoVO feriado = new FeriadoVO();
        feriado.setId(viewFeriado.getNuFeriado());
        feriado.setData(viewFeriado.getDtFeriado());
        feriado.setModalidade(viewFeriado.getIcModalidade());
        feriado.setTsInclusao(viewFeriado.getTsInclusao());
        feriado.setOrigem("A");

        if (!Util.isNullOuVazio(viewFeriado.getSgUf())) {
            feriado.setUf(new UFVO());
            feriado.getUf().setId(viewFeriado.getSgUf());
        }

        if (!Util.isNullOuVazio(viewFeriado.getNuLocalidade()) && mapMunicipios.containsKey(viewFeriado.getNuLocalidade())) {
            feriado.setMunicipio(mapMunicipios.get(viewFeriado.getNuLocalidade()));
        }
        entityManager.persist(feriado);
    }

    private void preencherFeriado(FeriadoVO feriado, ViewFeriado viewFeriado) {

        feriado.setData(viewFeriado.getDtFeriado());
        feriado.setModalidade(viewFeriado.getIcModalidade());
        feriado.setTsInclusao(viewFeriado.getTsInclusao());
        feriado.setOrigem("A");

        if (!Util.isNullOuVazio(viewFeriado.getSgUf())) {
            feriado.setUf(new UFVO());
            feriado.getUf().setId(viewFeriado.getSgUf());
        }

        if (!Util.isNullOuVazio(viewFeriado.getNuLocalidade()) && mapMunicipios.containsKey(viewFeriado.getNuLocalidade())) {
            feriado.setMunicipio(mapMunicipios.get(viewFeriado.getNuLocalidade()));
        }

    }

    private FeriadoVO preencherNovoFeriado(ViewFeriado viewFeriado) {
        FeriadoVO feriado = new FeriadoVO();

        feriado.setId(viewFeriado.getNuFeriado());
        feriado.setData(viewFeriado.getDtFeriado());
        feriado.setModalidade(viewFeriado.getIcModalidade());
        feriado.setTsInclusao(viewFeriado.getTsInclusao());
        feriado.setOrigem("A");

        if (!Util.isNullOuVazio(viewFeriado.getSgUf())) {
            feriado.setUf(new UFVO());
            feriado.getUf().setId(viewFeriado.getSgUf());
        }

        if (!Util.isNullOuVazio(viewFeriado.getNuLocalidade()) && mapMunicipios.containsKey(viewFeriado.getNuLocalidade())) {
            feriado.setMunicipio(mapMunicipios.get(viewFeriado.getNuLocalidade()));
        }

        return feriado;
    }

    @TransactionAttribute(TransactionAttributeType.NEVER)
    private void atualizarMunicipios() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException,
            HeuristicMixedException, HeuristicRollbackException, Exception {

        qtdMunicipiosInserido = 0;
        String mensagemLog = "";
        List<ViewMunicipio> municipios = new ArrayList<ViewMunicipio>();
        EntityTransaction entityTransaction = entityManager.getTransaction();
        if (!entityTransaction.isActive()) {
            entityTransaction.begin();
        }

        try {

            municipios = consultaListaMunicipios();

        } catch (Exception e) {

            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: CARREGAR TABELA DE MUNICÍPIOS (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIICO" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "consultaListaMunicipios");
            throw new Exception();
        }

        if (Util.isNullOuVazio(municipios)) {
            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: CARREGAR TABELA DE MUNICÍPIOS (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FONTE DE DADOS INCONSISTENTE - VIEW VAZIA" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            throw new Exception();
        }

        MunicipioVO municipio = null;

        try {

            for (ViewMunicipio viewMunicipio : municipios) {

                municipio = entityManager.find(MunicipioVO.class, viewMunicipio.getNuLocalidade());

                if (municipio != null) {
                    MunicipioVO municipioNovo = new MunicipioVO();
                    municipioNovo = preencherNovoMunicipio(viewMunicipio);
                    if (!municipioNovo.equalsViewSiico(municipio)) {
                        preencherMunicipio(municipio, viewMunicipio);
                        entityManager.merge(municipio);
                    }
                } else {
                    municipio = preencherNovoMunicipio(viewMunicipio);
                    entityManager.persist(municipio);
                    qtdMunicipiosInserido++;
                }
                mapMunicipios.put((Long) municipio.getId(), municipio);

            }
            entityTransaction.commit();
        } catch (Exception e) {
            mensagemLog = Constantes.LOG_MARCADOR + "ATIVIDADE AGENDADA" + FileUtils.SYSTEM_EOL + "DATA: " + Util.formatData(new Date(), "dd/MM/yyyy HH:mm:ss")
                    + FileUtils.SYSTEM_EOL + "EVENTO: CARREGAR TABELA DE MUNICÍPIOS (SIICO)" + FileUtils.SYSTEM_EOL
                    + "MENSAGEM: FALHA AO CONECTAR O BANCO DE DADOS DO SIRED" + Constantes.LOG_MARCADOR;
            logger.info(mensagemLog);
            logErrorException(e, "AtualizaTabelasBasicasTimerService", "atualizarMunicipios");
            if (entityTransaction.isActive() && entityTransaction.getRollbackOnly()) {
                entityTransaction.rollback();
            }
            throw new Exception();
        }
    }

    private void preencherMunicipio(MunicipioVO municipio, ViewMunicipio viewMunicipio) {

        municipio.setNome(viewMunicipio.getNoLocalidade());

        if (!Util.isNullOuVazio(viewMunicipio.getSgUf())) {
            municipio.setUf(new UFVO());
            municipio.getUf().setId(viewMunicipio.getSgUf());
        }

    }

    private MunicipioVO preencherNovoMunicipio(ViewMunicipio viewMunicipio) {

        MunicipioVO municipio = new MunicipioVO();
        municipio.setId(viewMunicipio.getNuLocalidade());
        municipio.setNome(viewMunicipio.getNoLocalidade());

        if (!Util.isNullOuVazio(viewMunicipio.getSgUf())) {
            municipio.setUf(new UFVO());
            municipio.getUf().setId(viewMunicipio.getSgUf());
        }

        return municipio;
    }

    /**
     * Loga o erro de uma Exception no log da aplicação
     */
    public void logErrorException(Exception e, String funcionalidade, String evento) {

        logger.error(LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), funcionalidade, evento));
    }

    private List<ViewTipoUnidade> consultaListaTipoUnidades() throws SQLException, Exception {
        return acessoSiicoService.findAllTipoUnidade();
    }

    private List<ViewUnidade> consultaListaUnidades() throws SQLException, Exception {
        List<ViewUnidade> lista = acessoSiicoService.findAllUnidade();
        for (ViewUnidade viewUnidade : lista) {
            mapUnidadesSiico.put(viewUnidade.getNuUnidade(), viewUnidade);
        }
        return lista;
    }

    private List<ViewFeriado> consultaListaFeriados() throws SQLException, Exception {
        return acessoSiicoService.findAllFeriado();
    }

    private List<ViewMunicipio> consultaListaMunicipios() throws SQLException, Exception {
        return acessoSiicoService.findAllMunicipio();
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public int getQtdTipoUnidadeInserido() {
        return qtdTipoUnidadeInserido;
    }

    public void setQtdTipoUnidadeInserido(int qtdTipoUnidadeInserido) {
        this.qtdTipoUnidadeInserido = qtdTipoUnidadeInserido;
    }

    public int getQtdTipoUnidadeAlterado() {
        return qtdTipoUnidadeAlterado;
    }

    public void setQtdTipoUnidadeAlterado(int qtdTipoUnidadeAlterado) {
        this.qtdTipoUnidadeAlterado = qtdTipoUnidadeAlterado;
    }

    public int getQtdUnidadesAlterada() {
        return qtdUnidadesAlterada;
    }

    public void setQtdUnidadesAlterada(int qtdUnidadesAlterada) {
        this.qtdUnidadesAlterada = qtdUnidadesAlterada;
    }

    public int getQtdUnidadesInseridas() {
        return qtdUnidadesInseridas;
    }

    public void setQtdUnidadesInseridas(int qtdUnidadesInseridas) {
        this.qtdUnidadesInseridas = qtdUnidadesInseridas;
    }

    public int getQtdUnidadesDesativadas() {
        return qtdUnidadesDesativadas;
    }

    public void setQtdUnidadesDesativadas(int qtdUnidadesDesativadas) {
        this.qtdUnidadesDesativadas = qtdUnidadesDesativadas;
    }

    public int getQtdUnidadesSemAlteracao() {
        return qtdUnidadesSemAlteracao;
    }

    public void setQtdUnidadesSemAlteracao(int qtdUnidadesSemAlteracao) {
        this.qtdUnidadesSemAlteracao = qtdUnidadesSemAlteracao;
    }

    public int getQtdMunicipiosInserido() {
        return qtdMunicipiosInserido;
    }

    public void setQtdMunicipiosInserido(int qtdMunicipiosInserido) {
        this.qtdMunicipiosInserido = qtdMunicipiosInserido;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public int getQtdFeriadosAlterados() {
        return qtdFeriadosAlterados;
    }

    public void setQtdFeriadosAlterados(int qtdFeriadosAlterados) {
        this.qtdFeriadosAlterados = qtdFeriadosAlterados;
    }

    public int getQtdFeriadosInseridos() {
        return qtdFeriadosInseridos;
    }

    public void setQtdFeriadosInseridos(int qtdFeriadosInseridos) {
        this.qtdFeriadosInseridos = qtdFeriadosInseridos;
    }

    public Long getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(Long intervalo) {
        this.intervalo = intervalo;
    }

    public int getQtdUnidadesParaAlteracao() {
        return qtdUnidadesParaAlteracao;
    }

    public void setQtdUnidadesParaAlteracao(int qtdUnidadesParaAlteracao) {
        this.qtdUnidadesParaAlteracao = qtdUnidadesParaAlteracao;
    }

}
