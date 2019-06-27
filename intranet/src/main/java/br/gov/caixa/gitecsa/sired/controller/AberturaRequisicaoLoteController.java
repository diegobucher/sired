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
import br.gov.caixa.gitecsa.sired.dto.RequisicaoLoteDTO;
import br.gov.caixa.gitecsa.sired.enumerator.FormatoDocumentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoDemandaEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.helper.GrupoCamposHelper;
import br.gov.caixa.gitecsa.sired.service.ArquivoLoteService;
import br.gov.caixa.gitecsa.sired.service.DocumentoService;
import br.gov.caixa.gitecsa.sired.service.OperacaoService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoLoteService;
import br.gov.caixa.gitecsa.sired.service.RequisicaoService;
import br.gov.caixa.gitecsa.sired.service.TipoDemandaService;
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
import br.gov.caixa.gitecsa.sired.vo.RequisicaoDocumentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TipoDemandaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

@Named
@ViewScoped
public class AberturaRequisicaoLoteController implements Serializable {

	private static final String REGEX_CAMPO_NUMERICO = "^[0-9,\\.]+$";

	private static final String REGEX_MARCADOR_OBRIGATORIO = "\\*";

	private static final String REGEX_SEPARADOR_HEADER = "\\|";

	private static final String REGEX_LINHA_HEADER = "(;+)$";

	private static final String SEPARADOR_NUM_REQUISICOES_ABERTAS = ", ";

    private static final String NOME_ARQUIVO_LOG = "aberturaRequisicaoLoteRejeitado.txt";

    private static final long serialVersionUID = 1L;
    
    private static final long LINHA_HEADER = 2L;
    
    @Inject
    protected FacesMensager facesMessager;

    @Inject
    private transient Logger logger;

    @Inject
    private UnidadeService unidadeService;
    
    @Inject
    private DocumentoService documentoService;
    
    @Inject
    private RequisicaoService requisicaoService;
    
    @Inject
    private RequisicaoLoteService requisicaoLoteService;
    
    @Inject
    private TipoDemandaService tipoDemandaService;
    
    @Inject
    private ArquivoLoteService arquivoLoteService;
    
    @Inject
    private OperacaoService operacaoService;
    
    private File logFile;
    
    private UploadedFile file;
    
    private String filename;
    
    private String codigoDocumento;
    
    private String versaoDocumento;
    
    private Integer numeroLinhas = 0;
    
    private Integer numeroRejeitados = 0;
    
    private DocumentoVO documento;
    
    private List<String> camposPlanilha;
    
    private Map<String, GrupoCampoVO> camposFormulario = new HashMap<String, GrupoCampoVO>();

    private ArrayList<String> msgValidacaoLog;
    
	// total de registros duplicados no arquivo importado
	private Integer registrosDuplicados = 0;
    
    public void validarArquivo() {
        Boolean invalido = false;
        try {
            MensagemUtils.setKeepMessages(true);
            
            if(ObjectUtils.isNullOrEmpty(this.file)) {
                invalido = true;
                this.facesMessager.addMessageError(MensagemUtils.obterMensagem("MA045"));
            } else if (!this.file.getFileName().toLowerCase().endsWith(".txt") && !this.file.getFileName().toLowerCase().endsWith(".csv")) {
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
    
    public void processarArquivo() throws BusinessException {
        
        ArquivoLoteVO arquivoLote = null;
        
        try {
            this.msgValidacaoLog = null;
            arquivoLote = this.arquivoLoteService.salvar(this.file.getFileName(), (UsuarioLdap)RequestUtils.getSessionValue("usuario"));
            
            this.validacaoPreCriticaPlanilha();
            List<RequisicaoLoteDTO> requisicoes = this.obterRequisicoesPlanilha();
            
            this.requisicaoLoteService.salvar(requisicoes);
            
            String[] codigos = new String[requisicoes.size()];
            for (int i = 0; i < requisicoes.size(); i++) {
                RequisicaoVO requisicao = requisicoes.get(i).getRequisicao();
                codigos[i] = requisicao.getCodigoRequisicao().toString();
            }
            
            this.arquivoLoteService.concluir(arquivoLote, false);

            String mensagem = String.format("Foram abertas %s requisições: %s.", requisicoes.size(), StringUtils.join(codigos, SEPARADOR_NUM_REQUISICOES_ABERTAS));
            if (registrosDuplicados > 0) {
            	mensagem += System.getProperty("line.separator");
            	mensagem += "Houveram linhas duplicadas no arquivo, e elas foram ignoradas.";
			}
            this.facesMessager.addMessageInfo(mensagem);
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
    }
    
    /**
     * Realizar validação de pré-crítica da planilha (header, nome do documento e versão)
     * 
     * @throws BusinessException
     */
    private void validacaoPreCriticaPlanilha() throws BusinessException {
        
        if (ObjectUtils.isNullOrEmpty(this.documento) || TipoSolicitacaoEnum.REMESSA.equals(this.documento.getGrupo().getTipoSolicitacao())) {
            throw new BusinessException(MensagemUtils.obterMensagem("ME026", this.codigoDocumento));
        } 
        
        if (!this.documento.getGrupo().getVersaoFormulario().toString().equals(this.versaoDocumento)) {
            throw new BusinessException(MensagemUtils.obterMensagem("ME027", this.versaoDocumento, this.documento.getGrupo().getVersaoFormulario().toString()));
        }
        
        for (int i = 0; i < this.camposPlanilha.size(); i++) {
            String campo = this.camposPlanilha.get(i);
            if (!Constantes.UNIDADE_GERADORA.equals(campo) && !Constantes.FORMATO.equals(campo) && !Constantes.DEMANDA.equals(campo) 
                    && !Constantes.NUMERO_PROCESSO.equals(campo) && !Constantes.OBSERVACAO.equals(campo)) {
                if(!this.camposFormulario.containsKey(campo)) {
                    throw new BusinessException(MensagemUtils.obterMensagem("ME028", this.camposPlanilha.get(i), this.documento.getNome()));
                } 
            } 
        }
        
        if (!this.camposPlanilha.contains(Constantes.UNIDADE_GERADORA) || !this.camposPlanilha.contains(Constantes.FORMATO) 
				|| !this.camposPlanilha.contains(Constantes.DEMANDA) || !this.camposPlanilha.contains(Constantes.NUMERO_PROCESSO) 
				|| !this.camposPlanilha.contains(Constantes.OBSERVACAO)) {
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
     * 
     * @return Lista de RequisicaoLoteDTO
     * @throws IOException
     * @throws BusinessException
     * @throws AppException
     */
    private List<RequisicaoLoteDTO> obterRequisicoesPlanilha() throws IOException, BusinessException, AppException {
        
        CSVFormat rfc4180 = CSVFormat.EXCEL
                .withHeader((String[])this.camposPlanilha.toArray())
                .withDelimiter(Constantes.SEPARADOR_CSV);
        
        InputStreamReader reader = new InputStreamReader(new BOMInputStream(this.file.getInputstream()), Constantes.ENCODING_ISO88591);
        CSVParser iterableParser = rfc4180.parse(reader);
        
        List<String> mensagensValidacao = new ArrayList<String>();
        List<RequisicaoLoteDTO> requisicoesLote = new ArrayList<RequisicaoLoteDTO>();
        
        this.numeroRejeitados = 0;
        this.registrosDuplicados = 0;
        for (CSVRecord registro : iterableParser) {
            if (registro.getRecordNumber() > LINHA_HEADER) {
                
                if (this.isEmptyRecord(registro))
                    continue;
                
                try {
                    RequisicaoLoteDTO requisicaoLote = this.analisarRegistroRequisicao(registro);
                    if (requisicoesLote.contains(requisicaoLote)) {
                    	registrosDuplicados++;
					}else{
						requisicoesLote.add(requisicaoLote);
					}
                    
                } catch (BusinessException e) {
                    mensagensValidacao.addAll(e.getErroList());
                    this.numeroRejeitados++;
                }
            }
        }
        
        if (!ObjectUtils.isNullOrEmpty(mensagensValidacao)) {
            throw new BusinessException(mensagensValidacao);
        }
        
        return requisicoesLote;
    }
    
    public Boolean isEmptyRecord(final CSVRecord record) {
        Iterator<String> itRecord = record.iterator();
        while(itRecord.hasNext()) {
            if(!ObjectUtils.isNullOrEmpty(itRecord.next())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Realiza a análise e validação das requisições
     * 
     * @param registro
     * @return
     * @throws AppException
     */
    private RequisicaoLoteDTO analisarRegistroRequisicao(CSVRecord registro) throws BusinessException, AppException {
        
        List<String> msgValidacao = new ArrayList<String>();
        Set<GrupoCampoVO> grupoCampos = new HashSet<GrupoCampoVO>();
        
        if (!registro.isConsistent()) {
            msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("ME020")));
            throw new BusinessException(msgValidacao);
        }
        
        RequisicaoVO requisicao = this.inicializarRequisicao();
        Set<GrupoCampoVO> ultVersaoGrupoCampos = this.documentoService.getUltimaVersaoGrupoCampoDocumento(this.documento);
        
        for (int c = 0; c < this.camposPlanilha.size(); c++) {
            
            //-- Valor informado na planilha
            String valorCampo = registro.get(c);
            
            //-- Nome do campo que consta na planilha
            String campo = this.camposPlanilha.get(c);
            
            //-- begin-of: Campos fixos
            if (Constantes.UNIDADE_GERADORA.equals(campo) || Constantes.FORMATO.equals(campo) 
                    || Constantes.DEMANDA.equals(campo) || Constantes.NUMERO_PROCESSO.equals(campo) || Constantes.OBSERVACAO.equals(campo)) {
                
                // Verifica os campos fixos que são obrigatórios e são independentes
                if (!Constantes.NUMERO_PROCESSO.equals(campo) && !Constantes.OBSERVACAO.equals(campo) && ObjectUtils.isNullOrEmpty(valorCampo)) {
                    msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA001", campo)));
                    continue;
                }
                
                if (Constantes.UNIDADE_GERADORA.equals(campo)) {
                    try {
                        UnidadeVO unidadeGeradora = this.unidadeService.findById(Long.valueOf(valorCampo.trim()));
                        if (ObjectUtils.isNullOrEmpty(unidadeGeradora)) {
                            msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA020", campo)));
                        } else {
                            requisicao.getRequisicaoDocumento().setUnidadeGeradora(unidadeGeradora);
                        }
                    } catch (NumberFormatException e) {
                        msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", campo)));
                    }
                } else if (Constantes.FORMATO.equals(campo)) {
                    try {
                        requisicao.setFormato(FormatoDocumentoEnum.fromString(valorCampo.trim()));
                    } catch (IllegalArgumentException e) {
                        msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", campo)));
                    }
                } else if (Constantes.DEMANDA.equals(campo)) {
                    try {
                        String numeroProcesso = requisicao.getRequisicaoDocumento().getNuDocumentoExigido();
                        TipoDemandaVO tipoDemanda = this.tipoDemandaService.findByNomeESetorial(
                                TipoDemandaEnum.fromString(valorCampo.trim()).getDescricao().toUpperCase(), 
                                this.documento.getIcSetorial()
                        );
                        // Demandas do tipo "Normal" não possuem número de processo
                        if (TipoDemandaEnum.NORMAL.getId().equalsIgnoreCase(tipoDemanda.getNome()) && !ObjectUtils.isNullOrEmpty(numeroProcesso)) {
                            msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", Constantes.NUMERO_PROCESSO)));
                        } else {
                            requisicao.getRequisicaoDocumento().setTipoDemanda(tipoDemanda);
                        }
                    } catch (IllegalArgumentException e) {
                        msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", campo)));
                    }
                } else if (Constantes.NUMERO_PROCESSO.equals(campo)) {
                    requisicao.getRequisicaoDocumento().setNuDocumentoExigido(valorCampo.trim());
                    if (!ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNuDocumentoExigido())
                    		&& requisicao.getRequisicaoDocumento().getNuDocumentoExigido().length() > Constantes.MAX_CHARS_NUMERO_PROCESSO) {
                    	msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA055", campo, Constantes.MAX_CHARS_NUMERO_PROCESSO)));
                    } else if (ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getNuDocumentoExigido()) 
                            && !ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getTipoDemanda())
                            && !TipoDemandaEnum.NORMAL.getId().equals(requisicao.getRequisicaoDocumento().getTipoDemanda().getNome())) {
                        msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA001", campo)));
                    }
                } else if (Constantes.OBSERVACAO.equals(campo)) {
                    if (!ObjectUtils.isNullOrEmpty(valorCampo)) {
                        String observacao = StringUtils.abbreviate(valorCampo, Constantes.MAX_CHARS_OBSERVACAO);
                        requisicao.getRequisicaoDocumento().setObservacao(observacao);
                    }
                }
                continue;
            }
            
            //-- begin-of: Campos dinâmicos
            Date valorData = null;
            
            //-- GrupoCampo correspondente ao nome do campo
            GrupoCampoVO grupoCampo = this.camposFormulario.get(campo);
            
            //-- Obrigatoriedade de Campos
            if (SimNaoEnum.SIM.equals(grupoCampo.getCampoObrigatorio()) && ObjectUtils.isNullOrEmpty(valorCampo)) {
                msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA001", campo)));
            }

            //-- Campo Data
            if (TipoCampoEnum.DATA.equals(grupoCampo.getCampo().getTipo())) {
                valorData = DateUtils.tryParse(valorCampo, null, "dd/MM/yy");
                if (!ObjectUtils.isNullOrEmpty(valorCampo) && ObjectUtils.isNullOrEmpty(valorData)) {
                    msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", campo)));
                }
            }
            
            if(grupoCampo != null && grupoCampo.getCampo() != null && Constantes.NOME_CAMPO_OPERACAO.equals(grupoCampo.getCampo().getNome())) {
              try {    
                Integer temp = Integer.parseInt(StringUtils.replace(valorCampo, " ", ""));
                valorCampo = String.format("%03d", temp);
              } catch (NumberFormatException nfe) {
                this.logger.error(nfe.getMessage(), nfe);
              }              
            }
            
            if(!ObjectUtils.isNullOrEmpty(valorData)) {
                grupoCampo.setValorData(valorData);
            } else if (grupoCampo.getCampo().getNome().equals(Constantes.NOME_CAMPO_OPERACAO) && !ObjectUtils.isNullOrEmpty(valorCampo)
            		&& NumberUtils.isNumber(valorCampo)) {
            	try {
            		OperacaoVO operacao = this.operacaoService.findById(valorCampo);
            		requisicao.getRequisicaoDocumento().setOperacao(operacao);
            	} catch (Exception e) {
            		 msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, e.getMessage()));
            	}
            } else if (!ObjectUtils.isNullOrEmpty(valorCampo) && !ObjectUtils.isNullOrEmpty(grupoCampo.getCampo().getTamanho()) 
            		&& valorCampo.length() > grupoCampo.getCampo().getTamanho()) {
                msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MA055", campo, grupoCampo.getCampo().getTamanho())));
            } else if (grupoCampo.getCampo().getTipo().equals(TipoCampoEnum.NUMERICO) && !ObjectUtils.isNullOrEmpty(valorCampo) 
            		&& !valorCampo.matches(REGEX_CAMPO_NUMERICO)) {
            	msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, MensagemUtils.obterMensagem("MI014", grupoCampo.getCampo().getDescricao())));
            } else {            	
            	grupoCampo.setValor(valorCampo);
            }
            
            grupoCampos.add(grupoCampo);
        }
        
        requisicao = GrupoCamposHelper.setValorCamposDinamicos(requisicao, grupoCampos);
        
        try {
        	this.validarCamposObrigatorios(requisicao, ultVersaoGrupoCampos);
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
        
        try {
        	// System.err.println("************** validarRequisicao e isRequisicaoDuplicada: CodigoRequisicao = "+ requisicao.getCodigoRequisicao());
        	this.requisicaoService.validarRequisicao(requisicao);
        	this.requisicaoService.isRequisicaoDuplicada(requisicao);
            requisicao.setQtSolicitadaDocumento(this.requisicaoService.calcularQuantidadeDocumentosSolicitada(requisicao));
            
        } catch (BusinessException e) {
            msgValidacao.add(this.getMensagemLogAberturaRequisicao(registro, e.getMessage()));
        }
        
        if (!ObjectUtils.isNullOrEmpty(msgValidacao)) { 
            throw new BusinessException(msgValidacao);
        }
        
        RequisicaoLoteDTO requisicaoLote = new RequisicaoLoteDTO();
        requisicaoLote.setRequisicao(requisicao);
        requisicaoLote.setRegistro(registro);
        
        return requisicaoLote;
    }
    
    private void validarCamposObrigatorios(final RequisicaoVO requisicao, final Set<GrupoCampoVO> ultVersaoGrupoCampos) throws RequiredException, AppException {
    	List<String> msgValidacao = new ArrayList<String>();
    	
    	if (ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getUnidadeGeradora())) {
    		msgValidacao.add(MensagemUtils.obterMensagem("MA001", Constantes.UNIDADE_GERADORA));
    	}
    	
    	if (ObjectUtils.isNullOrEmpty(requisicao.getFormato())) {
    		msgValidacao.add(MensagemUtils.obterMensagem("MA001", Constantes.FORMATO));
    	}
    	
    	if (ObjectUtils.isNullOrEmpty(requisicao.getRequisicaoDocumento().getTipoDemanda())) {
    		msgValidacao.add(MensagemUtils.obterMensagem("MA001", Constantes.DEMANDA));
    	}
    	
    	try {
        	GrupoCamposHelper.validarCamposDinamicosObrigatorios(requisicao.getRequisicaoDocumento(), ultVersaoGrupoCampos);
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
    private RequisicaoVO inicializarRequisicao() throws DataBaseException {
        
        // Inicializa a requisição
        RequisicaoVO requisicao = new RequisicaoVO();

        // Inicializa o usuário e a unidade
        UsuarioLdap usuario = (UsuarioLdap) RequestUtils.getSessionValue("usuario");
        UnidadeVO unidadeLotacao = this.unidadeService.findUnidadeLotacaoUsuarioLogado();
        
        requisicao.setCodigoUsuarioAbertura(usuario.getNuMatricula());
        requisicao.setUnidadeSolicitante(unidadeLotacao);
        requisicao.setDataHoraAbertura(new Date());
        requisicao.setDocumento(this.documento);

        // Inicializa a requisição documento
        requisicao.setRequisicaoDocumento(new RequisicaoDocumentoVO());
        
        return requisicao;
    }
    
    /**
     * Raliza a formatação das mensagens de validação que irão compor o relatório de abertura em lote
     * 
     * @param registro
     *            Linha do CSV
     * @param msg
     *            Mensagem de validação
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
                    String nomeCampo = (String)ObjectUtils.defaultIfNull(grupoCampo.getLegenda(), campo.getDescricao());
                    this.camposFormulario.put(nomeCampo.toUpperCase(), grupoCampo);
                }
            }
            
            this.codigoDocumento = elementos[0].trim();
            this.versaoDocumento = elementos[1].trim();
            this.camposPlanilha = Arrays.asList(in.readLine().replaceAll(REGEX_MARCADOR_OBRIGATORIO, StringUtils.EMPTY).split(String.valueOf(Constantes.SEPARADOR_CSV)));
            
            String line = StringUtils.EMPTY;
            
            while((line = in.readLine()) != null) {
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
    
    private boolean linhaVazia(String line){
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

}
