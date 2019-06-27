package br.gov.caixa.gitecsa.sired.extra.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.component.datalist.DataList;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.BaseConsulta;
import br.gov.caixa.gitecsa.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.service.GrupoService;
import br.gov.caixa.gitecsa.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.service.RemessaService;
import br.gov.caixa.gitecsa.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.comparator.RemessaDocDiarioNumItemComparator;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.extra.service.FeriadoService;
import br.gov.caixa.gitecsa.sired.extra.service.RemessaMovimentoDiarioService;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.util.CollectionUtils;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RelatorioSiredUtil;
import br.gov.caixa.gitecsa.sired.util.StringUtil;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;
import br.gov.caixa.gitecsa.util.ReportUtils;

@Named
@ViewScoped
@SuppressWarnings("deprecation")
public class ConsultaRemessaController extends BaseConsulta<RemessaVO> {

	private static final long serialVersionUID = -1258411817438664808L;

	@Inject
	private RemessaService remessaService;

	@Inject
	private UnidadeService unidadeService;

	@Inject
	private br.gov.caixa.gitecsa.sired.extra.service.TramiteRemessaService tramiteRemessaService;

	@Inject
	private GrupoService grupoService;

	@Inject
	private RemessaDocumentoService remessaDocumentoService;

	@Inject
	private RemessaMovimentoDiarioService remessaMovimentoDiarioService;

	private boolean pesquisaRealizada;

	private String numRemessa;
	

	private Date dataInicioFiltro = Util.getDataMesPassado();

	private Date dataFimFiltro;

	private Long nuRemessaFiltro;

	private Long nuLacreFiltro;
	
	private String coUsuarioFiltro;

	private UnidadeVO unidadeFiltro;

	private String codigoUnidadeFiltro;

	private String nomeUnidadeFiltro;

	private String filtroHistorico;

	private SituacaoRemessaEnum situacaoFiltro;

	private DocumentoVO documento;

	private RemessaVO remessa;

	private RemessaDocumentoVO remessaDocumento;

	private GrupoVO grupo;

	private TramiteRemessaVO tramiteRemessaVO;

	private StreamedContent capaLote;

	private List<RemessaVO> listaRemessa;

	private List<RemessaVO> listaFiltro;

	private List<TramiteRemessaVO> listaTramitesRemessa;

	private List<TramiteRemessaVO> filtredListaTramitesRemessa;

	private LazyDataModel<RemessaVO> listaRemessaModel;

	private boolean pesquisaSucesso;

	private String[] listaAcaoRemessa;

	private String acaoRemessa;

	private String horaAgendamento;

	private boolean reagendar;

	private boolean retornaModal;

	private MovimentoDiarioRemessaCDTO itemDetalheTipoC;
	
	private Boolean quantitativoZerado = false;

	@Inject
	private FeriadoService feriadoService;

	private GrupoVO grupoOriginal;

	@PostConstruct
	protected void init() throws AppException {
		filtroHistorico = StringUtils.EMPTY;
		this.itemDetalheTipoC = null;
		initTable();

	}

	private void initTable() {
		listaRemessaModel = new LazyDataModel<RemessaVO>() {

			private static final long serialVersionUID = 3528646700234313883L;

			@Override
			public List<RemessaVO> load(int first, int pageSize, String sortField, SortOrder sortOrder,
					Map<String, String> filters) {
				try {
					List<RemessaVO> list = null;
					if ((first == 0) && (pageSize == 0)) {
						list = new ArrayList<RemessaVO>();
					} else {
						list = pesquisar(first, pageSize);
					}

					if (!Util.isNullOuVazio(list)) {
						setRowCount(totalRegistros());
						pesquisaSucesso = Boolean.TRUE;
						return list;
					} else {
						setRowCount(0);
						pesquisaSucesso = Boolean.TRUE;
						return null;
					}
				} catch (AppException e) {
					pesquisaSucesso = Boolean.FALSE;

					facesMessager
							.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));

					logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "load"));
				}

				return null;
			}
		};
	}

	public void prepararFechamentoRemessa(RemessaVO remessaVO) {
		this.remessa = remessaVO;
		tramiteRemessaVO = new TramiteRemessaVO();
		tramiteRemessaVO.setRemessa(remessa);
		tramiteRemessaVO.setSituacao(new SituacaoRemessaVO());
		tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.FECHADA.getId());
		tramiteRemessaVO.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
		showDialog("modalFechamentoRemessa");
	}

	public void prepararAgendamento(RemessaVO remessa) {

		this.remessa = remessa;
		horaAgendamento = null;
		tramiteRemessaVO = new TramiteRemessaVO();
		tramiteRemessaVO.setRemessa(remessa);
		tramiteRemessaVO.setSituacao(new SituacaoRemessaVO());
		tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.AGENDADA.getId());
		tramiteRemessaVO.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
		showDialog("modalAgendamento");

	}

	public void agendar() throws Exception {

		if (validarDataHora()) {
			try {
				Calendar cal = Calendar.getInstance();
				String dataHoraAgendamento;
				String observacao;
				SimpleDateFormat dateFormat = new SimpleDateFormat(Constantes.FORMATO_DATA);

				cal.setTime(tramiteRemessaVO.getDataAgendamento());

				if (!Util.isNull(horaAgendamento)) {
					String[] hora = horaAgendamento.split(":");
					cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hora[0]));
					cal.set(Calendar.MINUTE, Integer.parseInt(hora[1]));
					tramiteRemessaVO.setDataAgendamento(cal.getTime());
					dataHoraAgendamento = tramiteRemessaVO.getDataAgendamentoFormatada();
				} else {
					tramiteRemessaVO.setDataAgendamento(cal.getTime());
					dataHoraAgendamento = dateFormat.format(tramiteRemessaVO.getDataAgendamento());
				}

				tramiteRemessaVO.setDataTramiteRemessa(new Date());
				observacao = MensagemUtils.obterMensagem("remessa.agendada.observacao.adendo", dataHoraAgendamento);
				if (!Util.isNullOuVazio(tramiteRemessaVO.getObservacao())) {
					observacao = observacao.concat(" " + tramiteRemessaVO.getObservacao().trim());
				}

				if (!ObjectUtils.isNullOrEmpty(tramiteRemessaVO.getDataAgendamento()) && !this.feriadoService
						.isDataUtil(tramiteRemessaVO.getDataAgendamento(), remessa.getUnidadeSolicitante())) {
					throw new BusinessException("Data de Agendamento inválida. A data deve ser um dia útil.");
				}

				tramiteRemessaVO.setObservacao(observacao);
				tramiteRemessaService.salvarTramiteRemessa(tramiteRemessaVO);

				remessa.setTramiteRemessaAtual(tramiteRemessaVO);
				remessa.setDataAgendamento(tramiteRemessaVO.getDataAgendamento());
				remessaService.saveOrUpdate(remessa);

				if (!Util.isNullOuVazio(acaoRemessa) && acaoRemessa.equals(Constantes.REAGENDAR)) {
					hideDialog("modalAcoesRemessa");
				} else {
					hideDialog("modalAgendamento");
				}
				facesMessager.addMessageInfo(
						MensagemUtils.obterMensagem("MS032", remessa.getId().toString(), dataHoraAgendamento));
				updateComponentes("formConsulta");
			} catch (BusinessException e) {
				facesMessager.addMessageError(e.getMessage());
			} catch (AppException e) {
				facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
				logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultaRemessaController", "agendar"));
			}
		}
	}

	public boolean validarDataHora() {

		if (!Util.isNullOuVazio(horaAgendamento)) {

			String[] horas = horaAgendamento.split(":");
			int hora = Integer.parseInt(horas[0]);
			int minuto = Integer.parseInt(horas[1]);

			if (hora > 23 || minuto > 59 || horaAgendamento.length() < 5) {
				facesMessager.addMessageError(MensagemUtils.obterMensagem("MI014", "remessa.label.hora.coleta"));
				return false;
			}
			Calendar data = Calendar.getInstance();
			data.setTime(tramiteRemessaVO.getDataAgendamento());
			data.set(Calendar.HOUR, hora);
			data.set(Calendar.MINUTE, minuto);
			tramiteRemessaVO.setDataAgendamento(data.getTime());
		} else if (isDataAtual(tramiteRemessaVO.getDataAgendamento())) {
			Calendar data = Calendar.getInstance();
			tramiteRemessaVO.setDataAgendamento(data.getTime());
		}
		// Instancia a data atual e zera os segundos e os milesegundos para comparar com
		// a data hora informada,
		Calendar dataAtual = Calendar.getInstance();
		dataAtual.set(Calendar.SECOND, 0);
		dataAtual.set(Calendar.MILLISECOND, 0);

		if (tramiteRemessaVO.getDataAgendamento().before(dataAtual.getTime())) {
			if (Util.isNullOuVazio(horaAgendamento)) {
				facesMessager.addMessageError(MensagemUtils.obterMensagem("MA061", "remessa.label.data.coleta"));
			} else {
				String campos = MensagemUtils.obterMensagem("remessa.label.data.coleta") + "/"
						+ MensagemUtils.obterMensagem("remessa.label.hora.coleta");
				facesMessager.addMessageError(MensagemUtils.obterMensagem("MA061", campos));
			}
			return false;
		}

		return true;
	}

	/**
	 * Verifica se a data do parâmetro é igual a data atual.
	 * 
	 * @param data
	 * @return
	 */
	public boolean isDataAtual(Date data) {

		Calendar dataAtual = Calendar.getInstance();
		dataAtual.set(Calendar.HOUR_OF_DAY, 0);
		dataAtual.set(Calendar.MINUTE, 0);
		dataAtual.set(Calendar.SECOND, 0);
		dataAtual.set(Calendar.MILLISECOND, 0);
		if (data.compareTo(dataAtual.getTime()) == 0) {
			return true;
		}

		return false;
	}

	public void prepararAcao(RemessaVO remessa) {

		this.remessa = remessa;
		acaoRemessa = null;
		horaAgendamento = null;
		reagendar = false;
		tramiteRemessaVO = new TramiteRemessaVO();
		tramiteRemessaVO.setRemessa(remessa);
		tramiteRemessaVO.setSituacao(new SituacaoRemessaVO());
		tramiteRemessaVO.setCodigoUsuario(JSFUtil.getUsuario().getEmail());

		if (remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.AGENDADA.getId())) {

			String[] listaAcao = { Constantes.REAGENDAR, Constantes.RECEBER, Constantes.RECEBER_CONFIRMAR_FECHAR };
			listaAcaoRemessa = listaAcao;

		}
		if (remessa.getTramiteRemessaAtual().getSituacao().getId().equals(SituacaoRemessaEnum.RECEBIDA.getId())) {

			String[] listaAcao = { Constantes.CONFERIR, Constantes.INVALIDAR };
			listaAcaoRemessa = listaAcao;

		}
		showDialog("modalAcoesRemessa");

	}

	public void definirSituacaoTramite() throws Exception {

		if (Util.isNullOuVazio(acaoRemessa)) {
			super.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.acao"));
		} else {
			if (acaoRemessa.equals(Constantes.REAGENDAR)) {
				tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.AGENDADA.getId());
				agendar();
			} else {
				if (acaoRemessa.equals(Constantes.RECEBER)) {
					tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.RECEBIDA.getId());
				} else if (acaoRemessa.equals(Constantes.CONFERIR)) {
					tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.CONFERIDA.getId());
				} else if (acaoRemessa.equals(Constantes.INVALIDAR)) {
					tramiteRemessaVO.getSituacao().setId(SituacaoRemessaEnum.INVALIDA.getId());
				} else if (acaoRemessa.equals(Constantes.RECEBER_CONFIRMAR_FECHAR)) {
					try {
						receberConfirmarFecharRemessa();
						return;
					} catch (AppException e) {
						logger.log(null, e.getStackTrace());
					}
				}
				salvarTramiteRemessaGenerico();
			}
		}

	}

	/**
	 * Salva o trâmite da remessa de acordo com a situação do mesmo.
	 * 
	 * @throws Exception
	 */
	public void salvarTramiteRemessaGenerico() throws Exception {
		try {

			tramiteRemessaVO.setDataTramiteRemessa(new Date());
			tramiteRemessaService.salvarTramiteRemessa(tramiteRemessaVO);

			remessa.setTramiteRemessaAtual(tramiteRemessaVO);
			remessaService.saveOrUpdate(remessa);
			hideDialog("modalAcoesRemessa");

			if (acaoRemessa.equals(Constantes.RECEBER)) {
				facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS033", remessa.getId().toString()));
			} else if (acaoRemessa.equals(Constantes.CONFERIR)) {
				facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS036", remessa.getId().toString()));
			} else if (acaoRemessa.equals(Constantes.INVALIDAR)) {
				facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS035", remessa.getId().toString()));
			}
			updateComponentes("formConsulta");

		} catch (AppException e) {
			facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			logger.error(
					LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultaRemessa", "salvarTramiteRemessaGenerico"));
		}
	}

	public void perpararDevolvida(RemessaVO remessaVO) {

		this.remessa = remessaVO;
		showDialog("modalDevolvida");

	}

	public void ajaxReagendar() {

		if (acaoRemessa.equals(Constantes.REAGENDAR)) {
			reagendar = true;
		} else {
			reagendar = false;
		}

	}

	/**
	 * Metodo que replica a localização do primeiro item da remessa para os demais
	 * itens da remessa.
	 */
	public void replicarLocalizacaoItens() {

		String localizacao = "";

		if (!ObjectUtils.isNullOrEmpty(this.remessa.remessaDocumentosTratados())) {
			localizacao = this.remessa.remessaDocumentosTratados().get(0).getDescricaoLocalizacao();
		} else {
			localizacao = this.remessa.remessaMovimentoTratado().get(0).getLocalizacao();
		}

		if (!StringUtils.isEmpty(localizacao)) {
			if (!ObjectUtils.isNullOrEmpty(this.remessa.remessaDocumentosTratados())) {
				for (int i = 1; i < this.remessa.getRemessaDocumentos().size(); i++) {
					this.remessa.getRemessaDocumentos().get(i).setDescricaoLocalizacao(localizacao);
				}
			}
			// MOVIMENTO DIÁRIO
			else {
				for (int i = 0; i < this.remessa.remessaMovimentoTratado().size(); i++) {
					this.remessa.remessaMovimentoTratado().get(i).setLocalizacao(localizacao);
				}
			}
		}
	}

	/**
	 * Metodo que limpa a localização de todos os itens da remessa.
	 */
	public void limparLocalizacaoItens() {

		if (!ObjectUtils.isNullOrEmpty(this.remessa.remessaDocumentosTratados())) {
			for (int i = 0; i < this.remessa.remessaDocumentosTratados().size(); i++) {
				this.remessa.getRemessaDocumentos().get(i).setDescricaoLocalizacao(StringUtils.EMPTY);
			}
		}
		// MOVIMENTO DIÁRIO
		else {
			for (int i = 0; i < this.remessa.remessaMovimentoTratado().size(); i++) {
				this.remessa.remessaMovimentoTratado().get(i).setLocalizacao(StringUtils.EMPTY);
			}
		}
	}

	public void fecharRemessa() throws Exception {
		try {
			// TODO - validar se a remessa é do tipo AB ou do tipo C
			if (this.remessa.getTipoRemessaMoviMentoDiario()) {
				// Salvar a localização dos itens da remessa Do tipo C
				for (RemessaMovimentoDiarioVO remessaMov : this.remessa.getMovimentosDiarioList()) {
					if (!Util.isNullOuVazio(remessaMov.getLocalizacao())) {
						remessaMovimentoDiarioService.salvarItemMovimentoDiario(remessaMov);
					}
				}
			} else {
				// Salvar a localização dos itens da remessa Do tipo AB
				for (RemessaDocumentoVO remessaDoc : this.remessa.getRemessaDocumentos()) {
					if (!Util.isNullOuVazio(remessaDoc.getDescricaoLocalizacao())) {
						remessaDocumentoService.saveOrUpdate(remessaDoc);
					}
				}
			}

			// Salvar o trâmite de fechamento da remessa
			TramiteRemessaVO tramite = new TramiteRemessaVO();
			tramite.setRemessa(remessa);
			tramite.setSituacao(new SituacaoRemessaVO());
			tramite.getSituacao().setId(SituacaoRemessaEnum.FECHADA.getId());
			tramite.setCodigoUsuario(JSFUtil.getUsuario().getEmail());
			tramite.setDataTramiteRemessa(new Date());
			tramite.setObservacao(tramiteRemessaVO.getObservacao());
			tramiteRemessaService.salvarTramiteRemessa(tramite);

			// Atualizar a remessa com o tramite atual
			remessa.setTramiteRemessaAtual(tramite);
			remessaService.saveOrUpdate(remessa);
			hideDialog("modalFechamentoRemessa");
			facesMessager.addMessageInfo(MensagemUtils.obterMensagem("MS039", remessa.getId().toString()));
			updateComponentes("formConsulta");

			limparLocalizacao();

		} catch (AppException e) {
			facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa",
					"gravarFechamentoRemessaInconsistente"));
		}
	}

	public void handleNumeroRemessa() {
		if (nuRemessaFiltro != null && nuRemessaFiltro > 0L) {
			this.nuLacreFiltro = null;
			this.coUsuarioFiltro = null;
			this.dataInicioFiltro = null;
			this.dataFimFiltro = null;
			this.unidadeFiltro = null;
			this.codigoUnidadeFiltro = null;
			this.nomeUnidadeFiltro = null;
			this.situacaoFiltro = null;
			updateComponentes("pnlCamposFiltro");
		}

	}

	/**
	 * Consulta as remessas que atendam aos parâmetros do filtro.
	 */
	public void localizar() {

		if (!validarParametrosDeConsulta()) {
			listaRemessaModel.load(0, 0, null, null, null);
			updateComponentes("formConsulta");
			updateComponentes("pnlAccordion");
			updateComponentes("tabela");
			return;
		}

		pesquisaRealizada = true;

		DataList dataList = (DataList) FacesContext.getCurrentInstance().getViewRoot()
				.findComponent("formConsulta:tabela");
		if (dataList != null) {
			dataList.setFirst(0);
		}

		listaRemessaModel.load(0, 1, null, null, null);

		if (listaRemessaModel.getRowCount() <= 0 && pesquisaSucesso) {
			JSFUtil.addErrorMessage(MensagemUtils.obterMensagem("MI022"));
		} else {
			updateComponentes("formConsulta");
		}

	}

	private List<RemessaVO> pesquisar(int first, int pageSize) throws AppException {

		if (pesquisaRealizada) {
			if (getNuRemessaFiltro() != null) {
				listaRemessa = remessaService.consultarRemessa(getNuRemessaFiltro(), null, null, null, null, null, null,
						first, pageSize);
			} else {
				listaRemessa = remessaService.consultarRemessa(getNuRemessaFiltro(), getNuLacreFiltro(), getDataInicioFiltro(),
						getDataFimFiltro(), getCoUsuarioFiltro(), getUnidadeFiltro(), getSituacaoFiltro(), first, pageSize);
			}

			for (RemessaVO remessaVO : listaRemessa) {
				if (remessaVO.getTipoRemessaMoviMentoDiario()) {
					remessaVO.setDataMovimentosList(remessaService.obterAgrupamentoDeItensDeRemessaPorDiaUnidade(remessaVO));
				}
			}

			return listaRemessa;
		} else {
			return null;
		}
	}

	public void pesquisarUnidadeSolicitante() {

		if (!Util.isNullOuVazio(codigoUnidadeFiltro)) {

			try {
				unidadeFiltro = new UnidadeVO();
				unidadeFiltro.setId(Long.parseLong(codigoUnidadeFiltro));
				List<UnidadeVO> unidades = unidadeService.findByParameters(unidadeFiltro);

				if (!Util.isNullOuVazio(unidades)) {
					unidadeFiltro = unidades.get(0);
					nomeUnidadeFiltro = unidadeFiltro.getNome().trim();
				} else {
					facesMessager.addMessageError(
							MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_UNIDADE_NAO_CADASTRADA));
				}

			} catch (Exception e) {
				facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
				logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultaRequisicaoController",
						"pesquisarUnidadeSolicitante"));
			}

		} else {
			unidadeFiltro = null;
			nomeUnidadeFiltro = "";
		}

	}

	private int totalRegistros() throws AppException {
		if (pesquisaRealizada) {
			if (getNuRemessaFiltro() != null) {
				return remessaService.consultarRemessaTotalRegistros(getNuRemessaFiltro(), null, null, null, null, null, null);
			} else {
				return remessaService.consultarRemessaTotalRegistros(getNuRemessaFiltro(), getNuLacreFiltro(), getDataInicioFiltro(),
						getDataFimFiltro(), getCoUsuarioFiltro(), getUnidadeFiltro(), getSituacaoFiltro());
			}
		} else {
			return 0;
		}
	}

	private boolean validarParametrosDeConsulta() {

		if (Util.isNullOuVazio(nuRemessaFiltro)) {

			if (Util.isNullOuVazio(dataInicioFiltro)) {
				super.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA001", "geral.label.dataInicio"));
				return false;
			}

			if (Util.isNullOuVazio(dataFimFiltro) && !Util.isNullOuVazio(dataInicioFiltro)) {
				dataFimFiltro = Calendar.getInstance().getTime();
			}

			if (dataInicioFiltro.after(dataFimFiltro)) {
				super.facesMessager
						.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_DT_INICIO_MENOR_DT_FIM));
				return false;
			}
		}

		return true;
	}

	public void verHistoricoRemessa(RemessaVO remessaVO) {
		try {
			this.remessa = remessaVO;
			TramiteRemessaVO tramite = new TramiteRemessaVO();
			tramite.setRemessa(remessaVO);
			this.listaTramitesRemessa = tramiteRemessaService.findByRemessa(remessa);
			this.filtredListaTramitesRemessa = this.listaTramitesRemessa;
			filtroHistorico = "";
			showDialog("modalHistorico");
		} catch (AppException e) {
			facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "verHistoricoRemessa"));
		}
	}

	public boolean exibirDemaisSituacoesRemessa(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.BLOQUEADA.getId())
				|| situacao.getId().equals(SituacaoRemessaEnum.FECHADA.getId())
				|| situacao.getId().equals(SituacaoRemessaEnum.FECHADA_INCONSISTENTE.getId())) {
			return true;
		}
		return false;
	}

	public boolean exibirEditarRemessa(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.RECEBIDA.getId())
				|| situacao.getId().equals(SituacaoRemessaEnum.EM_ALTERACAO.getId())) {
			return true;
		}
		return false;
	}

	/**
	 * Prepara remessa e redireciona sistema para a pagina de Rascunho de Remesssa
	 * para edição da mesma.
	 * 
	 * @param remessaVO
	 */
	public void editarRemessa(RemessaVO remessaVO) {
		try {
			JSFUtil.setSessionMapValue("remessa", remessaVO);
			JSFUtil.setSessionMapValue("origem", "manutencao");
			if (remessaVO.getTipoRemessaMoviMentoDiario()) {
				FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoC");
			} else {
				FacesContext.getCurrentInstance().getExternalContext().redirect("rascunhoRemessaTipoAB");
			}
		} catch (Exception e) {
			facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
			logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "editarRemessa"));
		}
	}

	public void limparLocalizacao() {

		for (RemessaDocumentoVO remessaDoc : this.remessa.getRemessaDocumentos()) {
			remessaDoc.setDescricaoLocalizacao(null);
		}

	}

	public boolean exibirSituacaoAberta(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.ABERTA.getId())) {
			return true;
		}
		return false;
	}

	public boolean exibirSituacaoAlteracoes(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.EM_ALTERACAO.getId())
				|| situacao.getId().equals(SituacaoRemessaEnum.ALTERADA.getId())
				|| situacao.getId().equals(SituacaoRemessaEnum.EM_DISPUTA.getId())) {
			return true;
		} else {
			return false;
		}
	}

	public boolean exibirSituacaoAgendada(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.AGENDADA.getId())) {
			return true;
		}
		return false;
	}

	public boolean exibirSituacaoRecebida(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.RECEBIDA.getId())) {
			return true;
		}
		return false;
	}

	public boolean exibirSituacaoPossivelFechamento(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.CONFERIDA.getId())
				|| situacao.getId().equals(SituacaoRemessaEnum.ALTERACAO_CONFIRMADA.getId())
				|| situacao.getId().equals(SituacaoRemessaEnum.ALTERACAO_DESFEITA.getId())) {
			return true;
		}
		return false;
	}

	public boolean exibirSituacaoInvalida(RemessaVO remessa) {

		SituacaoRemessaVO situacao = remessa.getTramiteRemessaAtual().getSituacao();
		if (situacao.getId().equals(SituacaoRemessaEnum.INVALIDA.getId())) {
			return true;
		}
		return false;
	}

	/**
	 * Prepara os objetos necessários para o modal de visualização de uma remessa
	 * DEVOLVIDA/INVÁLIDA.
	 * 
	 * @param remessa
	 */
	public void exibirMotivoDevolucaoInvalidacao(RemessaVO remessa) {
		this.remessa = remessa;
		tramiteRemessaVO = new TramiteRemessaVO();
		tramiteRemessaVO = remessa.getTramiteRemessaAtual();
		showDialog("modalDevolvidaInvalida");
	}

	public void visualizarRemessaDocumento(RemessaDocumentoVO remessaDocumentoVO, boolean retorna) {
		try {
			remessaDocumento = remessaDocumentoVO;
			remessa = remessaDocumentoVO.getRemessa();
			retornaModal = retorna;
			grupo = grupoService.obterGrupo(remessaDocumento.getRemessa().getDocumento());

			if (grupo != null) {
				recuperarCamposDinamicos(remessaDocumento);
				showDialog("modalRemessaDocumento");
			} else {
				facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			}

		} catch (AppException e) {
			facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			logger.error(
					LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultaRemessa", "visualizarRemessaDocumento"));
		}

	}

	private void recuperarCamposDinamicos(RemessaDocumentoVO vo) throws AppException {
		if (grupo == null) {
			return;
		}

		try {
			GrupoCamposHelper.getValorCamposDinamicos(vo, grupo.getGrupoCampos());
		} catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
			throw new AppException(e.getMessage());
		}
	}

	public String calculaTamanhoCampo(String legenda, String descricao, int tamanhoCampo, SimNaoEnum obrigatorio) {
		int tamanhoLabel = (!Util.isNullOuVazio(legenda) ? legenda.length() : descricao.length());
		if (tamanhoLabel < tamanhoCampo) {
			return "width: " + (tamanhoCampo * 10) + "px";
		} else {
			return "width: " + (tamanhoLabel * 6) + "px";
		}
	}

	public boolean isContemMascara(String mascara) {
		return (mascara.contains(".") || mascara.contains("/") || mascara.contains("-")) && !mascara.contains("0,00");
	}

	public boolean isMascaraNumerica(String mascara) {
		if (isContemMascara(mascara))
			return false;

		return mascara.matches("^[0-9]{1,}$");
	}

	public boolean isMascaraMoeda(String mascara) {
		return !isContemMascara(mascara) && mascara.contains("0,00");
	}

	public void gerarCapaDeLote(RemessaDocumentoVO remessaDocumento) {
		byte[] bytesArray = imprimirCapaDeLote(remessaDocumento);
		InputStream stream = new ByteArrayInputStream(bytesArray);
		// setCapaLote(new DefaultStreamedContent(stream, "application/pdf",
		// "CapaLoteItem" +
		// remessaDocumento.getNuItem().toString() + ".pdf"));
	}

	public byte[] imprimirCapaDeLote(RemessaDocumentoVO remessaDocumento) {

		try {

			// String campoEspecieDocumento = "";

			remessaDocumento = remessaDocumentoService.findByIdEager((Long) remessaDocumento.getId());

			HashMap<String, Object> parametros = new HashMap<String, Object>();
			// parametros.put("NUMERO_ITEM",
			// remessaDocumento.getNuItem().toString().trim());
			parametros.put("NUMERO_REMESSA", remessaDocumento.getRemessa().getId().toString().trim());
			parametros.put("NOME_UF", remessaDocumento.getUnidadeGeradora().getUf().getNome().trim());
			parametros.put("NOME_UF_UNIDADE", remessaDocumento.getUnidadeGeradora().getNome().trim());
			parametros.put("SG_UF_UNIDADE", remessaDocumento.getUnidadeGeradora().getUf().getId().toString().trim());
			parametros.put("NU_UNIDADE", remessaDocumento.getUnidadeGeradora().getId().toString());

			if (!Util.isNullOuVazio(remessaDocumento.getDataInicio())
					&& !Util.isNullOuVazio(remessaDocumento.getDataFim())) {
				parametros.put("CAMPOS_DATA",
						Util.formatData(remessaDocumento.getDataInicio(), Constantes.FORMATO_DATA).trim() + " - "
								+ Util.formatData(remessaDocumento.getDataFim(), Constantes.FORMATO_DATA).trim());
			} else if (!Util.isNullOuVazio(remessaDocumento.getDataInicio())) {
				parametros.put("CAMPOS_DATA",
						Util.formatData(remessaDocumento.getDataInicio(), Constantes.FORMATO_DATA).trim());
			} else if (!Util.isNullOuVazio(remessaDocumento.getDataFim())) {
				parametros.put("CAMPOS_DATA",
						Util.formatData(remessaDocumento.getDataFim(), Constantes.FORMATO_DATA).trim());
			} else if (!Util.isNullOuVazio(remessaDocumento.getDataGeracao())) {
				parametros.put("CAMPOS_DATA",
						Util.formatData(remessaDocumento.getDataGeracao(), Constantes.FORMATO_DATA).trim());
			}

			// if(!Util.isNullOuVazio(remessaDocumento.getDataFragmentacao()) ) {
			// parametros.put("DT_FRAGMENTACAO",
			// Util.formatData(remessaDocumento.getDataFragmentacao(),
			// Constantes.FORMATO_DATA).trim());
			// }

			parametros.put("DT_ATUAL", Util.formatData(new Date(), Constantes.FORMATO_DATA));

			return new ReportUtils().obterRelatorio(
					getClass().getClassLoader()
							.getResourceAsStream(Constantes.CAMINHO_JASPER.concat("Capa_lote.jasper")),
					parametros, null, ReportUtils.REL_TIPO_PDF);

		} catch (Exception e) {
			facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "imprimirCapaDeLote"));
		}

		return null;
	}

	/**
	 * Exporta o Relatorio De Acordo com o nome Informado
	 * 
	 * @param document
	 */
	public void exportarDatagrid() {
		HttpServletResponse res = JSFUtil.getServletResponse();
		res.setContentType("application/vnd.ms-excel");
		res.setHeader("Content-disposition",
				"attachment; filename=Manutencao_de_Remessa_" + Util.formatDataHoraNomeArquivo(new Date()) + ".xls");
		try {
			ServletOutputStream outputStream = res.getOutputStream();
			exportarTabelaXLS(outputStream);
			outputStream.flush();
			outputStream.close();
			FacesContext faces = FacesContext.getCurrentInstance();
			faces.responseComplete();
		} catch (Exception e) {
			RequestContext.getCurrentInstance().execute("hideStatus();");
			facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultaRemessa", "exportarDatagrid"));
		}

	}

	public void exportarDatagridCsv() throws AppException {
		HttpServletResponse res = JSFUtil.getServletResponse();
		res.setContentType("application/vnd.ms-excel");
		res.setHeader("Content-disposition",
				"attachment; filename=RelatorioRemessa_" + Util.formatDataHoraNomeArquivo(new Date()) + ".csv");
		try {
			ServletOutputStream outputStream = res.getOutputStream();

			this.exportarTabelaParaCsv(outputStream);

			outputStream.flush();
			outputStream.close();

			FacesContext faces = FacesContext.getCurrentInstance();
			faces.responseComplete();

		} catch (IOException e) {
			RequestContext.getCurrentInstance().execute("hideStatus();");
			facesMessager.addMessageError(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
			logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "ConsultarRemessa", "exportarDatagridCsv"));
		}
	}

	public void exportarTabelaXLS(ServletOutputStream outputStream) throws IOException, AppException {
		List<RemessaVO> listaRemessa = pesquisar(0, 0);

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Sheet0");
		HSSFCellStyle estiloTituloColuna = RelatorioSiredUtil.getEstiloTituloColuna(wb);
		HSSFCellStyle estiloCelula = RelatorioSiredUtil.getEstiloCelulaCentralizado(wb);
		HSSFCellStyle estiloCelulaNegrito = RelatorioSiredUtil.getEstiloCelulaNegrito(wb);

		int linha = 2;
		int numeroCelula;
		int maximoCelulas = 11; // valor inicial igual às células do cabeçalho
		for (int i = 0; i < listaRemessa.size(); i++) {

			final HSSFRow linhaCabecalho = sheet.createRow(linha);
			numeroCelula = 0;

			RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "Nº OU CÓD. REMESSA");
			RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "UNIDADE SOLICITANTE");
			RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "USUÁRIO");
			RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "ABERTURA");
			RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "AGENDAMENTO");
			RelatorioSiredUtil.adicionaCelula(linhaCabecalho, numeroCelula++, estiloTituloColuna, "SITUAÇÃO");

			linha++;

			final HSSFRow rowLineRemessa = sheet.createRow(linha);
			numeroCelula = 0;

			final RemessaVO remessaPai = listaRemessa.get(i);

			// -- Nº REMESSA
			if (remessaPai.getTipoRemessaMoviMentoDiario()) {
				RelatorioSiredUtil.adicionaCelula(rowLineRemessa, numeroCelula++, estiloCelulaNegrito,
						remessaPai.getCodigoRemessaTipoC().toString());
			} else {
				RelatorioSiredUtil.adicionaCelula(rowLineRemessa, numeroCelula++, estiloCelulaNegrito,
						remessaPai.getId().toString());
			}

			// -- UNIDADE SOLICITANTE
			RelatorioSiredUtil.adicionaCelula(rowLineRemessa, numeroCelula++, estiloCelula,
					remessaPai.getUnidadeSolicitante().getDescricaoCompleta());

			// -- USUARIO
			RelatorioSiredUtil.adicionaCelula(rowLineRemessa, numeroCelula++, estiloCelula,
					remessaPai.getCodigoUsuarioAbertura());

			// -- ABERTURA
			RelatorioSiredUtil.adicionaCelula(rowLineRemessa, numeroCelula++, estiloCelula,
					remessaPai.getDataHoraAberturaFormatada());

			// -- AGENDAMENTO
			RelatorioSiredUtil.adicionaCelula(rowLineRemessa, numeroCelula++, estiloCelula,
					remessaPai.getDataAgendamentoFormatada() != null ? remessaPai.getDataAgendamentoFormatada()
							: " -- ");

			// -- SITUAÇÃO
			RelatorioSiredUtil.adicionaCelula(rowLineRemessa, numeroCelula++, estiloCelula,
					remessaPai.getTramiteRemessaAtual().getSituacao().getNome());

			linha++;

			// -- TIPO C
			if (remessaPai.getTipoRemessaMoviMentoDiario()) {

				final List<MovimentoDiarioRemessaCDTO> listaMovDiario = remessaPai.getDataMovimentosList();
				final HSSFRow rowHeadDetalhe = sheet.createRow(linha);

				numeroCelula = 3;

				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "DATA GERAÇÃO");
				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "UNIDADE GERADORA");
				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "DOCUMENTO");
				
				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "LOTÉRICO");
				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "Nº DO TERMINAL");
				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "Nº GRUPO 1");
				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "Nº GRUPO 2");
				RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelula++, estiloTituloColuna, "Nº GRUPO 3");

				for (MovimentoDiarioRemessaCDTO mov : listaMovDiario) {
					
					for (final RemessaMovimentoDiarioVO subitem : mov.getRemessaMovDiarioList()) {
						
						linha++;
						final HSSFRow rowLineDetalhe = sheet.createRow(linha);
						numeroCelula = 3;

						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, subitem.getDataFormatada());
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, subitem.getNomeUnidadeFormatada());
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, remessaPai.getDocumento().getNome());
						
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, subitem.getIcLoterico().getDescricao());
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, subitem.getNuTerminal().toString());
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, subitem.getGrupo1().toString());
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, subitem.getGrupo2().toString());
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, subitem.getGrupo3().toString());
					}
				}

				linha++;

				HSSFRow rowRodape = sheet.createRow(linha);
				rowRodape.createCell(0).setCellValue("");
				sheet.addMergedRegion(new CellRangeAddress(linha, linha, 0, maximoCelulas - 1));

				linha++;
				
			} else {

				// -- TIPO A/B
				final List<RemessaDocumentoVO> listaDocumentos = remessaPai.getRemessaDocumentos();
				
				for (RemessaDocumentoVO doc : listaDocumentos) {
					
					final HSSFRow rowHeadDetalhe = sheet.createRow(linha);
					
					int numeroCelulaHeader = 2;
					
					RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelulaHeader++, estiloTituloColuna, "CAIXA ARQUIVO");
					RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelulaHeader++, estiloTituloColuna, "DOCUMENTO");
					RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelulaHeader++, estiloTituloColuna, "UNIDADE GERADORA");
					RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelulaHeader++, estiloTituloColuna, "DATA DE GERAÇÃO");
					
					if (!doc.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO) && !doc
							.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_BASE_ARQUIVO)) {
						
						linha++;
						
						final HSSFRow rowLineDetalhe = sheet.createRow(linha);
						
						numeroCelula = 2;
						
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula,
								doc.getCodigoRemessa().toString());
						
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula,
								doc.getDocumento().getNome());
						
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula,
								doc.getUnidadeGeradora().getDescricaoCompleta());
						
						RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula,
								doc.getDataGeracaoFormatada());
						
						this.documento = doc.getDocumento();

						if (documento != null) {
							this.grupo = this.grupoService.obterGrupo(documento);
						}

						try {
							this.recuperarCamposDinamicos(doc);
						} catch (AppException e) {
							e.printStackTrace();
						}

						if (grupo != null) {
							for (GrupoCampoVO fc : grupo.getGrupoCampos()) {
								
								String valor = StringUtils.EMPTY;
								final String campo = fc.getCampo().getDescricao().toUpperCase();

								if (ObjectUtils.isCampoData(fc.getCampo())) {
									valor = DateUtils.format(fc.getValorData(), DateUtils.DEFAULT_FORMAT);
								} else {
									valor = fc.getValor();
								}
								
								//-- COLUNAS DINÂMICAS
								RelatorioSiredUtil.adicionaCelula(rowHeadDetalhe, numeroCelulaHeader++, estiloTituloColuna, campo);
								RelatorioSiredUtil.adicionaCelula(rowLineDetalhe, numeroCelula++, estiloCelula, valor);
							}
						}
					}
					
					linha++;
				}
				
				linha++;
				HSSFRow rowRodape = sheet.createRow(linha);
				rowRodape.createCell(0).setCellValue("");
				sheet.addMergedRegion(new CellRangeAddress(linha, linha, 0, maximoCelulas - 1));

				linha++;
			}
		}

		RelatorioSiredUtil.criarCabecalhoPadraoRelatorio(wb, sheet, maximoCelulas);
		RelatorioSiredUtil.criarCabecalhoNomeRelatorio(wb, sheet, maximoCelulas, "MANUTENÇÃO DE REMESSA");
		RelatorioSiredUtil.definirTamanhoCelulas(wb, sheet, sheet.getLastRowNum());
		wb.write(outputStream);
	}

	public void exportarTabelaParaCsv(ServletOutputStream outputStream) throws IOException, AppException {

		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();

		List<RemessaVO> listaRemessa = pesquisar(0, 0);

		for (int i = 0; i < listaRemessa.size(); i++) {

			// -- Tipo A/B
			final RemessaVO remessaPai = listaRemessa.get(i);

			for (int j = 0; j < remessaPai.getRemessaDocumentos().size(); j++) {

				final Map<String, String> itens = new LinkedHashMap<String, String>();

				// -- CAMPOS DA REMESSA
				itens.put("NU_REMESSA", remessaPai.getId().toString());
				itens.put("UNIDADE_SOLICITANTE", remessaPai.getUnidadeSolicitante().getDescricaoCompleta());
				itens.put("USUARIO", remessaPai.getCodigoUsuarioAbertura());
				itens.put("DH_ABERTURA", remessaPai.getDataHoraAberturaFormatada());
				itens.put("DH_AGENDAMENTO",
						!Util.isNullOuVazio(remessaPai.getDataAgendamento()) ? remessaPai.getDataAgendamentoFormatada()
								: StringUtils.EMPTY);
				itens.put("SITUACAO", remessaPai.getTramiteRemessaAtual().getSituacao().getNome());

				final RemessaDocumentoVO documentoVO = remessaPai.getRemessaDocumentos().get(j);

				// -- CAMPOS DO ITEM DA REMESSA
				itens.put("CAIXA_ARQUIVO", ObjectUtils.isNullOrEmpty(documentoVO.getCodigoRemessa()) ? ""
						: documentoVO.getCodigoRemessa().toString());
				itens.put("NO_DOCUMENTO", ObjectUtils.isNullOrEmpty(remessaPai.getDocumento()) ? ""
						: remessaPai.getDocumento().getNome());
				itens.put("UNIDADE_GERADORA", documentoVO.getUnidadeGeradora().getDescricaoCompleta());

				this.documento = documentoVO.getDocumento();

				if (documento != null) {
					this.grupo = this.grupoService.obterGrupo(documento);
				}

				try {
					this.recuperarCamposDinamicos(documentoVO);
				} catch (AppException e) {
					e.printStackTrace();
				}

				if (grupo != null) {
					for (GrupoCampoVO fc : grupo.getGrupoCampos()) {

						String valor = StringUtils.EMPTY;
						final String campo = fc.getCampo().getNome().toUpperCase();

						if (ObjectUtils.isCampoData(fc.getCampo())) {
							valor = DateUtils.format(fc.getValorData(), DateUtils.DEFAULT_FORMAT);
						} else {
							valor = fc.getValor();
						}

						itens.put(campo, valor);
					}
				}

				itens.put("DATA", ObjectUtils.isNullOrEmpty(documentoVO.getDataGeracaoFormatada()) ? ""
						: documentoVO.getDataGeracaoFormatada());
				itens.put("PERÍODO", ObjectUtils.isNullOrEmpty(documentoVO.getDataGeracaoFormatada()) ? ""
						: documentoVO.getPeriodoFormatado());

				itens.put("OBSERVACAO",
						!Util.isNullOuVazio(documentoVO.getRemessa().getTramiteRemessaAtual().getObservacao())
								? documentoVO.getRemessa().getTramiteRemessaAtual().getObservacao()
								: StringUtils.EMPTY);

				maps.add(itens);
			}

			// -- Tipo C
			if (!ObjectUtils.isNullOrEmpty(remessaPai.getDataMovimentosList())) {

				// -- CAMPOS DO ITEM DA REMESSA
				for (final MovimentoDiarioRemessaCDTO movDiarioCDTO : remessaPai.getDataMovimentosList()) {

					for (final RemessaMovimentoDiarioVO subitem : movDiarioCDTO.getRemessaMovDiarioList()) {

						final Map<String, String> itens = new LinkedHashMap<String, String>();

						// -- CAMPOS DA REMESSA
						itens.put("NU_REMESSA", remessaPai.getId().toString());
						itens.put("UNIDADE_SOLICITANTE", remessaPai.getUnidadeSolicitante().getDescricaoCompleta());
						itens.put("USUARIO", remessaPai.getCodigoUsuarioAbertura());
						itens.put("DH_ABERTURA", remessaPai.getDataHoraAberturaFormatada());
						itens.put("DH_AGENDAMENTO",
								!Util.isNullOuVazio(remessaPai.getDataAgendamento())
										? remessaPai.getDataAgendamentoFormatada()
										: StringUtils.EMPTY);
						itens.put("SITUACAO", remessaPai.getTramiteRemessaAtual().getSituacao().getNome());

						itens.put("DATA_GERACAO", ObjectUtils.isNullOrEmpty(movDiarioCDTO.getDataFormatada()) ? ""
								: movDiarioCDTO.getDataFormatada());
						itens.put("UNIDADE_GERADORA", movDiarioCDTO.getNomeUnidadeFormatada());
						itens.put("NO_DOCUMENTO", ObjectUtils.isNullOrEmpty(remessaPai.getDocumento()) ? ""
								: remessaPai.getDocumento().getNome());

						this.documento = remessaPai.getDocumento();

						// -- CAMPOS DO SUBITEM DA REMESSA
						itens.put("IC_LOTERICO", subitem.getIcLoterico().getDescricao());
						itens.put("NU_TERMINAL", subitem.getNuTerminal().toString());
						itens.put("QT_GRUPO_1", subitem.getGrupo1().toString());
						itens.put("QT_GRUPO_2", subitem.getGrupo2().toString());
						itens.put("QT_GRUPO_3", subitem.getGrupo3().toString());

						maps.add(itens);
					}
				}
			}
		}

		final String SEPARADOR = ":";

		final String DELIMITADOR = ";";

		final String TEXTO_DOIS_PONTOS = ":";

		final String TEXTO_PONTO = ".";

		final String TEXTO_PONTO_VIRGULA = ";";

		final String TEXTO_HIFEM = "-";

		StringBuilder relatorio = new StringBuilder();

		for (Map<String, String> map : maps) {
			for (Entry<String, String> item : map.entrySet()) {
				String key = (item.getKey() == null ? StringUtils.EMPTY : item.getKey());
				String value = (item.getValue() == null ? StringUtils.EMPTY : item.getValue());
				value = value.replace(TEXTO_DOIS_PONTOS, TEXTO_HIFEM);
				value = value.replace(TEXTO_PONTO_VIRGULA, TEXTO_PONTO);
				value = StringUtil.removeAccents(value.trim());

				relatorio.append(key + SEPARADOR + value + DELIMITADOR);
			}

			relatorio.append(FileUtils.SYSTEM_EOL);
		}

		outputStream.write(relatorio.toString().getBytes());
	}

	public void visualizarRemessaDocumento(RemessaDocumentoVO remessaDocumentoVO) {

		try {
			this.remessaDocumento = remessaDocumentoVO;
			this.remessa = remessaDocumentoVO.getRemessa();
			grupo = null;
			grupo = grupoService.obterGrupoAlterados(this.remessaDocumento.getDocumento());

			if (grupo != null
					&& (this.remessaDocumento.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)
							|| this.remessaDocumento.getIcAlteracaoValida()
									.equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_TERCEIRIZADA))) {
				recuperarCamposDinamicos(remessaDocumento);
				showDialog("modalRemessaDocumento");
			} else if (grupo != null && this.remessaDocumento.getIcAlteracaoValida()
					.equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
				grupoOriginal = grupoService.obterGrupo(remessaDocumento.getDocumento());
				recuperarCamposDinamicos(this.remessaDocumento);
				recuperarCamposDinamicosOriginais(this.remessaDocumento.getNumeroRemessaTipoAB());
				validarValoresIguais();
				showDialog("modalRemessaDocumentoAlteracao");
			} else {
				facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
			}

		} catch (AppException e) {
			facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
			logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "MantemRemessa", "visualizarRemessaDocumento"));
		}

	}

	public void validarValoresIguais() throws AppException {
		List<GrupoCampoVO> gruposOriginais = new ArrayList<GrupoCampoVO>();
		gruposOriginais.addAll(grupoOriginal.getGrupoCampos());
		List<GrupoCampoVO> gruposAlterados = new ArrayList<GrupoCampoVO>();
		gruposAlterados.addAll(grupo.getGrupoCampos());

		for (GrupoCampoVO grupoCampoOriginal : gruposOriginais) {
			for (GrupoCampoVO grupoCampoAlterado : gruposAlterados) {
				if (grupoCampoOriginal.getCampo().equals(grupoCampoAlterado.getCampo())) {
					if (!ObjectUtils.isNullOrEmpty(grupoCampoOriginal.getValor())
							&& !ObjectUtils.isNullOrEmpty(grupoCampoAlterado.getValor())) {
						if (!grupoCampoOriginal.getValor().equals(grupoCampoAlterado.getValor())) {
							grupoCampoAlterado.setValoresDiferentes(true);
						}
					} else if ((!ObjectUtils.isNullOrEmpty(grupoCampoOriginal.getValor())
							&& ObjectUtils.isNullOrEmpty(grupoCampoAlterado.getValor()))
							|| (ObjectUtils.isNullOrEmpty(grupoCampoOriginal.getValor())
									&& !ObjectUtils.isNullOrEmpty(grupoCampoAlterado.getValor()))) {
						grupoCampoAlterado.setValoresDiferentes(true);
					}
				}
			}
		}
	}

	public List<GrupoCampoVO> getListGrupoCampoOriginal() {
		if (!ObjectUtils.isNullOrEmpty(grupoOriginal) && !ObjectUtils.isNullOrEmpty(grupoOriginal.getGrupoCampos())) {
			return CollectionUtils.asSortedList(grupoOriginal.getGrupoCampos());
		}

		return new ArrayList<GrupoCampoVO>();
	}

	private void recuperarCamposDinamicosOriginais(RemessaDocumentoVO vo) throws AppException {
		if (grupoOriginal == null) {
			return;
		}

		try {
			GrupoCamposHelper.getValorCamposDinamicos(vo, grupoOriginal.getGrupoCampos());
		} catch (br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException e) {
			throw new AppException(e.getMessage(), e);
		}
	}

	public void limparDetalheMovimentotipoC() {
		this.itemDetalheTipoC = null;
	}

	public List<RemessaMovimentoDiarioVO> carregarListasAlteracaoSubstituido() {
		if (this.itemDetalheTipoC != null) {
			List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoC.getRemessaMovDiarioList();
			List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = new ArrayList<RemessaMovimentoDiarioVO>();
			for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioList) {
				if (remessaMovimentoDiarioVO.getId() != null) {
					if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
							.equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)
							|| remessaMovimentoDiarioVO.getIcAlteracaoValida()
									.equals(SituacaoAlteracaoRemessaEnum.PADRAO)
							|| remessaMovimentoDiarioVO.getIcAlteracaoValida()
									.equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
						remessaMovimentoDiarioSubstituido.add(remessaMovimentoDiarioVO);
					}
				}
			}
			Collections.sort(remessaMovimentoDiarioSubstituido, new RemessaDocDiarioNumItemComparator());
			return remessaMovimentoDiarioSubstituido;
		}
		return null;
	}

	public List<RemessaMovimentoDiarioVO> carregarListasAlteracaoAlterado() {
		
		this.quantitativoZerado = false;
		
		if (this.itemDetalheTipoC != null) {
			
			List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoC.getRemessaMovDiarioList();
			List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = new ArrayList<RemessaMovimentoDiarioVO>();
			
			for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioList) {
				if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
						.equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
					remessaMovimentoDiarioSubstituido.add(remessaMovimentoDiarioVO);
					
					this.identificarQuantitativoZerado(remessaMovimentoDiarioVO);
				}
			}
			
			Collections.sort(remessaMovimentoDiarioSubstituido, new RemessaDocDiarioNumItemComparator());
			
			return remessaMovimentoDiarioSubstituido;
		}
		return null;
	}

	private void identificarQuantitativoZerado(final RemessaMovimentoDiarioVO itemRemessa) {
		
		if (! this.quantitativoZerado) {
			
			if(itemRemessa.isFlagValorDiferenteNuTerminal() && itemRemessa.getNuTerminal() == 0L) {
				this.quantitativoZerado = true;
			} else if (itemRemessa.isFlagValorDiferenteIcGrupo1() && itemRemessa.getGrupo1() == 0) {
				this.quantitativoZerado = true;
			} else if (itemRemessa.isFlagValorDiferenteIcGrupo2() && itemRemessa.getGrupo2() == 0) {
				this.quantitativoZerado = true;
			} else if (itemRemessa.isFlagValorDiferenteIcGrupo3() && itemRemessa.getGrupo3() == 0) {
				this.quantitativoZerado = true;
			}
		}
	}

	public void visualizarRemessaDocumentoMovimentoDiario(MovimentoDiarioRemessaCDTO mov) {
		this.itemDetalheTipoC = mov;
		RemessaVO remessaVO = new RemessaVO();
		List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoC.getRemessaMovDiarioList();
		for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioList) {
			if (remessaMovimentoDiarioVO.getId() != null) {
				remessaVO = remessaMovimentoDiarioVO.getRemessa();
				if (remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)
						|| remessaMovimentoDiarioVO.getIcAlteracaoValida()
								.equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
					carregarListaMovDiarioTratado();
					showDialog("modalDetalharRemessaDocumento");
					break;
				}
				if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
						.equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)
						|| remessaMovimentoDiarioVO.getIcAlteracaoValida()
								.equals(SituacaoAlteracaoRemessaEnum.REGISTRO_SUBSTITUIDO)) {
					if (remessaVO.getTramiteRemessaAtual().getSituacao().getId()
							.equals(SituacaoRemessaEnum.ALTERADA.getId())
							|| remessaVO.getTramiteRemessaAtual().getSituacao().getId()
									.equals(SituacaoRemessaEnum.EM_DISPUTA.getId())) {
						verificaValoresAlteradosTipoC();
						showDialog("modalDetalharRemessaDocumentoAlteracao");
						break;
					} else {
						carregarListaMovDiarioTratado();
						showDialog("modalDetalharRemessaDocumento");
						break;
					}
				}
			}
		}
	}

	public void verificaValoresAlteradosTipoC() {
		List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = carregarListasAlteracaoSubstituido();
		List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioAlterado = carregarListasAlteracaoAlterado();
		for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVOAlterado : remessaMovimentoDiarioAlterado) {
			for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVOSubstituido : remessaMovimentoDiarioSubstituido) {
				if (remessaMovimentoDiarioVOSubstituido.getId()
						.equals(remessaMovimentoDiarioVOAlterado.getNumeroRemessaTipoC().getId())) {
					if (!remessaMovimentoDiarioVOAlterado.getIcLoterico()
							.equals(remessaMovimentoDiarioVOSubstituido.getIcLoterico())) {
						remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcLoterico(true);
					}
					if (!remessaMovimentoDiarioVOAlterado.getNuTerminal()
							.equals(remessaMovimentoDiarioVOSubstituido.getNuTerminal())) {
						remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteNuTerminal(true);
					}
					if (!remessaMovimentoDiarioVOAlterado.getGrupo1()
							.equals(remessaMovimentoDiarioVOSubstituido.getGrupo1())) {
						remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcGrupo1(true);
					}
					if (!remessaMovimentoDiarioVOAlterado.getGrupo2()
							.equals(remessaMovimentoDiarioVOSubstituido.getGrupo2())) {
						remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcGrupo2(true);
					}
					if (!remessaMovimentoDiarioVOAlterado.getGrupo3()
							.equals(remessaMovimentoDiarioVOSubstituido.getGrupo3())) {
						remessaMovimentoDiarioVOAlterado.setFlagValorDiferenteIcGrupo3(true);
					}
				}
			}
		}
	}

	private List<RemessaMovimentoDiarioVO> carregarListaMovDiarioTratado() {
		List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioListTratada = new ArrayList<RemessaMovimentoDiarioVO>();
		List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioListAux = new ArrayList<RemessaMovimentoDiarioVO>();
		if (this.itemDetalheTipoC != null) {
			List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioList = this.itemDetalheTipoC.getRemessaMovDiarioList();
			List<RemessaMovimentoDiarioVO> remessaMovimentoDiarioSubstituido = new ArrayList<RemessaMovimentoDiarioVO>();
			remessaMovimentoDiarioListTratada.addAll(remessaMovimentoDiarioList);
			remessaMovimentoDiarioListTratada.addAll(remessaMovimentoDiarioSubstituido);
			for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovimentoDiarioListTratada) {
				if (remessaMovimentoDiarioVO.getId() != null) {
					if (remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)
							|| remessaMovimentoDiarioVO.getIcAlteracaoValida()
									.equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)
							|| remessaMovimentoDiarioVO.getIcAlteracaoValida()
									.equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
						remessaMovimentoDiarioListAux.add(remessaMovimentoDiarioVO);
					}
				}
			}
		}
		return remessaMovimentoDiarioListAux;
	}

	public boolean exibirBotaoAlteracao(RemessaDocumentoVO remessaDocumentoVO) {
		if (remessaDocumentoVO.getIcAlteracaoValida()
				.equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
			return true;
		}
		return false;
	}

	public boolean exibirBotaoAlteracaoTipoC(MovimentoDiarioRemessaCDTO mov) {
		List<RemessaMovimentoDiarioVO> listaMovimentos = new ArrayList<RemessaMovimentoDiarioVO>();
		listaMovimentos.addAll(mov.getRemessaMovDiarioList());
		for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listaMovimentos) {
			if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
					.equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
				return true;
			}
		}
		return false;
	}

	public boolean exibirBotaoAdicaoTipoC(MovimentoDiarioRemessaCDTO mov) {
		List<RemessaMovimentoDiarioVO> listaMovimentos = new ArrayList<RemessaMovimentoDiarioVO>();
		listaMovimentos.addAll(mov.getRemessaMovDiarioList());
		for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listaMovimentos) {
			if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
					.equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
				return true;
			}
		}
		return false;
	}

	public Boolean exibirMensagemInclusaoTipoC() {
		if (this.itemDetalheTipoC != null) {
			List<RemessaMovimentoDiarioVO> remessaMovDiarioList = this.itemDetalheTipoC.getRemessaMovDiarioList();

			for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovDiarioList) {
				if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
						.equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
					return true;
				}
			}
		}
		return false;
	}

	public void receberConfirmarFecharRemessa() throws Exception {
		tramiteRemessaVO.setDataTramiteRemessa(new Date());
		tramiteRemessaService.salvarTramiteRemessaRecebida(this.remessa);
		remessaService.update(this.remessa);
		tramiteRemessaService.salvarTramiteRemessaConferida(this.remessa);
		fecharRemessa();
		hideDialog("modalAcoesRemessa");
	}

	@Override
	protected RemessaVO newInstance() {
		return new RemessaVO();
	}

	@Override
	protected AbstractService<RemessaVO> getService() {
		return remessaService;
	}

	@Override
	public String nomeFuncionalidade() {
		return "ConsultaRemessa";
	}

	public SituacaoRemessaEnum[] getSituacoesRemessaFiltro() {
		return SituacaoRemessaEnum.valuesExtranet();
	}

	public boolean isPesquisaRealizada() {
		return pesquisaRealizada;
	}

	public void setPesquisaRealizada(boolean pesquisaRealizada) {
		this.pesquisaRealizada = pesquisaRealizada;
	}

	public String getNumRemessa() {
		return numRemessa;
	}

	public void setNumRemessa(String numRemessa) {
		this.numRemessa = numRemessa;
	}

	public Date getDataInicioFiltro() {
		return dataInicioFiltro;
	}

	public void setDataInicioFiltro(Date dataInicioFiltro) {
		this.dataInicioFiltro = dataInicioFiltro;
	}

	public Date getDataFimFiltro() {
		return dataFimFiltro;
	}

	public void setDataFimFiltro(Date dataFimFiltro) {
		this.dataFimFiltro = dataFimFiltro;
	}

	public Long getNuRemessaFiltro() {
		return nuRemessaFiltro;
	}

	public void setNuRemessaFiltro(Long nuRemessaFiltro) {
		this.nuRemessaFiltro = nuRemessaFiltro;
	}

	public String getCoUsuarioFiltro() {
		return coUsuarioFiltro;
	}

	public void setCoUsuarioFiltro(String coUsuarioFiltro) {
		this.coUsuarioFiltro = coUsuarioFiltro;
	}

	public UnidadeVO getUnidadeFiltro() {
		return unidadeFiltro;
	}

	public void setUnidadeFiltro(UnidadeVO unidadeFiltro) {
		this.unidadeFiltro = unidadeFiltro;
	}

	public String getCodigoUnidadeFiltro() {
		return codigoUnidadeFiltro;
	}

	public void setCodigoUnidadeFiltro(String codigoUnidadeFiltro) {
		this.codigoUnidadeFiltro = codigoUnidadeFiltro;
	}

	public String getNomeUnidadeFiltro() {
		return nomeUnidadeFiltro;
	}

	public void setNomeUnidadeFiltro(String nomeUnidadeFiltro) {
		this.nomeUnidadeFiltro = nomeUnidadeFiltro;
	}

	public String getFiltroHistorico() {
		return filtroHistorico;
	}

	public void setFiltroHistorico(String filtroHistorico) {
		this.filtroHistorico = filtroHistorico;
	}

	public SituacaoRemessaEnum getSituacaoFiltro() {
		return situacaoFiltro;
	}

	public void setSituacaoFiltro(SituacaoRemessaEnum situacaoFiltro) {
		this.situacaoFiltro = situacaoFiltro;
	}

	public List<RemessaVO> getListaFiltro() {
		return listaFiltro;
	}

	public void setListaFiltro(List<RemessaVO> listaFiltro) {
		this.listaFiltro = listaFiltro;
	}

	public LazyDataModel<RemessaVO> getListaRemessaModel() {
		return listaRemessaModel;
	}

	public void setListaRemessaModel(LazyDataModel<RemessaVO> listaRemessaModel) {
		this.listaRemessaModel = listaRemessaModel;
	}

	public boolean isPesquisaSucesso() {
		return pesquisaSucesso;
	}

	public void setPesquisaSucesso(boolean pesquisaSucesso) {
		this.pesquisaSucesso = pesquisaSucesso;
	}

	public List<RemessaVO> getListaRemessa() {
		return listaRemessa;
	}

	public void setListaRemessa(List<RemessaVO> listaRemessa) {
		this.listaRemessa = listaRemessa;
	}

	public List<TramiteRemessaVO> getListaTramitesRemessa() {
		return listaTramitesRemessa;
	}

	public void setListaTramitesRemessa(List<TramiteRemessaVO> listaTramitesRemessa) {
		this.listaTramitesRemessa = listaTramitesRemessa;
	}

	public List<TramiteRemessaVO> getFiltredListaTramitesRemessa() {
		return filtredListaTramitesRemessa;
	}

	public void setFiltredListaTramitesRemessa(List<TramiteRemessaVO> filtredListaTramitesRemessa) {
		this.filtredListaTramitesRemessa = filtredListaTramitesRemessa;
	}

	public RemessaVO getRemessa() {
		return remessa;
	}

	public void setRemessa(RemessaVO remessa) {
		this.remessa = remessa;
	}

	public StreamedContent getCapaLote() {
		return capaLote;
	}

	public void setCapaLote(StreamedContent capaLote) {
		this.capaLote = capaLote;
	}

	public RemessaDocumentoVO getRemessaDocumento() {
		return remessaDocumento;
	}

	public void setRemessaDocumento(RemessaDocumentoVO remessaDocumento) {
		this.remessaDocumento = remessaDocumento;
	}

	public GrupoVO getGrupo() {
		return grupo;
	}

	public void setGrupo(GrupoVO grupo) {
		this.grupo = grupo;
	}

	public List<GrupoCampoVO> getListGrupoCampo() {
		if (!ObjectUtils.isNullOrEmpty(grupo) && !ObjectUtils.isNullOrEmpty(grupo.getGrupoCampos())) {
			return CollectionUtils.asSortedList(grupo.getGrupoCampos());
		}

		return new ArrayList<GrupoCampoVO>();
	}

	public TramiteRemessaVO getTramiteRemessaVO() {
		return tramiteRemessaVO;
	}

	public void setTramiteRemessaVO(TramiteRemessaVO tramiteRemessaVO) {
		this.tramiteRemessaVO = tramiteRemessaVO;
	}

	public String[] getListaAcaoRemessa() {
		return listaAcaoRemessa;
	}

	public void setListaAcaoRemessa(String[] listaAcaoRemessa) {
		this.listaAcaoRemessa = listaAcaoRemessa;
	}

	public String getAcaoRemessa() {
		return acaoRemessa;
	}

	public void setAcaoRemessa(String acaoRemessa) {
		this.acaoRemessa = acaoRemessa;
	}

	public String getHoraAgendamento() {
		return horaAgendamento;
	}

	public void setHoraAgendamento(String horaAgendamento) {
		this.horaAgendamento = horaAgendamento;
	}

	public boolean isReagendar() {
		return reagendar;
	}

	public void setReagendar(boolean reagendar) {
		this.reagendar = reagendar;
	}

	public boolean isRetornaModal() {
		return retornaModal;
	}

	public void setRetornaModal(boolean retornaModal) {
		this.retornaModal = retornaModal;
	}

	/**
	 * @return the itemDetalheTipoC
	 */
	public MovimentoDiarioRemessaCDTO getItemDetalheTipoC() {
		return itemDetalheTipoC;
	}

	/**
	 * @param itemDetalheTipoC
	 *            the itemDetalheTipoC to set
	 */
	public void setItemDetalheTipoC(MovimentoDiarioRemessaCDTO itemDetalheTipoC) {
		this.itemDetalheTipoC = itemDetalheTipoC;
	}

	/**
	 * @return the grupoOriginal
	 */
	public GrupoVO getGrupoOriginal() {
		return grupoOriginal;
	}

	/**
	 * @param grupoOriginal
	 *            the grupoOriginal to set
	 */
	public void setGrupoOriginal(GrupoVO grupoOriginal) {
		this.grupoOriginal = grupoOriginal;
	}

	public Boolean hasQuantitativoZerado() {
		return quantitativoZerado;
	}

	public Long getNuLacreFiltro() {
		return nuLacreFiltro;
	}

	public void setNuLacreFiltro(Long nuLacreFiltro) {
		this.nuLacreFiltro = nuLacreFiltro;
	}

}
