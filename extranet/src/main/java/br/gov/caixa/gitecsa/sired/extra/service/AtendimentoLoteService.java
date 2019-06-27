package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoDocumentoOriginalEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.extra.dto.AtendimentoLoteDTO;
import br.gov.caixa.gitecsa.sired.extra.report.RelatorioAtendimentoLoteCSV;
import br.gov.caixa.gitecsa.sired.extra.report.RelatorioAtendimentoLoteDocDigital;
import br.gov.caixa.gitecsa.sired.extra.report.Reportable;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaContratoVO;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

@Stateless
public class AtendimentoLoteService extends AbstractService<ArquivoLoteVO> implements Serializable {

    private static final long serialVersionUID = -1011458609930501095L;

    private static final int CSV_HEADER_QTD_COLUMNS = 4;

    private static final String CSV_HEADER_OBS = "observacao";

    private static final String CSV_HEADER_QTD = "qtdDisponibilizadaDocumento";

    private static final String CSV_HEADER_SUPORTE = "suporte";

    private static final String CSV_HEADER_OCORRENCIA = "ocorrencia";

    private static final String CSV_HEADER_CODIGO = "codigoRequisicao";

    @Inject
    private RequisicaoService requisicaoService;

    @Inject
    private OcorrenciaAtendimentoService ocorrenciaService;

    @Inject
    private SuporteService suporteService;

    @Inject
    private TramiteRequisicaoService tramiteService;

    @Inject
    private ArquivoLoteService arquivoLoteService;

    @Inject
    private SituacaoRequisicaoService situacaoRequisicaoService;
    
    @Inject
    private DocumentoOriginalService docOriginalService;

    @Override
    protected void validaCamposObrigatorios(ArquivoLoteVO entity) throws BusinessException {
    }

    @Override
    protected void validaRegras(ArquivoLoteVO entity) throws BusinessException {
    }

    @Override
    protected void validaRegrasExcluir(ArquivoLoteVO entity) throws BusinessException {
    }

    @Override
    protected GenericDAO<ArquivoLoteVO> getDAO() {
        return null;
    }

    /**
     * Realiza o atendimento em lote online
     * 
     * @param arquivo
     *            Arquivo CSV contendo a lista de requisições que serão atendidas
     * @param usuario
     *            Usuário logado no sistema
     * @return Relatório resultante do atendimento
     * @throws BusinessException
     * @throws AppException
     */
    public Reportable atenderOnline(File arquivo, UsuarioLdap usuario) throws BusinessException, AppException {
        ArquivoLoteVO arquivoLote = null;
        try {
            arquivoLote = this.arquivoLoteService.salvar(arquivo.getName(), usuario);
            Reportable relatorio = this.atenderLote(arquivo, arquivoLote, usuario);
            relatorio.save(arquivoLote.getRelatorioLote(), System.getProperty(Constantes.EXTRANET_DIRETORIO_RELATORIOS));
            this.arquivoLoteService.concluir(arquivoLote, false);

            return relatorio;
        } catch (BusinessException e) {
            this.arquivoLoteService.concluir(arquivoLote, true);
            throw e;
        } catch (AppException e) {
            this.arquivoLoteService.concluir(arquivoLote, true);
            throw e;
        }
    }

    /**
     * Realiza o atendimento em lote por meio de um TimeTask (processo batch)
     * 
     * @param arquivo
     *            Arquivo CSV contendo a lista de requisições que serão atendidas
     * @param arquivoLote
     *            Arquivo de Lote
     * @param usuario
     *            Usuário logado no sistema
     * @return Relatório resultante do atendimento
     * @throws BusinessException
     * @throws AppException
     */
    public Reportable atenderBatch(File arquivo, ArquivoLoteVO arquivoLote, UsuarioLdap usuario) throws BusinessException, AppException {
        return this.atenderLote(arquivo, arquivoLote, usuario);
    }

    /**
     * Realiza o atendimento de cada requisição contida no arquivo CSV informado
     * 
     * @param arquivo
     *            Arquivo CSV contendo a lista de requisições que serão atendidas
     * @param arquivoLote
     *            Arquivo de Lote
     * @param usuario
     *            Usuário logado no sistema
     * @return Relatório resultante do atendimento
     * @throws AppException
     */
    private RelatorioAtendimentoLoteCSV atenderLote(File arquivo, ArquivoLoteVO arquivoLote, UsuarioLdap usuario) throws AppException {
        try {
            // -- Inicializa o parser do CSV
            // CSVFormat rfc4180 = CSVFormat.DEFAULT.withHeader(CSV_HEADER_CODICO, CSV_HEADER_OCORRENCIA, CSV_HEADER_SUPORTE, CSV_HEADER_QTD, CSV_HEADER_OBS);
            CSVFormat rfc4180 = CSVFormat.DEFAULT.withDelimiter(Constantes.SEPARADOR_CSV).withHeader(CSV_HEADER_CODIGO, CSV_HEADER_OCORRENCIA,
                    CSV_HEADER_SUPORTE, CSV_HEADER_QTD, CSV_HEADER_OBS);

            InputStreamReader reader = new InputStreamReader(new BOMInputStream(new FileInputStream(arquivo)));
            CSVParser iterableParser = rfc4180.parse(reader);

            RelatorioAtendimentoLoteCSV relatorio = new RelatorioAtendimentoLoteCSV(arquivo.getName());

            for (CSVRecord registro : iterableParser) {

                AtendimentoLoteDTO atendimento = this.analisarRegistroAtendimento(registro, usuario);

                // -- Requisição já processada
                if (!ObjectUtils.isNullOrEmpty(atendimento.getRequisicao()) && !ObjectUtils.isNullOrEmpty(atendimento.getRequisicao().getCodigoRequisicao())
                        && relatorio.hasCodigo(arquivo.getName(), atendimento.getRequisicao().getCodigoRequisicao())) {
                    atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME007")));
                }

                // -- Requisição inválida
                if (!ObjectUtils.isNullOrEmpty(atendimento.getMensagensValidacao())) {
                    relatorio.adicionar(arquivo.getName(), registro.get(CSV_HEADER_CODIGO), false);
                    for (String msg : atendimento.getMensagensValidacao()) {
                        relatorio.addDetail(msg);
                    }
                    continue;
                }

                try {
                    this.salvar(arquivo, arquivoLote, atendimento);
                    relatorio.adicionar(arquivo.getName(), registro.get(CSV_HEADER_CODIGO), true);
                } catch (BusinessException e) {
                    relatorio.adicionar(arquivo.getName(), registro.get(CSV_HEADER_CODIGO), false);
                    LogUtils.getMensagemPadraoLog(e.getMessage(), this.getClass().getSimpleName(), StringUtils.EMPTY, usuario.getEmail());
                }

            }

            relatorio.buildSumario();
            relatorio.createHeader();
            relatorio.createFooter();

            reader.close();

            moverArquivoParaPastaAtendimento(arquivo);

            return relatorio;
        } catch (Exception e) {
            LogUtils.getMensagemPadraoLog(e.getMessage(), this.getClass().getSimpleName(), StringUtils.EMPTY, usuario.getEmail());
            throw new AppException(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO), e);
        }
    }

    /**
     * Adiciona o timestamp no início do nome do arquivo e move para a pasta de atendimentos.
     * 
     * @param arquivo
     * @throws Exception
     */
    private void moverArquivoParaPastaAtendimento(File arquivo) throws Exception {
        String arquivoComTimestamp = FileUtils.appendDateTimeToFileName(arquivo.getCanonicalPath(), new Date(),
                RelatorioAtendimentoLoteDocDigital.SUFIXO_NOME_RELATORIO);
        arquivo = FileUtils.renameFile(arquivo, arquivoComTimestamp);

        // -- Cria a estrutura de diretórios e move o arquivo CSV para o diretório de documentos no caso de ser um processamento em lote
        File diretorio = FileUtils.createDirIfNotExists(System.getProperty(Constantes.EXTRANET_DIRETORIO_ATENDIMENTOS));
        if (!diretorio.getCanonicalPath().equalsIgnoreCase(arquivo.getParent())) {
            org.apache.commons.io.FileUtils.moveFileToDirectory(arquivo, diretorio, true);
        }
    }

    /**
     * Salva o atendimento
     * 
     * @param arquivo
     *            Arquivo CSV contendo a lista de requisições que serão atendidas
     * @param arquivoLote
     *            Arquivo de Lote
     * @param usuario
     *            Usuário logado no sistema
     * @param atendimento
     *            Dados do Atendimento
     * @throws BusinessException
     */
    private void salvar(File arquivo, ArquivoLoteVO arquivoLote, AtendimentoLoteDTO atendimento) throws BusinessException {

        try {

            TramiteRequisicaoVO tramite = new TramiteRequisicaoVO();

            tramite.setCodigoUsuario(arquivoLote.getCodigoUsuario());
            tramite.setDataHora(new Date());
            tramite.setOcorrencia(atendimento.getOcorrencia());
            tramite.setQtdDisponibilizadaDocumento(atendimento.getQtdDisponibilizada());
            tramite.setSuporte(atendimento.getSuporte());
            tramite.setArquivoLote(arquivoLote);
            tramite.setRequisicao(atendimento.getRequisicao());
            tramite.setObservacao(atendimento.getObservacao());

            SituacaoRequisicaoVO situacao = null;
            SituacaoRequisicaoVO situacaoAtual = atendimento.getRequisicao().getTramiteRequisicaoAtual().getSituacaoRequisicao();
            if (SituacaoRequisicaoEnum.ABERTA.getId().equals(((Long) situacaoAtual.getId()))) {
                if (OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor().equals(atendimento.getOcorrencia().getId())) {
                    // ABERTA => PEND UPLOAD
                    situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId());
                } else {
                    // ABERTA => ATENDIDA
                    situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.ATENDIDA.getId());
                }
            } else if (SituacaoRequisicaoEnum.PEND_DADOS_FAT.getId().equals(((Long) situacaoAtual.getId()))) {
                // PEND UPLOAD => ATENDIDA
                situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.ATENDIDA.getId());
                tramite.setArquivoDisponibilizado(atendimento.getRequisicao().getTramiteRequisicaoAtual().getArquivoDisponibilizado());
            } else if (SituacaoRequisicaoEnum.REABERTA.getId().equals(((Long) situacaoAtual.getId()))) {
                if (OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor().equals(atendimento.getOcorrencia().getId())) {
                    // REABERTA => PEND UPLOAD (REABERTA)
                    situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId());
                } else {
                    // ABERTA => REATENDIDA
                    situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.REATENDIDA.getId());
                }
            } else if (SituacaoRequisicaoEnum.REABERTA_PEND_DADOS_FAT.getId().equals(((Long) situacaoAtual.getId()))) {
                // PEND UPLOAD (REABERTA) => REATENDIDA
                situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.REATENDIDA.getId());
                tramite.setArquivoDisponibilizado(atendimento.getRequisicao().getTramiteRequisicaoAtual().getArquivoDisponibilizado());
            }

            if (SituacaoRequisicaoEnum.ATENDIDA.getId().equals(situacao.getId()) || SituacaoRequisicaoEnum.REATENDIDA.getId().equals(situacao.getId())) {
                tramite.setDataHoraAtendimento(Calendar.getInstance().getTime());
            }

            tramite.setSituacaoRequisicao(situacao);
            this.tramiteService.save(tramite);

            atendimento.getRequisicao().setTramiteRequisicaoAtual(tramite);
            this.requisicaoService.update(atendimento.getRequisicao());
            
            // #OS310
            if ((SituacaoRequisicaoEnum.ATENDIDA.getId().equals(tramite.getSituacaoRequisicao().getId())
                    || SituacaoRequisicaoEnum.REATENDIDA.getId().equals(tramite.getSituacaoRequisicao().getId())) 
                    && OcorrenciaAtendimentoEnum.ORIGINAL_UNIDADE.getValor().equals(atendimento.getOcorrencia().getId())) {
                this.docOriginalService.registrarAndamento(SituacaoDocumentoOriginalEnum.ENVIADO, atendimento.getRequisicao(), arquivoLote.getCodigoUsuario());
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
    }

    /**
     * Verifica se a ocorrência do atendimento significa que ele foi recuperado, ou seja,
     * 
     * @param ocorrenciaAtendimentoVO
     * @return
     *         <code>true</code> se a ocorrência significa que o documento foi recuperado. <code>false</code> caso contrário.
     */
    private boolean documentoFoiRecuperado(OcorrenciaAtendimentoVO ocorrenciaAtendimentoVO) {
        Set<String> ocorrenciasAtendimento = new HashSet<String>();
        ocorrenciasAtendimento.add(OcorrenciaAtendimentoEnum.DOC_DIGITAL.getLabel());
        ocorrenciasAtendimento.add(OcorrenciaAtendimentoEnum.ORIGINAL_UNIDADE.getLabel());
        ocorrenciasAtendimento.add(OcorrenciaAtendimentoEnum.COPIA_AUTENTICADA.getLabel());

        return ocorrenciasAtendimento.contains(ocorrenciaAtendimentoVO.getNome());
    }

    /**
     * Realiza a análise do registro de atendimento contido no arquivo CSV
     * 
     * @param registro
     *            Linha do CSV
     * @param usuario
     *            Usuário logado no sistema
     * @return Registro de Atendimento
     */
    private AtendimentoLoteDTO analisarRegistroAtendimento(CSVRecord registro, UsuarioLdap usuario) {

        AtendimentoLoteDTO atendimento = new AtendimentoLoteDTO();
        atendimento.setRequisicao(new RequisicaoVO());

        // -- Validações da estrutura do arquivo
        if (registro.size() < CSV_HEADER_QTD_COLUMNS) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("MI033")));
            return atendimento;
        }

        if (ObjectUtils.isNullOrEmpty(registro.get(CSV_HEADER_CODIGO)) || !NumberUtils.isDigits(registro.get(CSV_HEADER_CODIGO))) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME001")));
            return atendimento;
        }

        if (ObjectUtils.isNullOrEmpty(registro.get(CSV_HEADER_OCORRENCIA))) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro,
                    MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_CAMPO_OCORRENCIA_ATENDIMENTO_VAZIO)));
            return atendimento;
        }

        // -- Validações da requisição
        atendimento.setRequisicao(this.requisicaoService.obterPorNumeroID(Long.valueOf(registro.get(CSV_HEADER_CODIGO))));
        if (ObjectUtils.isNullOrEmpty(atendimento.getRequisicao())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro,
                    MensagemUtils.obterMensagem("ME016", registro.get(CSV_HEADER_CODIGO))));
            return atendimento;
        }

        Set<String> situacoesAtendimento = new HashSet<String>();
        situacoesAtendimento.add(SituacaoRequisicaoEnum.ABERTA.getLabel());
        situacoesAtendimento.add(SituacaoRequisicaoEnum.REABERTA.getLabel());
        situacoesAtendimento.add(SituacaoRequisicaoEnum.PEND_DADOS_FAT.getLabel());
        situacoesAtendimento.add(SituacaoRequisicaoEnum.REABERTA_PEND_DADOS_FAT.getLabel());

        atendimento.setSituacaoRequisicao(atendimento.getRequisicao().getTramiteRequisicaoAtual().getSituacaoRequisicao());
        if (!situacoesAtendimento.contains(atendimento.getSituacaoRequisicao().getNome())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro,
                    MensagemUtils.obterMensagem("ME002", atendimento.getRequisicao().getCodigoRequisicao(), atendimento.getSituacaoRequisicao().getNome())));
            return atendimento;
        }

        EmpresaContratoVO empresaContrato = atendimento.getRequisicao().getEmpresaContrato();
        if (!ObjectUtils.isNullOrEmpty(empresaContrato) && !ObjectUtils.isNullOrEmpty(empresaContrato.getEmpresa())
                && !empresaContrato.getEmpresa().getCnpj().equals(usuario.getNuCnpj())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME003")));
            return atendimento;
        }

        // -- Validações da ocorrência de atendimento
        atendimento.setOcorrencia(this.ocorrenciaService.findByFonetica(registro.get(CSV_HEADER_OCORRENCIA)));
        if (ObjectUtils.isNullOrEmpty(atendimento.getOcorrencia())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME009")));
            return atendimento;
        }

        // -- Validações do tipo de suporte
        atendimento.setSuporte(this.suporteService.findByFonetica(registro.get(CSV_HEADER_SUPORTE)));

        if (!ObjectUtils.isNullOrEmpty(registro.get(CSV_HEADER_SUPORTE)) && ObjectUtils.isNullOrEmpty(atendimento.getSuporte())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro,
                    MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_TIPO_SUPORTE_VAZIO)));
            return atendimento;
        }

        boolean documentoFoiRecuperado = documentoFoiRecuperado(atendimento.getOcorrencia());

        if (!documentoFoiRecuperado && !ObjectUtils.isNullOrEmpty(atendimento.getSuporte())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME010")));
            return atendimento;
        }

        if (documentoFoiRecuperado && ObjectUtils.isNullOrEmpty(atendimento.getSuporte())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME017")));
            return atendimento;
        }

        // -- Validações da qtd. disponibilizada
        if (ObjectUtils.isNullOrEmpty(registro.get(CSV_HEADER_QTD))) {
            atendimento.setQtdDisponibilizada(0);
        } else {
            atendimento.setQtdDisponibilizada(Integer.valueOf(registro.get(CSV_HEADER_QTD).trim()));
        }

        if (atendimento.getQtdDisponibilizada() < 0) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro,
                    MensagemUtils.obterMensagem(ConstsSiredExtra.ERRO_QTD_DISPONIBILIZADA_INVALIDA)));
            return atendimento;
        }

        if (!documentoFoiRecuperado && !atendimento.getQtdDisponibilizada().equals(0)) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME012")));
            return atendimento;
        }

        if (documentoFoiRecuperado && atendimento.getQtdDisponibilizada().equals(0)) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(registro, MensagemUtils.obterMensagem("ME018")));
            return atendimento;
        }

        // -- Validações da observação
        if ((registro.size() > CSV_HEADER_QTD_COLUMNS) && !ObjectUtils.isNullOrEmpty(registro.get(CSV_HEADER_OBS))) {
            String obs = registro.get(CSV_HEADER_OBS);
            atendimento.setObservacao(obs.substring(0, Math.min(Constantes.NUM_CHARS_OBSERVACAO_TRAMITE, obs.length())));
        }

        return atendimento;
    }

    /**
     * Raliza a formatação das mensagens de validação que irão compor o relatório de atendimento
     * 
     * @param registro
     *            Linha do CSV
     * @param msg
     *            Mensagem de validação
     * @return Mensagem de validação formatada
     */
    private String getMensagemRelatorioAtendimento(CSVRecord registro, String msg) {
        return String.format("Linha %s - %s", registro.getRecordNumber(), msg);
    }
}
