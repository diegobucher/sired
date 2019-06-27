package br.gov.caixa.gitecsa.sired.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.arquitetura.util.JasperReportUtils;
import br.gov.caixa.gitecsa.ldap.AutenticadorLdap;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.PaginatorService;
import br.gov.caixa.gitecsa.sired.dao.RemessaDAO;
import br.gov.caixa.gitecsa.sired.dto.EtiquetaRemessaABDTO;
import br.gov.caixa.gitecsa.sired.dto.FiltroFaturamentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.dto.ItensTermoRemessaABDTO;
import br.gov.caixa.gitecsa.sired.dto.MovimentoDiarioRemessaCDTO;
import br.gov.caixa.gitecsa.sired.dto.RemessasAbertasDTO;
import br.gov.caixa.gitecsa.sired.dto.ResumoAtendimentoRemessaDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.helper.RemessaHelper;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.BaseVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.sired.vo.ViewRelatorioFaturamentoABVO;
import br.gov.caixa.gitecsa.util.JSFUtil;
import br.gov.caixa.gitecsa.util.ReportUtils;
import net.sf.jasperreports.engine.JRException;

@Stateless(name = "remessaDocumentoService")
public class RemessaService extends PaginatorService<RemessaVO> {

  private static final long serialVersionUID = 1497723163270770934L;

  private static final String USUARIO_SIRED = "SIRED";

  @Inject
  private RemessaDAO dao;

  @Inject
  private ParametroSistemaService parametroSistemaService;

  @Inject
  private UnidadeService unidadeService;

  @Inject
  protected FacesMensager facesMessager;

  @Inject
  private TramiteRemessaService tramiteRemessaService;

  @Inject
  private BaseService baseService;

  @Inject
  private SequencialRemessaService sequencialRemessaService;

  @Inject
  private RemessaDocumentoService remessaDocumentoService;

  @Inject
  private RemessaMovimentoDiarioService remessaMovimentoDiarioService;

  @Inject
  private FeriadoService feriadoService;

  private StreamedContent etiquetaMovDiario;
  
  private ReportUtils reportUtils;

  @Override
  protected void validaCamposObrigatorios(RemessaVO entity) {

  }

  @Override
  protected void validaRegras(RemessaVO entity) {

  }

  @Override
  protected void validaRegrasExcluir(RemessaVO entity) {

  }

  @Override
  public Integer count(Map<String, Object> filters) {
    return null;
  }

  @Override
  public List<RemessaVO> pesquisar(Integer offset, Integer limit, Map<String, Object> filters) {
    return null;
  }

  @Override
  protected GenericDAO<RemessaVO> getDAO() {
    return this.dao;
  }

  public List<RemessaVO> relatorioFaturamentoTipoC(FiltroFaturamentoRemessaDTO filtro) {
    return this.dao.relatorioFaturamentoTipoC((FiltroFaturamentoRemessaDTO) filtro);
  }

  public List<ViewRelatorioFaturamentoABVO> relatorioFaturamentoTipoAB(FiltroFaturamentoRemessaDTO filtro) {
    return this.dao.relatorioFaturamentoTipoAB((FiltroFaturamentoRemessaDTO) filtro);
  }

  public List<ResumoAtendimentoRemessaDTO> getResumoAtendimentos(Date dataInicio, Date dataFim) {
    return this.dao.getResumoAtendimentos(dataInicio, dataFim);
  }

  public Integer getQtdItensPorBase(BaseVO base, Date dataInicio, Date dataFim) {
    return this.dao.getQtdItensPorBase(base, dataInicio, dataFim);
  }

  public Integer getQtdRemessasPorBase(BaseVO base, Date dataInicio, Date dataFim) {
    return this.dao.getQtdRemessasPorBase(base, dataInicio, dataFim);
  }

  public Integer getQtdRemessasDentroPrazoPorBase(BaseVO base, Date dataInicio, Date dataFim) {
    try {
      Integer prazo = Integer.valueOf(this.parametroSistemaService.findByNome("PZ_COLETA_REMESSA").getVlParametroSistema());
      return this.dao.getQtdRemessasDentroPrazoPorBase(base, prazo, dataInicio, dataFim);
    } catch (Exception e) {
      return 0;
    }
  }

  public RemessaVO clonar(final RemessaVO remessa) throws BusinessException {

    try {

      UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
      UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
      RemessaVO clone = RemessaHelper.clonar(remessa, usuario, unidadeLotacao);
      clone.setCodigoRemessaTipoC(
          remessa.getCodigoRemessaTipoC() != null ? sequencialRemessaService.generate(remessa.getUnidadeSolicitante()) : null);
      clone.setEmpresaContrato(null);
      clone.setBase(null);
      // clone.setEmpresaContrato(this.baseService.findContratoBaseByUnidadeEager(unidadeLotacao));
      // clone.setBase(clone.getEmpresaContrato().getBase());
      this.save(clone);

      this.dao.update(clone);
      ArrayList<RemessaDocumentoVO> listRemessaDocumento = new ArrayList<RemessaDocumentoVO>();
      for (RemessaDocumentoVO item : remessa.getRemessaDocumentos()) {
        // O sistema só pode clonar os itens que estão na situação padrão
        if (item.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)) {
          RemessaDocumentoVO itemClone = (RemessaDocumentoVO) ObjectUtils.clone(item);
          itemClone.setId(null);
          itemClone.setRemessa(clone);
          itemClone.setCodigoRemessa(sequencialRemessaService.generate(clone.getUnidadeSolicitante()));
          itemClone.setCodigoUsuarioUltimaAlteracao(JSFUtil.getUsuario().getNuMatricula());
          itemClone.setDataUltimaAlteracao(new Date());
          itemClone.setDescricaoLocalizacao(null);
          itemClone = remessaDocumentoService.salvar(itemClone);
          listRemessaDocumento.add(itemClone);
        }
      }
      ArrayList<RemessaMovimentoDiarioVO> listRemessaDiario = new ArrayList<RemessaMovimentoDiarioVO>();
      if (remessa.getTipoRemessaMoviMentoDiario()) {
        for (RemessaMovimentoDiarioVO mov : remessa.getMovimentosDiarioList()) {
          // O sistema só pode clonar os itens que estão na situação padrão
          if (mov.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO)) {
            RemessaMovimentoDiarioVO movClone = (RemessaMovimentoDiarioVO) ObjectUtils.clone(mov);
            movClone.setId(null);
            movClone.setRemessa(clone);
            movClone.setCodigoUsuarioUltimaAlteracao(JSFUtil.getUsuario().getNuMatricula());
            movClone.setDataHoraUltimaAlteracao(new Date());
            movClone.setLocalizacao(null);
            movClone.setNumeroRemessaTipoC(null);
            movClone = remessaMovimentoDiarioService.salvarItemMovimentoDiario(movClone);
            listRemessaDiario.add(movClone);
          }
        }
      }
      clone.setRemessaDocumentos(listRemessaDocumento);
      clone.setMovimentosDiarioList(listRemessaDiario);

      TramiteRemessaVO tramite = this.tramiteRemessaService.getRascunho();
      tramite.setRemessa(clone);
      this.tramiteRemessaService.save(tramite);

      clone.setTramiteRemessaAtual(tramite);
      this.update(clone);

      return clone;

    } catch (BusinessException e) {
      throw e;
    } catch (Exception e) {
      throw new BusinessException(MensagemUtils.obterMensagem("MA012"));
    }
  }

  public RemessaVO salvarRascunho(RemessaVO remessa) throws Exception {
    if (remessa.getId() == null) {
      remessa.setCodigoRemessaTipoC(sequencialRemessaService.generate(remessa.getUnidadeSolicitante()));
      remessa = this.dao.save(remessa);
    } else {
      remessa = this.dao.update(remessa);
    }

    /** Inserir o Tramite após persistir a Remessa */
    remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaRascunho(remessa));
    // remessa = consultarContratoVigenteUnidadeGeradora(remessa);
    remessa = this.dao.update(remessa);
    return remessa;
  }

  public RemessaVO salvarRascunhoRemessaAB(RemessaVO remessa) throws Exception {
    if (remessa.getId() == null) {
      remessa = this.dao.save(remessa);
    } else {
      remessa = this.dao.update(remessa);
    }

    /** Inserir o Tramite após persistir a Remessa */
    remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaRascunho(remessa));
    remessa = this.dao.update(remessa);
    return remessa;
  }

  public RemessaVO obterRemessaComMovimentosDiarios(long id) {
    return this.dao.obterRemessaComMovimentosDiarios(id);
  }

  public void concluirRemessaMovimentoDiarioTipoC(RemessaVO remessa) throws Exception {
    remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaAberta(remessa));
    remessa.setCodigoUsuarioAbertura(JSFUtil.getUsuario().getNuMatricula());
    remessa = this.dao.update(remessa);
    remessa.setDataHoraAbertura(new Date());
    remessa.setTramiteRemessaAtual(tramiteRemessaService.salvarTramiteRemessaAgendada(remessa));
    remessa = this.dao.update(remessa);
    remessa.setDataAgendamento(remessa.getTramiteRemessaAtual().getDataAgendamento());
    remessa = consultarContratoVigenteUnidadeGeradora(remessa);
    remessa = this.dao.update(remessa);
  }

  /**
   * Verifica se existe contrato vigente para abrir a remessa. Tem que haver uma empresa contrato vinculada a Base da
   * Unidade Solicitante da Remessa.
   * @param remessa
   * @throws BusinessException
   * @throws DataBaseException
   */
  private RemessaVO consultarContratoVigenteUnidadeGeradora(RemessaVO remessa) throws BusinessException, DataBaseException {
    EmpresaContratoVO contrato = null;
    remessa = obterRemessaComMovimentosDiarios((Long) remessa.getId());
    /** Se a remessa for movimento diário. */
    if (remessa.getTipoRemessaMoviMentoDiario()) {
      if (remessa.getMovimentosDiarioList() != null && !remessa.getMovimentosDiarioList().isEmpty()) {
        contrato = this.baseService.findContratoBaseByUnidadeEager(remessa.getMovimentosDiarioList().get(0).getUnidadeGeradora());
        if (ObjectUtils.isNullOrEmpty(contrato)) {
          throw new BusinessException(MensagemUtils.obterMensagem("ME006",
              remessa.getMovimentosDiarioList().get(0).getUnidadeGeradora().getDescricaoCompleta()));
        }
      }
      /** Se a remessa for do tipo AB. */
    } else {
      if (remessa.getRemessaDocumentos() != null && !remessa.getRemessaDocumentos().isEmpty()) {
        contrato = this.baseService.findContratoBaseByUnidadeEager(remessa.getRemessaDocumentos().get(0).getUnidadeGeradora());
        if (ObjectUtils.isNullOrEmpty(contrato)) {
          throw new BusinessException(MensagemUtils.obterMensagem("ME006",
              remessa.getRemessaDocumentos().get(0).getUnidadeGeradora().getDescricaoCompleta()));
        }
      }
    }
    if (contrato != null) {
      remessa.setEmpresaContrato(contrato);
      remessa.setBase(contrato.getBase());
    }
    return remessa;
  }

  public List<MovimentoDiarioRemessaCDTO> obterAgrupamentoDeItensDeRemessaPorDiaUnidade(RemessaVO remessa) {

    List<MovimentoDiarioRemessaCDTO> dataMovimentosList = new ArrayList<>();
    List<RemessaMovimentoDiarioVO> listaC = new ArrayList<>();
    Map<String, List<RemessaMovimentoDiarioVO>> mapa = new HashMap<>();
    remessa = obterRemessaComMovimentosDiarios((Long) remessa.getId());

    String chave;
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    for (RemessaMovimentoDiarioVO item : remessa.getMovimentosDiarioList()) {
      chave = ((Long) item.getUnidadeGeradora().getId()).toString() + "#" + sdf.format(item.getDataMovimento());
      listaC = mapa.get(chave);
      if (listaC == null) {
        listaC = new ArrayList<>();
      }
      listaC.add(item);
      mapa.put(chave, listaC);
    }

    // Transforma o map na lista esperada
    MovimentoDiarioRemessaCDTO mov;
    for (String key : mapa.keySet()) {
      mov = new MovimentoDiarioRemessaCDTO();
      mov.setChave(key);
      mov.setDataMovimento(obterDataPorChave(key));
      mov.setRemessaMovDiarioList(mapa.get(key));
      dataMovimentosList.add(mov);
    }
    /** Ordena por Data */
    Collections.sort(dataMovimentosList);

    return dataMovimentosList;

  }

  private Date obterDataPorChave(String key) {
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    Date data = null;
    try {
      data = sdf.parse(StringUtils.substringAfter(key, "#"));
    } catch (ParseException e) {
      return null;
    }
    return data;
  }

  public StreamedContent imprimirEtiquetaMovDiario(RemessaVO remessa) throws Exception {

    byte[] bytesArray = gerarEtiquetaMovimentoDiario(remessa);
    InputStream stream = new ByteArrayInputStream(bytesArray);
    this.etiquetaMovDiario =
        new DefaultStreamedContent(stream, "application/pdf", "EtiquetaRemessa" + remessa.getId().toString() + ".pdf");
    return this.etiquetaMovDiario;
  }

  public byte[] gerarEtiquetaMovimentoDiario(RemessaVO remessa) {
    try {
      HashMap<String, Object> params = new HashMap<>();

      params.put("PARAM_COD_REMESSA", remessa.getCodigoRemessaTipoC().toString());
      params.put("PARAM_UNIDADE_SOLICITANTE", remessa.getUnidadeSolicitante().getDescricaoCompleta());
      params.put("PARAM_ABERTURA", remessa.getDataHoraAberturaFormatada());
      params.put("PARAM_LACRE", remessa.getLacre() != null ? remessa.getLacre().toString() : " -- ");
      params.put("PARAM_DIAS_MOVIMENTO", montarStringListaDatasMovimentosRemessa(remessa));
      params.put("PARAM_LOGO_CAIXA", String.format("%slogoCaixa.jpg", Constantes.REPORT_IMG_DIR));
      params.put("PARAM_ABERTURA", remessa.getDataHoraAberturaFormatada());

      List<MovimentoDiarioRemessaCDTO> remessaMovimentoDiario = remessa.getDataMovimentosList();
      List<RemessaMovimentoDiarioVO> listaMovDiario = new ArrayList<>();

      for (MovimentoDiarioRemessaCDTO remessaMovimentoDiarioDTO : remessaMovimentoDiario) {
        listaMovDiario.addAll(remessaMovimentoDiarioDTO.getRemessaMovDiarioList());
      }

      RemessaMovimentoDiarioVO primeiroMovDiarioDia = new RemessaMovimentoDiarioVO();
      /**
       * Validação da exibição do conteúdo: 1 - Caso a remessa possua o primeiro movimento cadastrado do dia, o
       * movimento será um novo movimento; 2 - Caso a remessa seja uma adição em uma remessa já cadastrada e a data dela
       * seja anterior a 90 dias da criação da remessa, será uma inserção sem custo; 3 - Caso a remessa seja uma adição
       * em uma remessa já cadastrada e a data dela seja superior a 90 dias da criação da remessa, será uma inserção com
       * custo;
       **/
      for (RemessaMovimentoDiarioVO remessaMov : listaMovDiario) {
        primeiroMovDiarioDia = remessaMovimentoDiarioService.findByUnidadeDtMovimento(remessaMov);
        
        if (remessaMov.getDataMovimento().equals(primeiroMovDiarioDia.getDataMovimento())
            && remessaMov.getUnidadeGeradora().getId().equals(primeiroMovDiarioDia.getUnidadeGeradora().getId())) {
          if (remessa.getId().equals(primeiroMovDiarioDia.getRemessa().getId())) {
            remessaMov.setConteudo("Novo Movimento");
          } else {
            if (isMaiorNoventaDias(remessaMov)) {
              remessaMov.setConteudo("Inserção (com custo)");
            } else {
              remessaMov.setConteudo("Inserção (sem custo)");
            }
          }
        } else {
          remessaMov.setConteudo("Novo Movimento");
        }
      }

      remessa = dao.findByIdFetchAll(remessa);
      params.put("PARAM_DESTINO",
          remessa.getBase().getUnidade().getSiglaUnidade() + "/" + remessa.getBase().getUnidade().getSiglaLocalizacao());
      params.put("PARAM_BARCODE", remessa.getCodigoRemessaTipoC().toString());

      InputStream jasper = this.getClass().getClassLoader()
          .getResourceAsStream(String.format("%sEtiquetaRemessaMovimentoDiario.jasper", Constantes.REPORT_BASE_DIR));

      reportUtils = new ReportUtils();
      return reportUtils.obterRelatorio(jasper, params, listaMovDiario, ReportUtils.REL_TIPO_PDF);

    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      logger.error(LogUtils.getMensagemPadraoLog(e.getMessage(), "EditaRemessa", "imprimirTermo"));
    }

    return new byte[0];
  }
  
  public Boolean isMaiorNoventaDias(RemessaMovimentoDiarioVO remessaMov) throws NumberFormatException, DataBaseException {
    Calendar dataMovimento = Calendar.getInstance();
    dataMovimento.setTime(remessaMov.getDataMovimento());
    dataMovimento.add(Calendar.DAY_OF_MONTH, Integer.valueOf(
        this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_PZ_MOVIMENTO_DIARIO).getVlParametroSistema()));

    Calendar dataAbertura = Calendar.getInstance();
    dataAbertura.setTime(remessaMov.getRemessa().getDataHoraAbertura());

    if (dataMovimento.after(dataAbertura)) {
      return false;
    } else {
      return true;
    }
  }

  public String montarStringListaDatasMovimentosRemessa(RemessaVO remessa) {
    String datas = " -- ";
    if (!remessa.getMovimentosDiarioList().isEmpty()) {
      String teste;
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      Map<Date, String> mapa = new HashMap<>();
      datas = "";
      for (RemessaMovimentoDiarioVO temp : remessa.getMovimentosDiarioList()) {
        teste = mapa.get(temp.getDataMovimento());
        if (teste == null) {
          mapa.put(temp.getDataMovimento(), sdf.format(temp.getDataMovimento()));
        }
      }
      List<Date> listaDatasOrdenadas = new ArrayList<>();
      for (Date data : mapa.keySet()) {
        listaDatasOrdenadas.add(data);
      }
      Collections.sort(listaDatasOrdenadas);
      for (Date date : listaDatasOrdenadas) {
        datas += mapa.get(date);
        datas += ", ";
      }
      datas = datas.substring(0, datas.length() - 2);
    }
    return datas;
  }

  public void atualizarRascunhoRemessa(RemessaVO remessa) {
    if (remessa.getId() == null) {
      remessa = this.dao.save(remessa);
    } else {
      remessa = this.dao.update(remessa);
    }
  }

  public RemessaVO obterRemessaComListaDocumentos(Long id) {
    return this.dao.obterRemessaComListaDocumentos(id);
  }

  public RemessaVO concluirRemessaDocumentosTipoAB(RemessaVO remessa) throws Exception {
    TramiteRemessaVO tramite = null;
    tramite = tramiteRemessaService.salvarTramiteRemessaAberta(remessa);
    remessa.setTramiteRemessaAtual(tramite);
    remessa = this.dao.update(remessa);
    remessa.setCodigoUsuarioAbertura(JSFUtil.getUsuario().getNuMatricula());
    remessa.setDataHoraAbertura(new Date());
    remessa = this.dao.update(remessa);
    remessa.setDataAgendamento(tramite.getDataAgendamento());
    remessa = consultarContratoVigenteUnidadeGeradora(remessa);
    remessa = this.dao.update(remessa);

    return remessa;
  }

  public StreamedContent gerarEtiquetaDocumentoAB(RemessaVO remessa) throws BusinessException, JRException, IOException {
    HashMap<String, Object> params = new HashMap<String, Object>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    params.put("PARAM_LOGO_CAIXA", String.format("%slogoCaixa.jpg", Constantes.REPORT_IMG_DIR));
    List<EtiquetaRemessaABDTO> lista = new ArrayList<>();
    for (RemessaDocumentoVO doc : remessa.getRemessaDocumentos()) {
      String matricula = doc.getRemessa().getCodigoUsuarioAbertura();
      UsuarioLdap usuario = consultaUsuarioLDAP(matricula);

      EtiquetaRemessaABDTO etiqueta = new EtiquetaRemessaABDTO();
      etiqueta.setCaixaArquivo(doc.getCodigoRemessa());
      etiqueta.setDataAbertura(sdf.format(doc.getRemessa().getDataHoraAbertura()));
      etiqueta.setDataExpurgo(doc.getDataFim() != null ? sdf.format(doc.getDataFim()) : "--");
      etiqueta.setNumeroRemessa(doc.getCodigoRemessa().toString());
      if (!ObjectUtils.isNullOrEmpty(usuario.getNomeUsuario())) {
        etiqueta.setResponsavel(usuario.getNomeUsuario());
      }
      etiqueta.setTipoDocumental(doc.getDocumento().getNome());
      etiqueta.setUnidadeGeradora(doc.getUnidadeGeradora().getDescricaoCompleta());
      lista.add(etiqueta);
    }

    InputStream jasper =
        this.getClass().getClassLoader()
            .getResourceAsStream(String.format("%sEtiquetaRemessaAB.jasper", Constantes.REPORT_BASE_DIR));

    String nomeArquivo = "EtiquetaRemessa" + remessa.getId().toString() + ".pdf";
    return JasperReportUtils.run(jasper, nomeArquivo, params, lista);
  }

  public StreamedContent gerarTermoResponsabilidade(RemessaVO remessa) throws BusinessException, JRException, IOException {

    HashMap<String, Object> params = new HashMap<String, Object>();

    String matricula = remessa.getCodigoUsuarioAbertura();
    UsuarioLdap usuario = consultaUsuarioLDAP(matricula);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());

    params.put("IMAGE_DIR", String.format("%slogoCaixa.jpg", Constantes.REPORT_IMG_DIR));
    params.put("CIDADE_LOTACAO", usuario.getCidade() != null ? usuario.getCidade().trim() : "--");
    params.put("DATA_ATUAL_EXTENSO", Util.dataPorExtenso(calendar).trim());
    params.put("NOME_EMPREGADO_LOGADO", usuario.getNomeUsuario() != null ? usuario.getNomeUsuario().trim() : "--");
    params.put("FUNCAO_LOGADO", usuario.getNoFuncao() != null ? usuario.getNoFuncao().trim() : "--");
    params.put("MATRICULA_LOGADO", usuario.getNuMatricula() != null ? usuario.getNuMatricula().toUpperCase().trim() : "--");
    params.put("UNIDADE_LOGADO", usuario.getCoUnidade() != null ? usuario.getCoUnidade().toString().trim() : "--");
    params.put("NOME_UNIDADE_LOGADO", usuario.getNoUnidade() != null ? usuario.getNoUnidade().trim() : "--");
    params.put("NUMERO_REMESSA", remessa.getId().toString());
    params.put("UNIDADE_REMESSA", remessa.getUnidadeSolicitante().getDescricaoCompleta());
    params.put("ABERTURA_REMESSA", remessa.getDataHoraAberturaFormatada());
    params.put("SITUACAO_REMESSA", remessa.getTramiteRemessaAtual().getSituacao().getNome());

    List<ItensTermoRemessaABDTO> listaItensRemessa = new ArrayList<>();
    for (RemessaDocumentoVO doc : remessa.getRemessaDocumentos()) {
      ItensTermoRemessaABDTO item = new ItensTermoRemessaABDTO();
      item.setCaixaArquivo(doc.getCodigoRemessa());
      item.setConteudo(doc.getDocumento().getNome());
      listaItensRemessa.add(item);
    }

    InputStream jasper =
        this.getClass().getClassLoader()
            .getResourceAsStream(String.format("%sTermo_responsabilidade.jasper", Constantes.REPORT_BASE_DIR));

    String nomeArquivo = "Remessa" + remessa.getId().toString() + ".pdf";
    return JasperReportUtils.run(jasper, nomeArquivo, params, listaItensRemessa);

  }

  public UsuarioLdap consultaUsuarioLDAP(String matricula) {
    UsuarioLdap usuarioAvaliacao = new UsuarioLdap();
    try {
      AutenticadorLdap autenticadorLdap = new AutenticadorLdap();
      usuarioAvaliacao =
          autenticadorLdap.pesquisarForaDoGrupoDoLdap(matricula.trim(), System.getProperty(Constantes.INTRANET_URL_LDAP));
    } catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }
    return usuarioAvaliacao;
  }

  public StreamedContent gerarEtiquetaDocumentoABIndividual(RemessaDocumentoVO doc)
      throws BusinessException, JRException, IOException {
    String matricula = doc.getRemessa().getCodigoUsuarioAbertura();
    UsuarioLdap usuario = consultaUsuarioLDAP(matricula);
    HashMap<String, Object> params = new HashMap<String, Object>();
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    params.put("PARAM_LOGO_CAIXA", String.format("%slogoCaixa.jpg", Constantes.REPORT_IMG_DIR));
    List<EtiquetaRemessaABDTO> lista = new ArrayList<>();
    EtiquetaRemessaABDTO etiqueta = new EtiquetaRemessaABDTO();
    etiqueta.setCaixaArquivo(doc.getCodigoRemessa());
    etiqueta.setDataAbertura(sdf.format(doc.getRemessa().getDataHoraAbertura()));
    etiqueta.setDataExpurgo(doc.getDataFim() != null ? sdf.format(doc.getDataFim()) : "--");
    etiqueta.setNumeroRemessa(doc.getCodigoRemessa().toString());
    if (!ObjectUtils.isNullOrEmpty(usuario.getNomeUsuario())) {
      etiqueta.setResponsavel(usuario.getNomeUsuario());
    }
    etiqueta.setTipoDocumental(doc.getDocumento().getNome());
    etiqueta.setUnidadeGeradora(doc.getUnidadeGeradora().getDescricaoCompleta());
    lista.add(etiqueta);

    InputStream jasper =
        this.getClass().getClassLoader()
            .getResourceAsStream(String.format("%sEtiquetaRemessaAB.jasper", Constantes.REPORT_BASE_DIR));

    String nomeArquivo = "EtiquetaRemessa" + doc.getRemessa().getId().toString() + ".pdf";
    return JasperReportUtils.run(jasper, nomeArquivo, params, lista);
  }

  public int confirmarRemessasPendentesAlteracao() throws NumberFormatException, DataBaseException, BusinessException {
    Date hoje = new Date();
    List<RemessaVO> listaRemessasTipoAB = dao.findAllPendentesConfirmacaoTipoAb();
    List<RemessaVO> listaRemessasTipoC = dao.findAllPendentesConfirmacaoTipoC();
    List<RemessaVO> listaRemessas = new ArrayList<RemessaVO>();
    listaRemessas.addAll(listaRemessasTipoAB);
    listaRemessas.addAll(listaRemessasTipoC);
    Integer prazo =
        Integer.valueOf(this.parametroSistemaService.findByNome(Constantes.PARAMETRO_SISTEMA_PZ_CONFIRMACAO_AUTOMATICA_REMESSA)
            .getVlParametroSistema());
    int totalConfirmadas = 0;
    for (RemessaVO remessaVO : listaRemessas) {
      Integer dias =
          this.feriadoService.getNumeroDiasUteis(hoje, remessaVO.getTramiteRemessaAtual().getDataTramiteRemessa(),
              remessaVO.getUnidadeSolicitante());
      if (dias > prazo) {
        try {
          // Cria um novo tramite...
          TramiteRemessaVO tramiteNovo =
              this.tramiteRemessaService.getBySituacaoRemessa(SituacaoRemessaEnum.ALTERACAO_CONFIRMADA, USUARIO_SIRED);
          tramiteNovo.setRemessa(remessaVO);
          remessaVO.setTramiteRemessaAtual(tramiteNovo);

          List<RemessaDocumentoVO> listaRemessaDocumento = new ArrayList<RemessaDocumentoVO>();
          if (!remessaVO.getRemessaDocumentos().equals(null)) {
            listaRemessaDocumento = remessaVO.getRemessaDocumentos();
            for (RemessaDocumentoVO remessaDocumentoVO : listaRemessaDocumento) {
              if (remessaDocumentoVO.getIcAlteracaoValida()
                  .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
                remessaDocumentoVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
                remessaDocumentoService.salvar(remessaDocumentoVO);
              }
            }
          }

          List<RemessaMovimentoDiarioVO> listaRemessaMovDiario = new ArrayList<RemessaMovimentoDiarioVO>();
          if (!remessaVO.getMovimentosDiarioList().equals(null)) {
            listaRemessaMovDiario = remessaVO.getMovimentosDiarioList();
            for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : listaRemessaMovDiario) {
              if (remessaMovimentoDiarioVO.getIcAlteracaoValida()
                  .equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
                remessaMovimentoDiarioVO.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
                remessaMovimentoDiarioService.salvarItemMovimentoDiario(remessaMovimentoDiarioVO);
              }
            }
          }

          totalConfirmadas++;
          tramiteRemessaService.salvar(tramiteNovo);
          dao.update(remessaVO);
        } catch (Exception e) {
          throw new BusinessException(e.getMessage());
        }
      }
    }
    return totalConfirmadas;
  }
  
  /**
   * Método que fecha todas as remessas na situação conferidas e alteração confirmada
   * Ele é chamado pelo job ConfirmacaoAutomaticaRemessaSchedule e retorna o valor de quantas remessas foram fechadas
   */
  public int fecharRemessasConferidasConfirmadas() {
    int totalFechadas = 0;
    
    List<RemessaVO> listaRemessas = dao.findAllConferidasConfirmadas();
    
    for(RemessaVO remessa : listaRemessas) {
      TramiteRemessaVO tramiteNovo =
          this.tramiteRemessaService.getBySituacaoRemessa(SituacaoRemessaEnum.FECHADA, USUARIO_SIRED);
      tramiteNovo.setRemessa(remessa);
      remessa.setTramiteRemessaAtual(tramiteNovo);
      
      tramiteRemessaService.salvar(tramiteNovo);
      dao.update(remessa);
      totalFechadas++;
    }
    return totalFechadas;
  }

  /**
   * @return the etiquetaMovDiario
   */
  public StreamedContent getEtiquetaMovDiario() {
    return etiquetaMovDiario;
  }

  /**
   * @param etiquetaMovDiario the etiquetaMovDiario to set
   */
  public void setEtiquetaMovDiario(StreamedContent etiquetaMovDiario) {
    this.etiquetaMovDiario = etiquetaMovDiario;
  }
  
  public Map<UnidadeVO, RemessasAbertasDTO> pesquisaAbertasHoje(Date hojeMeiaNoite, Date hoje){
    
    HashMap<UnidadeVO, RemessasAbertasDTO> mapRemessa = new HashMap<>();
    List<RemessaVO> remessas =  this.dao.pesquisaAbertasHojeAB(hojeMeiaNoite, hoje);
    List<RemessaVO> remessasTipoC =  this.dao.pesquisaAbertasHojeC(hojeMeiaNoite, hoje);
    
    if(!remessas.isEmpty()) {
      for (RemessaVO remessa : remessas) {
        if(!mapRemessa.containsKey(remessa.getUnidadeSolicitante())) {
          mapRemessa.put(remessa.getUnidadeSolicitante(), new RemessasAbertasDTO()); 
        }
        RemessasAbertasDTO remessaDTO = mapRemessa.get(remessa.getUnidadeSolicitante());
        List<RemessaVO> remessasList = remessaDTO.getRemessasList();
        remessasList.add(remessa);
        remessaDTO.setUnidade(remessa.getUnidadeSolicitante());
      }
    }
    
    if(!remessasTipoC.isEmpty()) {
      for (RemessaVO remessaVO : remessasTipoC) {
        if(!mapRemessa.containsKey(remessaVO.getUnidadeSolicitante())) {
          mapRemessa.put(remessaVO.getUnidadeSolicitante(), new RemessasAbertasDTO()); 
        }
        remessaVO.setDataMovimentosList(obterAgrupamentoDeItensDeRemessaPorDiaUnidade(remessaVO));
        RemessasAbertasDTO remessaDTO = mapRemessa.get(remessaVO.getUnidadeSolicitante());
        List<RemessaVO> remessasList = remessaDTO.getRemessasList();
        remessasList.add(remessaVO);
        remessaDTO.setUnidade(remessaVO.getUnidadeSolicitante());
      }
    }
    
    return mapRemessa;
  }

}
