package br.gov.caixa.gitecsa.sired.service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Years;
import org.primefaces.model.UploadedFile;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequisicaoLoteException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequisicaoTransactionException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dao.RequisicaoDAO;
import br.gov.caixa.gitecsa.sired.dto.FiltroRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.dto.RequisicaoDTO;
import br.gov.caixa.gitecsa.sired.dto.RequisicaoLoteDTO;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRequisicaoDTO;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.GrupoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoAgrupamentoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoUnidadeEnum;
import br.gov.caixa.gitecsa.sired.helper.RequisicaoHelper;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.SiredUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.AvaliacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.ParametroSistemaVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Stateless(name = "requisicaoDocumentoService")
public class RequisicaoService extends PaginatorService<RequisicaoVO> {

    private static final String USUARIO_SIRED = "SIRED";

    private static final String REGEX_REGRA_DEVOLUCAO_CHEQUES = "(DEV|DEVOL[(VIDO)(UCAO)(UÇÃO)])";

    private static final long serialVersionUID = 8912638729588347322L;

    private static final String DATA_MARCO_SICCV = "2011-05-20";

    private static final int PRAZO_SOLICITACAO_EXTRATO = 12;

    @Inject
    private RequisicaoDAO dao;

    @Inject
    private UnidadeService unidadeService;

    @Inject
    private FeriadoService feriadoService;

    @Inject
    private BaseService baseService;

    @Inject
    private TramiteRequisicaoService tramiteService;

    @Inject
    private AvaliacaoRequisicaoService avaliacaoService;

    @Inject
    private SequencialRequisicaoService sequencialService;

    @Inject
    private ParametroSistemaService parametroService;

    @Inject
    private TipoDemandaService demandaService;
    
    @Resource
    private EJBContext ejbContext;
    
    private HashMap<UnidadeVO, RequisicaoDTO> mapRequisicao;

    @Override
    protected void validaCamposObrigatorios(RequisicaoVO entity) {

    }

    @Override
    protected void validaRegras(RequisicaoVO entity) {

    }

    @Override
    protected void validaRegrasExcluir(RequisicaoVO entity) {

    }

    @Override
    protected GenericDAO<RequisicaoVO> getDAO() {
        return this.dao;
    }

    public List<RequisicaoVO> pesquisar(Map<String, Object> filters) throws DataBaseException {
        return this.pesquisar(null, null, filters);
    }

    @Override
    public List<RequisicaoVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) throws DataBaseException {

        FiltroRequisicaoDTO filtro = (FiltroRequisicaoDTO) filters.get("filtroDTO");

        if (ObjectUtils.isNullOrEmpty(filtro.getUnidadeSolicitante()) && !JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)
                && !JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR)) {

            UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
            List<UnidadeVO> unidades = unidadeService.findAllByPerfil(usuario);

            if (JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)) {
                filtro.setMatriculaUsuario(usuario.getNuMatricula());
            }

            return (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) ? this.dao.consultar(filtro, unidades, offset, limit) : this.dao
                    .consultar(filtro, unidades);
        }

        return (!ObjectUtils.isNullOrEmpty(offset) && !ObjectUtils.isNullOrEmpty(limit)) ? this.dao.consultar(filtro, offset, limit) : this.dao
                .consultar(filtro);
    }

    @Override
    public Integer count(Map<String, Object> filters) throws DataBaseException {

        FiltroRequisicaoDTO filtro = (FiltroRequisicaoDTO) filters.get("filtroDTO");

        if (ObjectUtils.isNullOrEmpty(filtro.getUnidadeSolicitante()) && !JSFUtil.isPerfil(Constantes.PERFIL_GESTOR)
                && !JSFUtil.isPerfil(Constantes.PERFIL_AUDITOR)) {

            UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
            List<UnidadeVO> unidades = unidadeService.findAllByPerfil(usuario);

            return this.dao.count(filtro, unidades);
        }

        return this.dao.count(filtro);
    }

    public RequisicaoVO findByIdEager(Long id) {
        return this.dao.findByIdEager(id);
    }
    
    public RequisicaoVO findByCodigo(Long codigo) {
        return this.dao.findByCodigo(codigo);
    }

    /**
     * 
     * AD01558 3.1.7.9
     * 
     * O prazo para atendimento às requisições de documentos arquivados na Base de Arquivo é de:
     * 
     * Tipos A e B (Data de Geração do Documento > Parâmetro PZ_DOCUMENTOS_TIPO_C), Demandas diferentes de NORMAL: Até 05 dias úteis.
     * 
     * Tipos A e B (Data de Geração do Documento > Parâmetro PZ_DOCUMENTOS_TIPO_C), Demanda NORMAL: Até 15 dias úteis.
     * 
     * Tipo C (Data de Geração do Documento <= Parâmetro PZ_DOCUMENTOS_TIPO_C): Até 05 dias úteis.
     * 
     * RN010 - Regras para calcular o prazo de atendimento de requisição de documentos Tipo C (Não Setorial) O prazo para atendimento da
     * requisição de documentos Tipo C (Não Setorial) é de:
     * 
     * Vinte e quatro (24) horas, a partir da solicitação, condicionado à data de recebimento do movimento diário – para os documentos de
     * caixa com até 90 dias;
     * 
     * Cinco (5) dias úteis, a partir da solicitação, condicionado à data de recebimento do movimento diário – para documentos de caixa com
     * mais de 90 dias (nesse caso, o documento passa a ser classificado como do Tipo A).
     * 
     * @param requisicao
     * @return Um objeto Date contendo o prazo para atendimento
     * @throws DataBaseException 
     */
    public Date calcularPrazoAtendimento(RequisicaoVO requisicao) throws DataBaseException {
        ParametroSistemaVO parametro = this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_PZ_DOCUMENTOS_TIPO_C);

        TipoDocumentoEnum setorialOuNao = TipoDocumentoEnum.NAOSETORIAL;
        if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getDataGeracao())) {
            int diferenca = Math.abs(DateUtils.diff(Calendar.getInstance().getTime(), requisicao.getRequisicaoDocumento().getDataGeracao()));
            if (diferenca > Integer.valueOf(parametro.getVlParametroSistema())) { // SETORIAL
                setorialOuNao = TipoDocumentoEnum.SETORIAL;
            }
        }

        TipoDemandaVO tipoDemanda = demandaService.findByNomeESetorial(requisicao.getRequisicaoDocumento().getTipoDemanda().getNome(), setorialOuNao);
        return this.feriadoService.findProximaDataUtil(new Date(), tipoDemanda.getPrazoDias(), requisicao.getRequisicaoDocumento().getUnidadeGeradora());
    }

    /**
     * Realiza a validação do grupo do documento conforme regras descritas no documento suplementar: SIRED_Espec_Validação_Campos.doc 3.2.
     * Grupo CHEQUE COMPENSADO, Campo Data, UCs Abre e Edita Requisição 3.3. Grupos CHEQUE BOCA DE CAIXA e CHEQUE COMPENSADO, Campo
     * Observação, UCs Mantém Requisição 3.4. Grupo EXTRATO, Campo MES_ANO_INICIO, MES_ANO_FIM, UCs Mantém Requisição
     * 
     * @param documento
     * @throws BusinessException
     */
    public void validarGrupoDocumentoRequisicao(RequisicaoVO requisicao) throws BusinessException {

        String nomeGrupo = requisicao.getDocumento().getGrupo().getNome();

        // Caso o usuário solicite a inserção ou alteração de uma requisição cujo grupo do documento seja CHEQUE COMPENSADO, o sistema fará a
        // seguinte validação: caso a data informada pelo usuário seja maior ou igual à 20/05/2011, o sistema deve exibir a MA016 e não permitir a
        // continuidade da operação.
        if (nomeGrupo.equalsIgnoreCase(GrupoDocumentoEnum.CHEQUE_COMPENSADO.getLabel())) {
            if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getDataGeracao())) {

                Date dataMarco = DateUtils.tryParse(DATA_MARCO_SICCV, new Date());

                if (!requisicao.getRequisicaoDocumento().getDataGeracao().before(dataMarco)) {
                    throw new BusinessException(MensagemUtils.obterMensagem("MA016"));
                }
            }
            // Esta validação está relacionada ao campo DE_OBSERVACAO da tabela REDTBC15_REQUISICAO_DOCUMENTO.
            // Caso o usuário solicite a inserção ou alteração de uma requisição cujo grupo do documento seja CHEQUE BOCA DE CAIXA ou CHEQUE
            // COMPENSADO, o sistema deve procurar no campo Observação, a ocorrência do termo “DEV”, independente de ser parte maiúscula ou minúscula ou
            // parcial, ou seja, deve verificar ocorrências como “Dev”, “dev”, “devolvido”, “devolução”, etc. Caso exista tal termo o sistema deve exibir a
            // mensagem MA015 e não permitir a continuidade da operação.
        } else if (nomeGrupo.equalsIgnoreCase(GrupoDocumentoEnum.CHEQUE_COMPENSADO.getLabel())
                || nomeGrupo.equalsIgnoreCase(GrupoDocumentoEnum.CHEQUE_BOCA_DE_CAIXA.getLabel())) {

            Pattern re = Pattern.compile(REGEX_REGRA_DEVOLUCAO_CHEQUES, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
            Matcher m = re.matcher(requisicao.getRequisicaoDocumento().getObservacao());

            if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getObservacao()) && m.matches()) {
                throw new BusinessException(MensagemUtils.obterMensagem("MA015"));
            }

            // Caso o usuário solicite a inserção ou alteração de uma requisição cujo grupo do documento seja EXTRATO, o sistema deve validar o
            // período do extrato solicitado e, caso a requisição inicie ou finalize com
            // menos de um ano em relação ao mês anterior ao atual o sistema deve exibir a
            // mensagem MA014 e não permitir a continuidade da operação. Ex.: Em 09/2014, o usuário só poderá solicitar extratos que finalizem
            // antes do mês 08/2013. Esta validação deverá ser feita somente para as operações que tiverem o indicador IC_ OPERACAO_CONTA_CORRENTE = 1.
        } else if (nomeGrupo.equalsIgnoreCase(GrupoDocumentoEnum.EXTRATO.getLabel())
                && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNuMesAnoInicio())
                && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNuMesAnoFim())
                && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getOperacao())
                && requisicao.getRequisicaoDocumento().getOperacao().getIcOperacaoContaCorrente().equals(SimNaoEnum.SIM)) {
            String mesAnoInicio = formatarMesAno(requisicao.getRequisicaoDocumento().getNuMesAnoInicio());
            String mesAnoFim = formatarMesAno(requisicao.getRequisicaoDocumento().getNuMesAnoFim());

            Date dtInicio = DateUtils.tryParse(String.format("01/%s", mesAnoInicio), null);
            Date dtFim = DateUtils.tryParse(String.format("01/%s", mesAnoFim), null);

            if (!ObjectUtils.isNullOrEmpty(dtInicio) && !ObjectUtils.isNullOrEmpty(dtFim)) {
                Calendar mes = Calendar.getInstance();
                mes.add(Calendar.MONTH, -1);
                mes.setLenient(false);

                Calendar dataInicio = Calendar.getInstance();
                dataInicio.setTime(dtInicio);
                dataInicio.setLenient(false);

                Calendar dataFim = Calendar.getInstance();
                dataFim.setTime(dtFim);
                dataFim.setLenient(false);

                if ((Months.monthsBetween(new LocalDate(dataInicio.getTime()), new LocalDate(mes.getTime())).getMonths() < PRAZO_SOLICITACAO_EXTRATO)
                        || (Months.monthsBetween(new LocalDate(dataFim.getTime()), new LocalDate(mes.getTime())).getMonths() < PRAZO_SOLICITACAO_EXTRATO)) {
                    throw new BusinessException(MensagemUtils.obterMensagem("MA014"));
                }
            } else {
                // FIXME: #RC-SIRED Mensagem fora do padrão por não existir no
                // documento
                throw new BusinessException(MensagemUtils.obterMensagem("requisicao.mensagem.mesEAnoInvalido"));
            }
        }
    }

    private static String formatarMesAno(String mmAAAA) {
        if (!ObjectUtils.isNullOrEmpty(mmAAAA)) {
            if (mmAAAA.length() == 5) {
                mmAAAA = "0" + mmAAAA;
            }
            return mmAAAA.substring(0, 2) + "/" + mmAAAA.substring(2);
        }
        return null;
    }

    /**
     * Esta validação está relacionada ao campo NU_DIGITO_VERIFICADOR da tabela REDTBC15_REQUISICAO_DOCUMENTO, e se baseia nos valores
     * informados para os campos NU_UNIDADE_GERADORA_A02 (Agência), NU_OPERACAO_A11 (Operação) e NU_CONTA (Conta). Caso o dígito verificador
     * da conta seja diferente do calculado pelo sistema, o sistema deve exibir a mensagem MA034 e não permitir a continuidade da operação.
     * 
     * @param requisicao
     * @throws BusinessException
     */
    public void validarDigitoVerificadorConta(RequisicaoVO requisicao) throws BusinessException {

        if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNuDigitoVerificador())
                && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNumeroConta())
                && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getOperacao())
                && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getOperacao().getId())) {
            String numContaProcessado = requisicao.getRequisicaoDocumento().getNumeroConta().replace("_", "");
            String numeroConta = String.format("%04d%s%08d", requisicao.getRequisicaoDocumento().getUnidadeGeradora().getId(), requisicao
                    .getRequisicaoDocumento().getOperacao().getId(), Long.valueOf(numContaProcessado));
            if (!SiredUtils.isDigitoVerificadorContaCaixa(numeroConta, requisicao.getRequisicaoDocumento().getNuDigitoVerificador())) {
                throw new BusinessException(MensagemUtils.obterMensagem("MA034"));
            }
        }
    }

    /**
     * Caso a dezena contendo os dígitos verificadores do CPF seja diferente do calculado pelo sistema, o sistema deve exibir a mensagem
     * MA035 e não permitir a continuidade da operação.
     * 
     * @param requisicao
     * @throws BusinessException
     */
    public void validarCpf(RequisicaoVO requisicao) throws BusinessException {
        if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNumeroCpf())
                && !SiredUtils.isCpfValido(requisicao.getRequisicaoDocumento().getNumeroCpf())) {
            throw new BusinessException(MensagemUtils.obterMensagem("MA035"));
        }
    }

    /**
     * Caso a dezena contendo os dígitos verificadores do CNPJ seja diferente do calculado pelo sistema, o sistema deve exibir a mensagem
     * MA036 e não permitir a continuidade da operação.
     * 
     * @param requisicao
     * @throws BusinessException
     */
    public void validarCnpj(RequisicaoVO requisicao) throws BusinessException {
        if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNumeroCnpj())
                && !SiredUtils.isCnpjValido(requisicao.getRequisicaoDocumento().getNumeroCnpj())) {
            throw new BusinessException(MensagemUtils.obterMensagem("MA036"));
        }
    }

    /**
     * RF 2.3.04 - Contador de requisições atendidas O sistema deve contabilizar as requisições de documentos com finalidade de
     * processamento de relatório para o perfil Admin (ADM).
     * 
     * @param requisicao
     * @return
     */
    public Integer calcularQuantidadeDocumentosSolicitada(RequisicaoVO requisicao) {

        Date dtInicio = DateUtils.tryParse(String.format("01/%s", requisicao.getRequisicaoDocumento().getNuMesAnoInicio()), null);
        Date dtFim = DateUtils.tryParse(String.format("01/%s", requisicao.getRequisicaoDocumento().getNuMesAnoFim()), null);

        if (!ObjectUtils.isNullOrEmpty(dtInicio) && !ObjectUtils.isNullOrEmpty(dtFim)) {

            if (requisicao.getDocumento().getTipoAgrupamento().getValor().equals(TipoAgrupamentoDocumentoEnum.MENSAL.getValor())) {
                return Months.monthsBetween(new LocalDate(dtInicio), new LocalDate(dtFim)).getMonths();
            } else if (requisicao.getDocumento().getTipoAgrupamento().getValor().equals(TipoAgrupamentoDocumentoEnum.ANUAL.getValor())) {
                return Years.yearsBetween(new LocalDate(dtInicio), new LocalDate(dtFim)).getYears();
            }
        }

        return BigDecimal.ONE.intValue();
    }

    public void validarUnidadeGeradora(RequisicaoVO requisicao) throws BusinessException, DataBaseException {

        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        if (usuario != null) {

            UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
            if (unidadeLotacao.getTipoUnidade().getIndicadorUnidade().equals(TipoUnidadeEnum.PV)) {
                this.unidadeService.findUnidadePV(unidadeLotacao, usuario);
            } else {
                this.unidadeService.findUnidadeAutorizada(requisicao.getRequisicaoDocumento().getUnidadeGeradora(), usuario);
            }
        }
    }

    public void validarRequisicao(RequisicaoVO requisicao) throws BusinessException, DataBaseException {
        this.baseService.validarLoteSequencia(requisicao.getRequisicaoDocumento());
        this.validarGrupoDocumentoRequisicao(requisicao);
        this.validarDigitoVerificadorConta(requisicao);
        this.validarCpf(requisicao);
        this.validarCnpj(requisicao);
        this.validarUnidadeGeradora(requisicao);
        this.validarDataGeracao(requisicao);
    }

    private void validarDataGeracao(RequisicaoVO requisicao) throws BusinessException, DataBaseException {
        if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getDataGeracao())
                && !this.feriadoService.isDataUtil(requisicao.getRequisicaoDocumento().getDataGeracao(), requisicao.getUnidadeSolicitante())) {
            // FIXME: #OS_278 - Pendente: Incluir mensagem
            throw new BusinessException("Data de Geração inválida. A data deve ser um dia útil.");
        }
    }

    /**
     * Grava a requisição na base de dados
     * 
     * @param requisicao
     * @throws BusinessException
     */
    public void salvar(RequisicaoVO requisicao, final UploadedFile file) throws BusinessException {

        try {
            // Validações
            this.validarRequisicao(requisicao);

            // Trunca o texto da observação caso ultrapasse os 500 caracteres
            String observacao = StringUtils.abbreviate(requisicao.getRequisicaoDocumento().getObservacao(), Constantes.MAX_CHARS_OBSERVACAO);
            requisicao.getRequisicaoDocumento().setObservacao(observacao);

            // a qtde solicitada depende do intervalo de datas e do tipo do
            // documento, devendo ser executada ao gravar e ao concluir a
            // requisição.
            requisicao.setQtSolicitadaDocumento(this.calcularQuantidadeDocumentosSolicitada(requisicao));
            
            if (ObjectUtils.isNullOrEmpty(requisicao.getId())) {

                requisicao.setCodigoRequisicao(this.sequencialService.generate(requisicao.getUnidadeSolicitante()));
                this.save(requisicao);

                // Salvar como rascunho
                TramiteRequisicaoVO tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.RASCUNHO);
                tramite.setRequisicao(requisicao);
                this.tramiteService.save(tramite);

                requisicao.setTramiteRequisicaoAtual(tramite);
            } else {
                this.validarUnidadeGeradora(requisicao);
            }
            
            if (FormatoDocumentoEnum.ORIGINAL.equals(requisicao.getFormato()) 
            		&& !ObjectUtils.isNullOrEmpty(file) && file.getSize() > 0L) {
            	this.setArquivoJustificativa(requisicao, file);
            }
            
            this.update(requisicao);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
        	this.logger.error(e.getMessage(), e);
            throw new BusinessException(MensagemUtils.obterMensagem("MA012"), e);
        }
    }
    
    public void salvar(List<RequisicaoLoteDTO> requisicoes) throws RequisicaoLoteException, RequiredException, Exception {
        
        Integer numeroRejeitadas = 0;
        List<String> msgValidacao = new ArrayList<String>();
        
        for (RequisicaoLoteDTO requisicaoLote : requisicoes) {
            
            CSVRecord registro = requisicaoLote.getRegistro();
            RequisicaoVO requisicao = requisicaoLote.getRequisicao();
            
            try {
                // Cria a requisição
                requisicao.setCodigoRequisicao(this.sequencialService.generate(requisicao.getUnidadeSolicitante()));
                this.save(requisicao);
                
                TramiteRequisicaoVO tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.RASCUNHO);
                tramite.setRequisicao(requisicao);
                this.tramiteService.save(tramite);
                
                // Rascunho
                requisicao.setTramiteRequisicaoAtual(tramite);
                this.update(requisicao);
                
                // Concluir requisição sem validar as requisições, etapas já feitas antes da importação.
                this.concluir(requisicao, null, false);
            } catch (BusinessException e) {
                numeroRejeitadas++;
                msgValidacao.add(String.format("Linha %s - %s", registro.getRecordNumber(), e.getMessage()));
            } 
        }
        
        if (!ObjectUtils.isNullOrEmpty(msgValidacao)) {
            RequisicaoLoteException e = new RequisicaoLoteException(msgValidacao);
            e.setNumeroRejeitadas(numeroRejeitadas);
            throw e;
        }
    }

	public void concluir(RequisicaoVO requisicao, final UploadedFile file)
			throws BusinessException {
		concluir(requisicao, file, true);
	}
    
    public void concluir(RequisicaoVO requisicao, final UploadedFile file, boolean validarRequisicao) throws BusinessException {

        try {
        	
            // Validações
        	if (validarRequisicao) {
                this.validarRequisicao(requisicao);
                this.isRequisicaoDuplicada(requisicao);				
			}
            // a qtde solicitada depende do intervalo de datas e do tipo do
            // documento, devendo ser executada ao gravar e ao concluir a
            // requisição.
            requisicao.setQtSolicitadaDocumento(this.calcularQuantidadeDocumentosSolicitada(requisicao));
            
            // Sistema verifica e registra a empresa responsável pelo atendimento à esta requisição. FE2.
            // Sistema verifica e registra a base responsável pela área onde o documento está localizado. [RN003] FE5
            EmpresaContratoVO contrato = this.baseService.findContratoBaseByDocumentoEager(requisicao.getDocumento(), requisicao
						.getRequisicaoDocumento().getDataGeracao());
            
            if (ObjectUtils.isNullOrEmpty(contrato)) {
                contrato = this.baseService.findContratoBaseByUnidadeEager(requisicao.getRequisicaoDocumento().getUnidadeGeradora());
                if (ObjectUtils.isNullOrEmpty(contrato)) {
                    throw new BusinessException(MensagemUtils.obterMensagem("ME006", requisicao.getRequisicaoDocumento().getUnidadeGeradora()
                            .getDescricaoCompleta()));
                }
            }

            requisicao.setBase(contrato.getBase());
            requisicao.setEmpresaContrato(contrato);

            // Caso seja solicitado documento em formato ORIGINAL, atualiza a
            // situação atual da
            // Requisição para EM AUTORIZAÇÃO, caso contrário para ABERTA.
            // Registra no trâmite o usuário e a
            // data/hora da operação.
            TramiteRequisicaoVO tramite = null;

            if (!ObjectUtils.isNullOrEmpty(requisicao.getFormato()) && requisicao.getFormato().equals(FormatoDocumentoEnum.ORIGINAL)
                    && !this.hasAutorizacaoAutomaticaByUnidade(requisicao)) {
                tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.EM_AUTORIZACAO);
            } else {
                tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.ABERTA);
            }

            tramite.setRequisicao(requisicao);
            this.tramiteService.save(tramite);

            requisicao.setDataHoraAbertura(new Date());
            requisicao.setPrazoAtendimento(this.calcularPrazoAtendimento(requisicao));
            requisicao.setTramiteRequisicaoAtual(tramite);
            
            if (FormatoDocumentoEnum.ORIGINAL.equals(requisicao.getFormato()) 
            		&& !ObjectUtils.isNullOrEmpty(file) && file.getSize() > 0L) {
            	this.setArquivoJustificativa(requisicao, file);
            }
            
            this.update(requisicao);

        } catch (BusinessException e) {
        	this.logger.error("RequisicaoService.concluir.BusinessException");
            throw e;
        } catch (Exception e) {
        	this.logger.error("RequisicaoService.concluir.Exception");
        	this.logger.error(e.getMessage(), e);
            throw new BusinessException(MensagemUtils.obterMensagem("MA012"), e);
        }
    }

    public void autorizar(RequisicaoVO requisicao) throws BusinessException {

        try {

            TramiteRequisicaoVO tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.ABERTA);
            tramite.setRequisicao(requisicao);
            this.tramiteService.save(tramite);

            requisicao.setTramiteRequisicaoAtual(tramite);
            this.update(requisicao);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public void cancelar(RequisicaoVO requisicao, String observacao) throws BusinessException {

        try {

            TramiteRequisicaoVO tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.CANCELADA);
            tramite.setRequisicao(requisicao);
            tramite.setObservacao(observacao);
            this.tramiteService.save(tramite);

            requisicao.setTramiteRequisicaoAtual(tramite);
            this.update(requisicao);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public void avaliar(RequisicaoVO requisicao, AvaliacaoRequisicaoVO avaliacao) throws BusinessException {

        try {

            // Salva a avaliação...
            avaliacao.setDataHora(Calendar.getInstance().getTime());
            this.avaliacaoService.save(avaliacao);

            // Cria um novo tramite...
            SituacaoRequisicaoEnum situacaoTramite = (avaliacao.getIcReabertura().equals(SimNaoEnum.NAO)) ? SituacaoRequisicaoEnum.FECHADA
                    : SituacaoRequisicaoEnum.REABERTA;
            TramiteRequisicaoVO tramiteNovo = this.tramiteService.getBySituacaoRequisicao(situacaoTramite);

            Util.copiarInformacoesTramite(requisicao.getTramiteRequisicaoAtual(), tramiteNovo);

            tramiteNovo.setRequisicao(requisicao);
            this.tramiteService.save(tramiteNovo);

            // Define o tramite como o atual da requisição
            requisicao.setTramiteRequisicaoAtual(tramiteNovo);
            this.update(requisicao);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    public void tratar(RequisicaoVO requisicao) throws BusinessException {

        try {

            SituacaoRequisicaoEnum situacaoAtual = SituacaoRequisicaoEnum.get((Long) requisicao.getTramiteRequisicaoAtual().getSituacaoRequisicao().getId());

            SituacaoRequisicaoEnum situacaoTramite = (situacaoAtual.equals(SituacaoRequisicaoEnum.EM_TRATAMENTO)) ? SituacaoRequisicaoEnum.ATENDIDA
                    : SituacaoRequisicaoEnum.REATENDIDA;
            TramiteRequisicaoVO tramite = this.tramiteService.getBySituacaoRequisicao(situacaoTramite);

            tramite.setRequisicao(requisicao);

            Util.copiarInformacoesTramite(requisicao.getTramiteRequisicaoAtual(), tramite);

            this.tramiteService.save(tramite);

            requisicao.setTramiteRequisicaoAtual(tramite);
            this.update(requisicao);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }
    
    public void isRequisicaoDuplicadaNoLote() {
    	
    }

    public void isRequisicaoDuplicada(RequisicaoVO requisicao) throws BusinessException, AppException {
        ParametroSistemaVO parametro = this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_PZ_MANUTENCAO_ARQUIVOS);

        if (!ObjectUtils.isNullOrEmpty(parametro)) {

            Calendar calendar = Calendar.getInstance();

            try {
                calendar.add(Calendar.DAY_OF_YEAR, Integer.valueOf(parametro.getVlParametroSistema()) * -1);
            } catch (NumberFormatException e) {
                throw new AppException(MensagemUtils.obterMensagem("geral.exception.InvalidSystemParameter"), e);
            }

            List<RequisicaoVO> listRequisicao = this.dao.findRequisicoesEnviadasPorPeriodo(requisicao, calendar.getTime(), Calendar.getInstance().getTime());
            for (RequisicaoVO r : listRequisicao) {
                if (requisicao.getRequisicaoDocumento().equals(r.getRequisicaoDocumento())) {
                    throw new BusinessException(MensagemUtils.obterMensagem("MA007", r.getCodigoRequisicao().toString()));
                }
            }
        }
    }

    @Transactional(value=TxType.REQUIRES_NEW)
    public int fecharRequisicoesPendentesAvaliacao() throws BusinessException, NumberFormatException, DataBaseException {
        Date hoje = new Date();
        Integer prazo = Integer.valueOf(this.parametroService.findByNome(Constantes.PARAMETRO_SISTEMA_PZ_REABERTURA).getVlParametroSistema());
        List<RequisicaoVO> listRequisicao = this.dao.findAllPendentesFechamento();
        int totalFechadas = 0;
        for (RequisicaoVO requisicao : listRequisicao) {
            Integer dias = this.feriadoService.getNumeroDiasUteis(hoje, requisicao.getTramiteRequisicaoAtual().getDataHoraAtendimento(),
                    requisicao.getUnidadeSolicitante());
            if (dias > prazo) {
                try {
                  Boolean retorno = realizaAtualizacaoRequisicaoTramite(requisicao);
                  if(retorno) {
                    totalFechadas++;
                  }
                } catch (Exception e) {
                    throw new BusinessException(e.getMessage(), e);
                }
            }
        }
        return totalFechadas;
    }
    
    public Boolean realizaAtualizacaoRequisicaoTramite(RequisicaoVO requisicao) throws RequisicaoTransactionException {
      try {
          // Cria um novo tramite...
          TramiteRequisicaoVO tramiteNovo = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.FECHADA, USUARIO_SIRED);
    
          Util.copiarInformacoesTramite(requisicao.getTramiteRequisicaoAtual(), tramiteNovo);
    
          tramiteNovo.setRequisicao(requisicao);
          this.tramiteService.save(tramiteNovo);
    
          // Define o tramite como o atual da requisição
          requisicao.setTramiteRequisicaoAtual(tramiteNovo);
          this.update(requisicao);
          return true;
      }catch(Exception e) {
        throw new RequisicaoTransactionException(e.getMessage(), e);
      }
    }

    public List<ResumoAtendimentoRequisicaoDTO> getResumoAtendimentos(Date dataInicio, Date dataFim) {
        return this.dao.getResumoAtendimentos(dataInicio, dataFim);
    }
    
    public RequisicaoVO clonar(final RequisicaoVO requisicao) throws BusinessException, DataBaseException {
        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
        RequisicaoVO clone = RequisicaoHelper.clonar(requisicao, usuario, unidadeLotacao);
        this.salvar(clone, null);
        
        return clone;
    }
    
    public boolean hasAutorizacaoAutomaticaByUnidade(final RequisicaoVO requisicao) {
        String cgcUnidades = requisicao.getDocumento().getUnidadesAutorizadas();
        if (!ObjectUtils.isNullOrEmpty(cgcUnidades)) {
            String[] unidadeArray = StringUtils.split(cgcUnidades, ",");
            for (String unidade : unidadeArray) {
                try {
                    if (Long.valueOf(unidade).equals(requisicao.getUnidadeSolicitante().getId())) {
                        return true;
                    }
                } catch (Exception e) {
                    this.logger.error(e.getMessage(), e);
                    return false;
                }
            }
            
            return false;
        }
        
        return false;
    }

	public void cancelarRascunho(final RequisicaoVO requisicao) throws BusinessException {
		try {
            TramiteRequisicaoVO tramite = this.tramiteService.getBySituacaoRequisicao(SituacaoRequisicaoEnum.CANCELADA);
            tramite.setRequisicao(requisicao);
            this.tramiteService.save(tramite);

            requisicao.setTramiteRequisicaoAtual(tramite);
            this.update(requisicao);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
	}
	
	private void setArquivoJustificativa(RequisicaoVO requisicao, final UploadedFile file) throws RequiredException, Exception {
    	try {
			String noArquivo = String.format("RED_REQ_JUS_%s.%s",
					requisicao.getCodigoRequisicao(),
					StringUtils.right(file.getFileName(), 3));
			noArquivo = FileUtils.appendDateTimeToFileName(noArquivo, new Date(), "yyMMddHHmm");
			
        	String diretorio = System.getProperty(Constantes.CAMINHO_UPLOAD_JUSTIFICATIVA);
        	
        	FileUtils.createDirIfNotExists(diretorio);
        	
			OutputStream out = Files.newOutputStream(
					Paths.get(diretorio, noArquivo),
					StandardOpenOption.CREATE,
					StandardOpenOption.WRITE,
					StandardOpenOption.TRUNCATE_EXISTING);
        	out.write(file.getContents());
        	out.flush();
        	out.close();
        	
        	requisicao.setArquivoJustificativa(noArquivo);
		} catch (IOException e) {
			throw new BusinessException(MensagemUtils.obterMensagem("MA049"));
		}
		
	}
	
	public Map<UnidadeVO, RequisicaoDTO> pesquisaAbertasHoje(Date hojeMeiaNoite, Date hoje){
	  this.mapRequisicao = new HashMap<>();
	  List<RequisicaoVO> requisicoes =  this.dao.pesquisaAbertasHoje(hojeMeiaNoite, hoje);
	  
	  if(!requisicoes.isEmpty()) {
  	  for (RequisicaoVO requisicaoVO : requisicoes) {
        if(!this.mapRequisicao.containsKey(requisicaoVO.getUnidadeSolicitante())) {
          this.mapRequisicao.put(requisicaoVO.getUnidadeSolicitante(), new RequisicaoDTO()); 
        }
        RequisicaoDTO requisicaoDTO = this.mapRequisicao.get(requisicaoVO.getUnidadeSolicitante());
        List<RequisicaoVO> requisicoesList = requisicaoDTO.getRequisicaoList();
        requisicoesList.add(requisicaoVO);
        requisicaoDTO.setUnidade(requisicaoVO.getUnidadeSolicitante());
      }
	  }
	  return mapRequisicao;
	}

}
