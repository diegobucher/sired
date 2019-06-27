package br.gov.caixa.gitecsa.sired.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.EJBTransactionRolledbackException;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.omnifaces.cdi.ViewScoped;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import br.gov.caixa.gitecsa.arquitetura.controller.FacesMensager;
import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequiredException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.RequisicaoLoteException;
import br.gov.caixa.gitecsa.sired.dto.RemessaLoteDTO;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.ArquivoLoteService;
import br.gov.caixa.gitecsa.sired.service.DocumentoService;
import br.gov.caixa.gitecsa.sired.service.OperacaoService;
import br.gov.caixa.gitecsa.sired.service.RemessaDocumentoService;
import br.gov.caixa.gitecsa.sired.service.RemessaLoteService;
import br.gov.caixa.gitecsa.sired.service.RemessaService;
import br.gov.caixa.gitecsa.sired.service.SequencialRemessaService;
import br.gov.caixa.gitecsa.sired.service.UnidadeService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.RequestUtils;
import br.gov.caixa.gitecsa.sired.util.StringUtil;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;
import br.gov.caixa.gitecsa.sired.vo.DocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.GrupoCampoVO;
import br.gov.caixa.gitecsa.sired.vo.OperacaoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;
import br.gov.caixa.gitecsa.util.JSFUtil;

@Named
@ViewScoped
public class AberturaRemessaLoteController implements Serializable {

  private static final String REGEX_CAMPO_NUMERICO = "^[0-9,\\.]+$";

  private static final String REGEX_MARCADOR_OBRIGATORIO = "\\*";

  private static final String REGEX_SEPARADOR_HEADER = "\\|";

  private static final String REGEX_LINHA_HEADER = "(;+)$";

  private static final String SEPARADOR_NUM_REMESSAS_ABERTAS = ", ";

  private static final String NOME_ARQUIVO_LOG = "aberturaRemessaLoteRejeitado.txt";

  private static final long serialVersionUID = 1L;

  private static final long LINHA_HEADER = 2L;

  /*CAMPOS UTILIZADOS NO LPAD PARA QUE SEJA 
  INSERIDO SEMPRE A QUANTIDADE DE VALORES: 11 PARA CPF E 14 PARA CNPJ
  NA IMPORTAÇÃO DO ARQUIVO DE ABERTURA DE REMESSA EM LOTE*/ 
  private static final int QTD_DIGITOS_CPF = 11;

  private static final int QTD_DIGITOS_CNPJ = 14;

  @Inject
  protected FacesMensager facesMessager;

  @Inject
  private transient Logger logger;

  @Inject
  private UnidadeService unidadeService;

  @Inject
  private DocumentoService documentoService;

  @Inject
  private RemessaLoteService remessaLoteService;

  @Inject
  private SequencialRemessaService sequencialRemessaService;

  @Inject
  private ArquivoLoteService arquivoLoteService;

  @Inject
  private OperacaoService operacaoService;

  @Inject
  private RemessaService remessaService;

  @Inject
  private RemessaDocumentoService remessaDocumentoService;

  private File logFile;

  private UploadedFile file;

  private String filename;

  private String codigoDocumento;

  private String versaoDocumento;

  private Integer numeroLinhas = 0;

  private Integer numeroRejeitados = 0;

  private DocumentoVO documento;

  private List<String> camposPlanilha;

  private RemessaVO remessa;

  private UnidadeVO unidadeSolicitante;

  private Map<String, GrupoCampoVO> camposFormulario = new HashMap<String, GrupoCampoVO>();

  private ArrayList<String> msgValidacaoLog;

  private Boolean modoEdicaoRemessaAB;

  // total de registros duplicados no arquivo importado
  private Integer registrosDuplicados = 0;

  @PostConstruct
  protected void init() {
    try {
      this.setModoEdicaoRemessaAB(Boolean.FALSE);
      if (JSFUtil.getSessionMapValue("remessa") != null && JSFUtil.getSessionMapValue("modoEdicaoRemessaAB") != null) {
        this.modoEdicaoRemessaAB = (Boolean) JSFUtil.getSessionMapValue("modoEdicaoRemessaAB");
        this.remessa = (RemessaVO) JSFUtil.getSessionMapValue("remessa");
        this.remessa = remessaService.obterRemessaComListaDocumentos((Long) this.remessa.getId());
        JSFUtil.setSessionMapValue("remessa", null);
      } else {
        this.limparCampos();
      }
      this.unidadeSolicitante = unidadeService.findUnidadeLotacaoUsuarioLogado();
    } catch (Exception e) {
      facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.logger.error(e.getMessage(), e);
    }
  }

  private void limparCampos() {
    this.remessa = null;
  }

  public void validarArquivo() {
    Boolean invalido = false;
    try {
      MensagemUtils.setKeepMessages(true);

      if (ObjectUtils.isNullOrEmpty(this.file)) {
        invalido = true;
        this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA045"));
      } else if (!this.file.getFileName().toLowerCase().endsWith(".txt")
          && !this.file.getFileName().toLowerCase().endsWith(".csv")) {
        invalido = true;
        this.facesMessager.addMessageError(MensagemUtils.obterMensagem("ME021"));
      } else if (this.file.getSize() == 0L) {
        invalido = true;
        this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MI033"));
      } else {
        this.preProcessarArquivo();
      }

      this.logFile = null;
      this.numeroRejeitados = 0;
      this.msgValidacaoLog = null;

    } catch (BusinessException e) {
      invalido = true;
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(e.getMessage());
    } catch (Exception e) {
      invalido = true;
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
    } finally {
      if (invalido) {
        this.file = null;
        this.numeroLinhas = 0;
        this.codigoDocumento = StringUtils.EMPTY;
      }
    }
  }

  public Boolean getShowModalOnLoad() {
    return (ObjectUtils.isNullOrEmpty(this.file)) ? false : true;
  }

  public String processarArquivo() throws BusinessException {
    MensagemUtils.setKeepMessages(true);
    ArquivoLoteVO arquivoLote = null;

    try {
      this.msgValidacaoLog = null;
      arquivoLote =
          this.arquivoLoteService.salvar(this.file.getFileName(), (UsuarioLdap) RequestUtils.getSessionValue("usuario"));

      this.validacaoPreCriticaPlanilha();
      List<RemessaLoteDTO> remessaDocumentos = this.obterItensRemessaLote();
      List<RemessaDocumentoVO> remessaDocumentoVOList = new ArrayList<RemessaDocumentoVO>();

      // Caso a remessa seja nova, o sistema deverá adicionar todos os documentos e exibir mensagem de sucesso
      if (this.remessa == null) {
        this.remessa = inicializarRemessa();
        montaListaRemessaDocumentos(remessaDocumentoVOList, remessaDocumentos);
        this.remessa.setRemessaDocumentos(remessaDocumentoVOList);
        this.remessaLoteService.salvar(remessaDocumentos, this.remessa);
        this.msgValidacaoLog = null;
        String mensagem =
            String.format("Foi aberta a Remessa de número %s com %s item(ns). A Remessa está como rascunho e precisa ser concluída.", this.remessa.getId(), remessaDocumentos.size());
        this.facesMessager.addMessageInfo(mensagem);
        if (registrosDuplicados > 0) {
          mensagem += System.getProperty("line.separator");
          mensagem += "Houveram linhas duplicadas no arquivo, e elas foram ignoradas.";
        }
        init();
      }
      /*
       * Caso a remessa já exista (edição de rascunho de remessa), o sistema deverá adicionar os documentos à remessa e
       * retornar a página de alteração de rascunho de remessa
       */
      else {
        JSFUtil.setSessionMapValue("remessa", this.remessa);
        remessaDocumentoVOList = this.remessa.getRemessaDocumentos();
        montaListaRemessaDocumentos(remessaDocumentoVOList, remessaDocumentos);
        String mensagem = String.format("Foi(ram) importado(s) %s item(ns) à remessa. A Remessa está como rascunho e precisa ser concluída.", remessaDocumentos.size());
        this.msgValidacaoLog = null;
        this.facesMessager.addMessageInfo(mensagem);
        return "/paginas/remessa/rascunhoRemessaTipoAB.xhtml?faces-redirect=true";
      }
    } catch (RequisicaoLoteException e) {
      this.numeroRejeitados = e.getNumeroRejeitados();
      this.msgValidacaoLog = new ArrayList<String>(e.getErroList());
      this.arquivoLoteService.concluir(arquivoLote, true);
    } catch (BusinessException e) {
      if (!ObjectUtils.isNullOrEmpty(e.getErroList())) {
        this.msgValidacaoLog = new ArrayList<String>(e.getErroList());
      } else {
        this.facesMessager.addMessageError(e.getMessage());
      }
      this.arquivoLoteService.concluir(arquivoLote, true);
    } catch (IOException e) {
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.arquivoLoteService.concluir(arquivoLote, true);
    } catch (EJBTransactionRolledbackException e) {
      e.printStackTrace();
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(MensagemUtils.obterMensagem("ME030"));
      this.arquivoLoteService.concluir(arquivoLote, true);
    } catch (AppException e) {
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.arquivoLoteService.concluir(arquivoLote, true);
    } catch (Exception e) {
      e.printStackTrace();
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
      this.arquivoLoteService.concluir(arquivoLote, true);
    }
    return null;
  }

  private List<RemessaDocumentoVO> montaListaRemessaDocumentos(List<RemessaDocumentoVO> remessaDocumentoVOList,
      List<RemessaLoteDTO> remessaDocumentos) {
    UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");

    for (RemessaLoteDTO remessaLoteDtoList : remessaDocumentos) {
      RemessaDocumentoVO remessaDocumento = new RemessaDocumentoVO();
      remessaDocumento.setRemessa(this.remessa);
      remessaDocumento.setDocumento(this.documento);
      remessaDocumento.setUnidadeGeradora(remessaLoteDtoList.getRemessaDocumento().getUnidadeGeradora());
      remessaDocumento.setCodigoUsuarioUltimaAlteracao(usuario.getNuMatricula());
      remessaDocumento.setDescricaoLocalizacao(remessaLoteDtoList.getRemessaDocumento().getDescricaoLocalizacao());
      remessaDocumento.setNuValor(remessaLoteDtoList.getRemessaDocumento().getNuValor());
      remessaDocumento.setNoNome(remessaLoteDtoList.getRemessaDocumento().getNoNome());
      remessaDocumento.setNuDocumento(remessaLoteDtoList.getRemessaDocumento().getNuDocumento());
      remessaDocumento.setNuVolume(remessaLoteDtoList.getRemessaDocumento().getNuVolume());
      remessaDocumento.setNuContaInicio(remessaLoteDtoList.getRemessaDocumento().getNuContaInicio());
      remessaDocumento.setNuContaFim(remessaLoteDtoList.getRemessaDocumento().getNuContaFim());
      remessaDocumento.setNuConta(remessaLoteDtoList.getRemessaDocumento().getNuConta());
      remessaDocumento.setNuOperacao(remessaLoteDtoList.getRemessaDocumento().getNuOperacao());
      remessaDocumento.setNuDigitoVerificador(remessaLoteDtoList.getRemessaDocumento().getNuDigitoVerificador());
      remessaDocumento.setDataGeracao(remessaLoteDtoList.getRemessaDocumento().getDataGeracao());
      remessaDocumento.setDataInicio(remessaLoteDtoList.getRemessaDocumento().getDataInicio());
      remessaDocumento.setDataFim(remessaLoteDtoList.getRemessaDocumento().getDataFim());
      remessaDocumento.setDataUltimaAlteracao(new Date());
      remessaDocumento.setNuEncerramento(remessaLoteDtoList.getRemessaDocumento().getNuEncerramento());
      remessaDocumento.setNumeroCpf(StringUtils.leftPad(remessaLoteDtoList.getRemessaDocumento().getNumeroCpf(), QTD_DIGITOS_CPF, '0'));
      remessaDocumento.setNumeroCnpj(StringUtils.leftPad(remessaLoteDtoList.getRemessaDocumento().getNumeroCnpj(), QTD_DIGITOS_CNPJ, '0'));
      remessaDocumento.setNomeInicio(remessaLoteDtoList.getRemessaDocumento().getNomeInicio());
      remessaDocumento.setNomeFim(remessaLoteDtoList.getRemessaDocumento().getNomeFim());
      remessaDocumento.setNumeroInicio(remessaLoteDtoList.getRemessaDocumento().getNumeroInicio());
      remessaDocumento.setNumeroFim(remessaLoteDtoList.getRemessaDocumento().getNumeroFim());
      remessaDocumento.setIcAlteracaoValida(SituacaoAlteracaoRemessaEnum.PADRAO);
      remessaDocumento.setNumeroFolder(remessaLoteDtoList.getRemessaDocumento().getNumeroFolder());

      /* Caso a remessa já exista (edição de remessa), o sistema 
       * salva a remessaDocumento que já está vinculada à remessa */ 
      if (this.remessa.getId() != null) {
        try {
          remessaDocumento.setCodigoRemessa(sequencialRemessaService.generate(this.remessa.getUnidadeSolicitante()));
        } catch (BusinessException e) {
          e.printStackTrace();
        }
        remessaDocumentoService.salvar(remessaDocumento);
      } else {
        try {
          remessaDocumento.setCodigoRemessa(sequencialRemessaService.generate(this.remessa.getUnidadeSolicitante()));
        } catch (BusinessException e) {
          e.printStackTrace();
        }
      }
      remessaDocumentoVOList.add(remessaDocumento);
    }

    return remessaDocumentoVOList;
  }

  /**
   * Realizar validação de pré-crítica da planilha (header, nome do documento e versão)
   * @throws BusinessException
   */
  private void validacaoPreCriticaPlanilha() throws BusinessException {

    if (ObjectUtils.isNullOrEmpty(this.documento)
        || !TipoSolicitacaoEnum.REMESSA.equals(this.documento.getGrupo().getTipoSolicitacao())) {
      throw new BusinessException(MensagemUtils.obterMensagem("ME026", this.codigoDocumento));
    }

    if (!this.documento.getGrupo().getVersaoFormulario().toString().equals(this.versaoDocumento)) {
      throw new BusinessException(
          MensagemUtils.obterMensagem("ME027", this.versaoDocumento, this.documento.getGrupo().getVersaoFormulario().toString()));
    }

    for (int i = 0; i < this.camposPlanilha.size(); i++) {
      String campo = this.camposPlanilha.get(i);
      if (!Constantes.UNIDADE_GERADORA.equals(campo)) {
        if (!this.camposFormulario.containsKey(campo)) {
          throw new BusinessException(MensagemUtils.obterMensagem("ME028", this.camposPlanilha.get(i), this.documento.getNome()));
        }
      }
    }

    if (!this.camposPlanilha.contains(Constantes.UNIDADE_GERADORA)) {
      throw new BusinessException(MensagemUtils.obterMensagem("MI033"));
    }

    for (String coluna : this.camposFormulario.keySet()) {
      if (!this.camposPlanilha.contains(coluna)) {
        throw new BusinessException(MensagemUtils.obterMensagem("MI033"));
      }
    }
  }

  /**
   * Obter requisições que constam na planilha
   * @return Lista de RequisicaoLoteDTO
   * @throws IOException
   * @throws BusinessException
   * @throws AppException
   */
  private List<RemessaLoteDTO> obterItensRemessaLote() throws IOException, BusinessException, AppException {

    CSVFormat rfc4180 =
        CSVFormat.EXCEL.withHeader((String[]) this.camposPlanilha.toArray()).withDelimiter(Constantes.SEPARADOR_CSV);

    InputStreamReader reader =
        new InputStreamReader(new BOMInputStream(this.file.getInputstream()), Constantes.ENCODING_ISO88591);
    CSVParser iterableParser = rfc4180.parse(reader);

    List<String> mensagensValidacao = new ArrayList<String>();
    List<RemessaLoteDTO> itensRemessa = new ArrayList<RemessaLoteDTO>();

    this.numeroRejeitados = 0;
    this.registrosDuplicados = 0;
    for (CSVRecord registro : iterableParser) {
      if (registro.getRecordNumber() > LINHA_HEADER) {

        if (this.isEmptyRecord(registro))
          continue;

        try {
          RemessaLoteDTO remessaLote = this.analisarRegistroRemessa(registro);
          itensRemessa.add(remessaLote);

        } catch (BusinessException e) {
          mensagensValidacao.addAll(e.getErroList());
          this.numeroRejeitados++;
        }
      }
    }

    if (!ObjectUtils.isNullOrEmpty(mensagensValidacao)) {
      throw new BusinessException(mensagensValidacao);
    }

    return itensRemessa;
  }

  public Boolean isEmptyRecord(final CSVRecord record) {
    Iterator<String> itRecord = record.iterator();
    while (itRecord.hasNext()) {
      if (!ObjectUtils.isNullOrEmpty(itRecord.next())) {
        return false;
      }
    }

    return true;
  }

  /**
   * Realiza a análise e validação das requisições
   * @param registro
   * @return
   * @throws AppException
   */
  private RemessaLoteDTO analisarRegistroRemessa(CSVRecord registro) throws BusinessException, AppException {

    List<String> msgValidacao = new ArrayList<String>();
    Set<GrupoCampoVO> grupoCampos = new HashSet<GrupoCampoVO>();
    RemessaLoteDTO remessaLote = new RemessaLoteDTO();
    RemessaDocumentoVO remessaDocumentoVO = new RemessaDocumentoVO();

    if (!registro.isConsistent()) {
      msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("ME020")));
      throw new BusinessException(msgValidacao);
    }

    Set<GrupoCampoVO> ultVersaoGrupoCampos = this.documentoService.getUltimaVersaoGrupoCampoDocumento(this.documento);

    for (int c = 0; c < this.camposPlanilha.size(); c++) {

      // -- Valor informado na planilha
      String valorCampo = registro.get(c);

      // -- Nome do campo que consta na planilha
      String campo = this.camposPlanilha.get(c);

      // -- begin-of: Campos fixos
      if (Constantes.UNIDADE_GERADORA.equals(campo)) {

        if (Constantes.UNIDADE_GERADORA.equals(campo)) {
          try {
            UnidadeVO unidadeGeradora = this.unidadeService.findById(Long.valueOf(valorCampo.trim()));
            if (ObjectUtils.isNullOrEmpty(unidadeGeradora)) {
              msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA020", campo)));
            } else {
              remessaDocumentoVO.setUnidadeGeradora(unidadeGeradora);
            }
          } catch (NumberFormatException e) {
            msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", campo)));
          }
        }
        continue;
      }

      // -- begin-of: Campos dinâmicos
      Date valorData = null;

      // -- GrupoCampo correspondente ao nome do campo
      GrupoCampoVO grupoCampo = this.camposFormulario.get(campo);

      // -- Obrigatoriedade de Campos
      if (SimNaoEnum.SIM.equals(grupoCampo.getCampoObrigatorio()) && ObjectUtils.isNullOrEmpty(valorCampo)) {
        msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA001", campo)));
      }

      // -- Campo Data
      if (TipoCampoEnum.DATA.equals(grupoCampo.getCampo().getTipo())) {
        valorData = DateUtils.tryParse(valorCampo, null, "dd/MM/yy");
        if (!ObjectUtils.isNullOrEmpty(valorCampo) && ObjectUtils.isNullOrEmpty(valorData)) {
          msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", campo)));
        }
      }
      
      // -- Campo cpf e cnpj replace all em todos os "-", "." e "/"
      if(grupoCampo != null && grupoCampo.getCampo() != null
          && Constantes.NOME_CAMPO_CPF.equals(grupoCampo.getCampo().getNome()) || 
          Constantes.NOME_CAMPO_CNPJ.equals(grupoCampo.getCampo().getNome())) {
        String temp = StringUtils.replace(valorCampo, ".", "").replace("-", "").replace("/", "");
        valorCampo = temp;
      }

      if (grupoCampo != null && grupoCampo.getCampo() != null
          && Constantes.NOME_CAMPO_OPERACAO.equals(grupoCampo.getCampo().getNome())) {
        try {
          Integer temp = Integer.parseInt(StringUtils.replace(valorCampo, " ", ""));
          valorCampo = String.format("%03d", temp);
        } catch (NumberFormatException nfe) {
          this.logger.error(nfe.getMessage(), nfe);
        }
      }

      if (!ObjectUtils.isNullOrEmpty(valorData)) {
        grupoCampo.setValorData(valorData);
      } else if (grupoCampo.getCampo().getNome().equals(Constantes.NOME_CAMPO_OPERACAO) && !ObjectUtils.isNullOrEmpty(valorCampo)
          && NumberUtils.isNumber(valorCampo)) {
        try {
          OperacaoVO operacao = this.operacaoService.findById(valorCampo);
          remessaDocumentoVO.setNuOperacao(operacao.getId().toString());
        } catch (Exception e) {
          msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, e.getMessage()));
        }
      } else if (!ObjectUtils.isNullOrEmpty(valorCampo) && !ObjectUtils.isNullOrEmpty(grupoCampo.getCampo().getTamanho())
          && valorCampo.length() > grupoCampo.getCampo().getTamanho()) {
        msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro,
            MensagemUtils.obterMensagem("MA055", campo, grupoCampo.getCampo().getTamanho())));
      } else if (grupoCampo.getCampo().getTipo().equals(TipoCampoEnum.NUMERICO) && !ObjectUtils.isNullOrEmpty(valorCampo)
          && !valorCampo.matches(REGEX_CAMPO_NUMERICO)) {
        msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro,
            MensagemUtils.obterMensagem("MI014", grupoCampo.getCampo().getDescricao())));
      } else {
        grupoCampo.setValor(valorCampo);
      }

      grupoCampos.add(grupoCampo);
    }

    remessaDocumentoVO = GrupoCamposHelper.setValorCamposDinamicos(remessaDocumentoVO, grupoCampos);

    try {
      this.validarCamposObrigatorios(remessaDocumentoVO, ultVersaoGrupoCampos);
    } catch (RequiredException e) {
      final String msgBundle = MensagemUtils.obterMensagem("MA001");

      for (String msg : e.getErroList()) {

        String campo = StringUtil.difference(msgBundle, msg);
        String msgCampoInvalido = MensagemUtils.obterMensagem("MI014", campo);

        msg = this.getMensagemLogAberturaRequisicao(registro, msg);
        msgCampoInvalido = this.getMensagemLogAberturaRequisicao(registro, msgCampoInvalido);

        if (!msgValidacao.contains(msg) && !msgValidacao.contains(msgCampoInvalido)) {
          msgValidacao.add(msg);
        }
      }
    } catch (AppException e) {
      throw e;
    }

    remessaLote.setRemessaDocumento(remessaDocumentoVO);
    try {
      remessaDocumentoService.validarRemessaDocumento(remessaDocumentoVO);
    }catch (BusinessException e) {
      msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, e.getMessage()));
    }
    
    if (!ObjectUtils.isNullOrEmpty(msgValidacao)) {
      throw new BusinessException(msgValidacao);
    }

    return remessaLote;
  }

  private void validarCamposObrigatorios(final RemessaDocumentoVO remessaDocumentoVO,
      final Set<GrupoCampoVO> ultVersaoGrupoCampos) throws RequiredException, AppException {
    List<String> msgValidacao = new ArrayList<String>();

    if (ObjectUtils.isNullOrEmpty(remessaDocumentoVO.getUnidadeGeradora())) {
      msgValidacao.add(MensagemUtils.obterMensagem("MA001", Constantes.UNIDADE_GERADORA));
    }

    try {
      GrupoCamposHelper.validarCamposDinamicosObrigatorios(remessaDocumentoVO, ultVersaoGrupoCampos);
    } catch (RequiredException e) {
      for (String msg : e.getErroList()) {
        msgValidacao.add(msg);
      }
    } catch (AppException e) {
      throw e;
    }

    if (!msgValidacao.isEmpty()) {
      throw new RequiredException(msgValidacao);
    }
  }

  /**
   * Inicializa todos os elementos tags: #requisicao #form
   * @throws DataBaseException
   */
  private RemessaVO inicializarRemessa() throws DataBaseException {

    // Inicializa a requisição
    this.remessa = new RemessaVO();

    // Inicializa o usuário e a unidade
    UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
    UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();

    remessa.setCodigoUsuarioAbertura(usuario.getNuMatricula());
    remessa.setUnidadeSolicitante(unidadeLotacao);
    remessa.setDataHoraAbertura(new Date());
    remessa.setDocumento(this.documento);

    return remessa;
  }

  /**
   * Raliza a formatação das mensagens de validação que irão compor o relatório de abertura em lote
   * @param registro Linha do CSV
   * @param msg Mensagem de validação
   * @return Mensagem de validação formatada
   */
  private String getMensagemLogAberturaRequisicao(CSVRecord registro, String msg) {
    return String.format("Linha %s - %s", registro.getRecordNumber(), msg);
  }

  private void preProcessarArquivo() throws IOException, BusinessException {
    BufferedReader in = null;
    try {

      int count = 0;

      in = new BufferedReader(new InputStreamReader(this.file.getInputstream(), Constantes.ENCODING_WINDOWS_1252));
      this.filename = this.file.getFileName();

      // Realiza a validação do header: Id do documento e a versão
      String linhaHeader = in.readLine().replaceAll(REGEX_LINHA_HEADER, StringUtils.EMPTY);
      String[] elementos = linhaHeader.split(REGEX_SEPARADOR_HEADER);

      if (ObjectUtils.isNullOrEmpty(elementos) || elementos.length != 3 || !StringUtils.isNumeric(elementos[0])) {
        throw new BusinessException(MensagemUtils.obterMensagem("ME029"));
      }

      this.documento = this.documentoService.findByIdEager(Long.valueOf(elementos[0]));

      this.camposFormulario.clear();

      // Obtém os campos dinâmicos do formulário associado ao documento
      if (!ObjectUtils.isNullOrEmpty(this.documento)) {
        for (GrupoCampoVO grupoCampo : this.documento.getGrupo().getGrupoCampos()) {
          CampoVO campo = grupoCampo.getCampo();
          String nomeCampo = (String) ObjectUtils.defaultIfNull(grupoCampo.getLegenda(), campo.getDescricao());
          this.camposFormulario.put(nomeCampo.toUpperCase(), grupoCampo);
        }
      }

      this.codigoDocumento = elementos[0].trim();
      this.versaoDocumento = elementos[1].trim();
      this.camposPlanilha =
          Arrays.asList(in.readLine().replaceAll(REGEX_MARCADOR_OBRIGATORIO, StringUtils.EMPTY)
              .split(String.valueOf(Constantes.SEPARADOR_CSV)));

      String line = StringUtils.EMPTY;

      while ((line = in.readLine()) != null) {
        if (!this.linhaVazia(line)) {
          count++;
        }
      }

      if (count == 0) {
        throw new BusinessException(MensagemUtils.obterMensagem("MI033"));
      }

      this.numeroLinhas = count;

    } finally {
      if (!ObjectUtils.isNullOrEmpty(in)) {
        in.close();
      }
    }
  }

  private boolean linhaVazia(String line) {
    if (ObjectUtils.isNullOrEmpty(line)) {
      return true;
    }
    String linha = line.replaceAll(";", "").replaceAll(" ", "");
    return ObjectUtils.isNullOrEmpty(linha);
  }

  public Boolean canDownloadLink() {
    return (!ObjectUtils.isNullOrEmpty(this.msgValidacaoLog) && this.msgValidacaoLog.size() > 0);
  }

  public StreamedContent downloadLog() {

    this.logFile = new File(NOME_ARQUIVO_LOG);
    StringBuilder line = new StringBuilder();

    try {

      line.append(String.format("Arquivo \"%s\" enviado com %s itens.", this.filename, this.numeroLinhas));
      line.append(FileUtils.SYSTEM_EOL);
      line.append(String.format("%s itens rejeitados.", this.numeroRejeitados));
      line.append(FileUtils.SYSTEM_EOL);
      line.append(FileUtils.SYSTEM_EOL);

      for (String conteudo : this.msgValidacaoLog) {
        line.append(conteudo);
        line.append(FileUtils.SYSTEM_EOL);
      }

      line.append(FileUtils.SYSTEM_EOL);
      line.append(String.format("Final do log de erros gerado pela importação do arquivo \"%s\".", this.filename));

      PrintWriter writer = new PrintWriter(this.logFile);
      writer.print(line.toString());

      writer.flush();
      writer.close();

      return RequestUtils.download(this.logFile, this.logFile.getName());

    } catch (FileNotFoundException e) {
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
    } catch (BusinessException e) {
      this.logger.error(e.getMessage(), e);
      this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA012"));
    }

    return null;
  }

  public UploadedFile getFile() {
    return file;
  }

  public void setFile(UploadedFile file) {
    this.file = file;
  }

  public Integer getNumeroLinhas() {
    return numeroLinhas;
  }

  public void setNumeroLinhas(Integer numeroLinhas) {
    this.numeroLinhas = numeroLinhas;
  }

  public Boolean getModoEdicaoRemessaAB() {
    return modoEdicaoRemessaAB;
  }

  public void setModoEdicaoRemessaAB(Boolean modoEdicaoRemessaAB) {
    this.modoEdicaoRemessaAB = modoEdicaoRemessaAB;
  }

  /**
   * @return the remessa
   */
  public RemessaVO getRemessa() {
    return remessa;
  }

  /**
   * @param remessa the remessa to set
   */
  public void setRemessa(RemessaVO remessa) {
    this.remessa = remessa;
  }

}
