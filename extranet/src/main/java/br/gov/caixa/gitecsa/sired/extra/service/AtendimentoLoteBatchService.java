package br.gov.caixa.gitecsa.sired.extra.service;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.mail.EmailException;

import br.gov.caixa.gitecsa.ldap.usuario.UsuarioLdap;
import br.gov.caixa.gitecsa.sired.arquitetura.dao.GenericDAO;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.AppException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.ArquivoLoteValidationException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.BusinessException;
import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.arquitetura.service.AbstractService;
import br.gov.caixa.gitecsa.sired.enumerator.MimeTypeEnum;
import br.gov.caixa.gitecsa.sired.enumerator.OcorrenciaAtendimentoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.extra.dto.AtendimentoLoteDTO;
import br.gov.caixa.gitecsa.sired.extra.report.RelatorioAtendimentoLoteBatch;
import br.gov.caixa.gitecsa.sired.extra.report.RelatorioAtendimentoLoteCSV;
import br.gov.caixa.gitecsa.sired.extra.report.RelatorioAtendimentoLoteDocDigital;
import br.gov.caixa.gitecsa.sired.extra.report.Reportable;
import br.gov.caixa.gitecsa.sired.extra.util.ConstsSiredExtra;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.DateUtils;
import br.gov.caixa.gitecsa.sired.util.FileUtils;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.MailUtils;
import br.gov.caixa.gitecsa.sired.util.MensagemUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;
import br.gov.caixa.gitecsa.sired.vo.ArquivoLoteVO;
import br.gov.caixa.gitecsa.sired.vo.EmpresaVO;
import br.gov.caixa.gitecsa.sired.vo.OcorrenciaAtendimentoVO;
import br.gov.caixa.gitecsa.sired.vo.SituacaoRequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.TramiteRequisicaoVO;

@Stateless
public class AtendimentoLoteBatchService extends AbstractService<ArquivoLoteVO> implements Serializable {

    private static final long serialVersionUID = 6759794746987415978L;

    private static final String PADRAO_NOME_DOC_DIGITAL = "RED_REQ_([0-9]{11})_[0-9]{10}\\.(ZIP|PDF)";

    private static final String MAIL_SERVER = "ENDERECO_SERVIDOR_EMAIL";
    private static final String MAIL_FROM = "NO_MAIL_ORIGEM";
    private static final String MAIL_PORT = "PORTA_SERVIDOR_EMAIL";
    private static final String MAIL_GESTOR = "EMAIL_AREA_GESTORA";

    @Inject
    private ArquivoLoteService arquivoLoteService;

    @Inject
    private RequisicaoService requisicaoService;

    @Inject
    private AtendimentoLoteService atendimentoLoteSevice;

    @Inject
    private TramiteRequisicaoService tramiteService;

    @Inject
    private EmpresaService empresaService;

    @Inject
    private ParametroSistemaService parametroSistemaService;

    @Inject
    private SituacaoRequisicaoService situacaoRequisicaoService;

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

    private void carregarArquivos(File parentFolder, List<File> fileList) {
        for (File file : parentFolder.listFiles()) {
            if (file.isDirectory()) {
                carregarArquivos(file, fileList);
            } else {
                fileList.add(file);
            }
        }
    }

    /**
     * Realiza o atendimento em lote por meio de um TimeTask (processo batch)
     * 
     * @param arquivoCompactado
     *            Nome do arquivo ZIP contendo arquivos de atendimento CSV e documentos digitais no formato ZIP ou PDF
     * @param diretorioTemporario
     *            Diretório temporário onde o arquivo ZIP será descompactado para ser processado
     * @param usuario
     *            Usuário logado no sistema
     * @throws BusinessException
     * @throws AppException
     */
    public void atenderBatch(String arquivoCompactado, String diretorioTemporario, UsuarioLdap usuario, ArquivoLoteVO arquivoLote) throws BusinessException,
            AppException {

        try {

            FileUtils.unzipFile(arquivoCompactado, diretorioTemporario);

            File tmpDir = new File(diretorioTemporario);

            List<File> fileList = new ArrayList<File>();
            carregarArquivos(tmpDir, fileList); // percorre todos os diretórios recursivamente adicionando os arquivos na fileList.

            RelatorioAtendimentoLoteBatch relatorio = new RelatorioAtendimentoLoteBatch(arquivoLote);

            for (File arquivo : fileList) {
                Reportable subRelatorio = null;
                if (this.isArquivoAtendimento(arquivo)) {
                    subRelatorio = (RelatorioAtendimentoLoteCSV) this.atendimentoLoteSevice.atenderBatch(arquivo, arquivoLote, usuario);
                } else if (this.isDocumentoDigital(arquivo)) {
                    subRelatorio = this.importarLote(arquivo, arquivoLote, usuario);
                } else {
                    relatorio.addDetail(MensagemUtils.obterMensagem("ME024", arquivo.getName()));
                }

                if (!ObjectUtils.isNullOrEmpty(subRelatorio)) {
                    relatorio.addDetail(subRelatorio);
                    // relatorio
                    if (subRelatorio instanceof RelatorioAtendimentoLoteCSV) {
                        relatorio.adicionar(arquivo.getName(), ((RelatorioAtendimentoLoteCSV) subRelatorio).getItens(arquivo.getName()));
                    } else if (subRelatorio instanceof RelatorioAtendimentoLoteDocDigital) {
                        relatorio.adicionar(arquivo.getName(), ((RelatorioAtendimentoLoteDocDigital) subRelatorio).getItens(arquivo.getName()));
                    }
                }
            }

            relatorio.buildSumario();
            relatorio.createHeader();
            relatorio.createFooter();
            relatorio.save(arquivoLote.getRelatorioLote(), System.getProperty(Constantes.EXTRANET_DIRETORIO_RELATORIOS));

            org.apache.commons.io.FileUtils.deleteQuietly(new File(diretorioTemporario));
            this.enviarRelatorioEmailTerceirizada(arquivoLote, relatorio, usuario);

            this.arquivoLoteService.concluir(arquivoLote, false);

        } catch (BusinessException e) {
            this.arquivoLoteService.concluir(arquivoLote, true);
            this.notificarErroAreaGestora(arquivoCompactado, usuario, e);
            throw e;
        } catch (Exception e) {
            this.arquivoLoteService.concluir(arquivoLote, true);
            this.notificarErroAreaGestora(arquivoCompactado, usuario, e);
            throw new AppException(e.getMessage(), e);
        }
    }

    /**
     * Realiza a importação dos documentos digitais
     * 
     * @param arquivo
     *            Documentos digital
     * @param arquivoLote
     *            Arquivo de Lote
     * @param usuario
     *            Usuário logado no sistema
     * @return Relatório resultante do processamento em lote
     * @throws IOException
     * @throws ArquivoLoteValidationException
     * @throws BusinessException
     */
    private Reportable importarLote(File arquivo, ArquivoLoteVO arquivoLote, UsuarioLdap usuario) throws IOException, ArquivoLoteValidationException,
            BusinessException {

        String originalFilename = arquivo.getName();
        RelatorioAtendimentoLoteDocDigital relatorio = new RelatorioAtendimentoLoteDocDigital();

        String docDigital = FileUtils
                .appendDateTimeToFileName(arquivo.getCanonicalPath(), new Date(), RelatorioAtendimentoLoteDocDigital.SUFIXO_NOME_RELATORIO);
        File arquivoComTimestamp = FileUtils.renameFile(arquivo, docDigital);

        File diretorio = FileUtils.createDirIfNotExists(System.getProperty(Constantes.EXTRANET_DIRETORIO_DOCUMENTOS));
        org.apache.commons.io.FileUtils.moveFileToDirectory(arquivoComTimestamp, diretorio, true);

        AtendimentoLoteDTO atendimento = this.analisarDocumentoDigital(arquivoComTimestamp.getName(), arquivo.getName());

        if (!ObjectUtils.isNullOrEmpty(atendimento.getMensagensValidacao())) {
            for (String msg : atendimento.getMensagensValidacao()) {
                relatorio.addDetail(msg);
            }

            if (!ObjectUtils.isNullOrEmpty(atendimento.getRequisicao()) && !ObjectUtils.isNullOrEmpty(atendimento.getRequisicao().getCodigoRequisicao())) {
                relatorio.adicionar(originalFilename, atendimento.getRequisicao().getCodigoRequisicao().toString(), false);
            }
            return relatorio;
        }

        try {
            this.salvar(arquivoComTimestamp, arquivoLote, atendimento);
            relatorio.adicionar(originalFilename, atendimento.getRequisicao().getCodigoRequisicao().toString(), true);
        } catch (BusinessException e) {

            relatorio.adicionar(originalFilename, atendimento.getRequisicao().getCodigoRequisicao().toString(), false);
            LogUtils.getMensagemPadraoLog(e.getMessage(), this.getClass().getSimpleName(), StringUtils.EMPTY, usuario.getEmail());
        }

        return relatorio;
    }

    /**
     * Salva a importação de um documento digital
     * 
     * @param arquivo
     *            Documento digital
     * @param arquivoLote
     *            Arquivo de Lote
     * @param atendimento
     *            Dados do Atendimento
     * @throws BusinessException
     */
    private void salvar(File arquivo, ArquivoLoteVO arquivoLote, AtendimentoLoteDTO atendimento) throws BusinessException {

        try {

            TramiteRequisicaoVO tramiteNovo = new TramiteRequisicaoVO();

            Util.copiarInformacoesTramite(atendimento.getRequisicao().getTramiteRequisicaoAtual(), tramiteNovo);

            tramiteNovo.setCodigoUsuario(arquivoLote.getCodigoUsuario());
            tramiteNovo.setDataHora(new Date());
            tramiteNovo.setArquivoLote(arquivoLote);
            tramiteNovo.setRequisicao(atendimento.getRequisicao());
            tramiteNovo.setArquivoDisponibilizado(arquivo.getName());

            tramiteNovo.setOcorrencia(new OcorrenciaAtendimentoVO());
            tramiteNovo.getOcorrencia().setId(OcorrenciaAtendimentoEnum.DOC_DIGITAL.getValor());

            SituacaoRequisicaoVO situacao = null;
            SituacaoRequisicaoVO situacaoAtual = atendimento.getRequisicao().getTramiteRequisicaoAtual().getSituacaoRequisicao();
            if (SituacaoRequisicaoEnum.ABERTA.getId().equals(((Long) situacaoAtual.getId()))) {
                // ABERTA => PEND ATENDIMENTO
                situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.PEND_DADOS_FAT.getId());
            } else if (SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getId().equals(((Long) situacaoAtual.getId()))) {
                // PEND UPLOAD => ATENDIDA
                situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.ATENDIDA.getId());
            } else if (SituacaoRequisicaoEnum.REABERTA.getId().equals(((Long) situacaoAtual.getId()))) {
                // REABERTA => PEND ATENDIDA (REABERTA)
                situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.REABERTA_PEND_DADOS_FAT.getId());
            } else if (SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getId().equals(((Long) situacaoAtual.getId()))) {
                // PEND UPLOAD (REABERTA) => REATENDIDA
                situacao = this.situacaoRequisicaoService.findById(SituacaoRequisicaoEnum.REATENDIDA.getId());
            }

            if (SituacaoRequisicaoEnum.ATENDIDA.getId().equals(situacao.getId()) || SituacaoRequisicaoEnum.REATENDIDA.getId().equals(situacao.getId())) {
                tramiteNovo.setDataHoraAtendimento(Calendar.getInstance().getTime());
            }

            tramiteNovo.setSituacaoRequisicao(situacao);

            this.tramiteService.save(tramiteNovo);

            atendimento.getRequisicao().setTramiteRequisicaoAtual(tramiteNovo);
            this.requisicaoService.update(atendimento.getRequisicao());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(MensagemUtils.obterMensagem(ConstsSiredExtra.ALERTA_ERRO_DESCONHECIDO));
        }
    }

    /**
     * Analisar documento digital
     * 
     * @param arquivoComTimestamp
     *            Nome do documento digital
     * @return Registro de Atendimento
     */
    private AtendimentoLoteDTO analisarDocumentoDigital(String arquivoComTimestamp, String nomeArquivoOriginal) {

        AtendimentoLoteDTO atendimento = new AtendimentoLoteDTO();

        if (!Pattern.matches(PADRAO_NOME_DOC_DIGITAL, arquivoComTimestamp.toUpperCase())) {
            atendimento.adicionarMensagemValidacao(MensagemUtils.obterMensagem("ME015", nomeArquivoOriginal));
            return atendimento;
        }

        Long codRequisicao = Long.valueOf(arquivoComTimestamp.toUpperCase().replaceAll(PADRAO_NOME_DOC_DIGITAL, "$1"));
        atendimento.setRequisicao(this.requisicaoService.obterPorNumeroID(codRequisicao));
        if (ObjectUtils.isNullOrEmpty(atendimento.getRequisicao())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(MensagemUtils.obterMensagem("ME016", codRequisicao.toString())));
            return atendimento;
        }

        Set<String> situacoesAtendimento = new HashSet<String>();
        situacoesAtendimento.add(SituacaoRequisicaoEnum.ABERTA.getLabel());
        situacoesAtendimento.add(SituacaoRequisicaoEnum.REABERTA.getLabel());
        situacoesAtendimento.add(SituacaoRequisicaoEnum.PENDENTE_UPLOAD.getLabel());
        situacoesAtendimento.add(SituacaoRequisicaoEnum.REABERTA_PENDENTE_UPLOAD.getLabel());

        atendimento.setSituacaoRequisicao(atendimento.getRequisicao().getTramiteRequisicaoAtual().getSituacaoRequisicao());
        if (!situacoesAtendimento.contains(atendimento.getSituacaoRequisicao().getNome())) {
            atendimento.adicionarMensagemValidacao(getMensagemRelatorioAtendimento(MensagemUtils.obterMensagem("ME002", atendimento.getRequisicao()
                    .getCodigoRequisicao(), atendimento.getSituacaoRequisicao().getNome())));
            return atendimento;
        }

        return atendimento;
    }

    /**
     * Verifica se o arquivo é um documento digital
     * 
     * @param arquivo
     *            Nome do arquivo
     * @return <b>True</b> se o arquivo for do tipo <b>ZIP</b> ou <b>PDF</b> e <b>false</b> caso contrário
     */
    private boolean isDocumentoDigital(File arquivo) {
        return arquivo.getName().toLowerCase().endsWith(MimeTypeEnum.ZIP_COMPRESSED.getExtension())
                || arquivo.getName().toLowerCase().endsWith(MimeTypeEnum.PDF.getExtension());
    }

    /**
     * Verifica se o arquivo é um CSV de atendimento
     * 
     * @param arquivo
     *            Nome do arquivo
     * @return <b>True</b> se o arquivo for do tipo <b>TXT</b> ou <b>CSV</b> e <b>false</b> caso contrário
     */
    private boolean isArquivoAtendimento(File arquivo) {
        return arquivo.getName().toLowerCase().endsWith(MimeTypeEnum.CSV.getExtension())
                || arquivo.getName().toLowerCase().endsWith(MimeTypeEnum.TXT.getExtension());
    }

    /**
     * Raliza a formatação das mensagens de validação que irão compor o relatório de atendimento
     * 
     * @param codRequisicao
     *            Código da requisição
     * @param msg
     *            Mensagem de validação
     * @return Mensagem de validação formatada
     */
    private String getMensagemRelatorioAtendimento(String msg) {
        return String.format("DOCUMENTO DIGITAL INVÁLIDO - %s", msg);
    }

    /**
     * Envia o relatório de atendimento para a empresa terceirizada
     * 
     * @param arquivoLote
     *            Arquivo de lote
     * @param relatorio
     *            Relatório do atendimento
     * @param usuario
     *            Usuário logado
     * @throws DataBaseException 
     */
    private void enviarRelatorioEmailTerceirizada(ArquivoLoteVO arquivoLote, RelatorioAtendimentoLoteBatch relatorio, UsuarioLdap usuario) throws DataBaseException {

        try {

            EmpresaVO empresa = this.empresaService.obterEmpresaCNPJ(usuario.getNuCnpj());

            Map<String, String> config = new HashMap<String, String>();
            config.put(MailUtils.MAIL_SETTING_HOST, this.parametroSistemaService.findByNome(MAIL_SERVER).getVlParametroSistema());
            config.put(MailUtils.MAIL_SETTING_PORT, this.parametroSistemaService.findByNome(MAIL_PORT).getVlParametroSistema());

            String from = parametroSistemaService.findByNome(MAIL_FROM).getVlParametroSistema();

            StringBuffer body = new StringBuffer();
            body.append(String.format("<p>Prezado(a) %s,</p>", empresa.getNome()));
            body.append(String.format("<p>Veja em anexo o Relatório do Processamento em Lote do arquivo %s enviado em %s.</p>", arquivoLote.getNome(),
                    DateUtils.format(arquivoLote.getDataEnvioArquivo(), DateUtils.DATETIME_FORMAT)));
            body.append("<p>Para atendimento acesse http://www.arquivo.caixa.gov.br</p>");
            body.append("SIRED – SISTEMA DE REMESSA E RECUPERAÇÃO DE DOCUMENTOS.<br/>");
            body.append("MENSAGEM GERADA AUTOMATICAMENTE. FAVOR NÃO RESPONDER.<br/>");

            MailUtils.send(from, empresa.getCaixaPostal(), "SIRED - Relatório do Processamento em Lote", body, relatorio.getFile(), config);

        } catch (EmailException e) {
            LogUtils.getMensagemPadraoLog(e.getMessage(), this.getClass().getSimpleName(), StringUtils.EMPTY, usuario.getEmail());
        }
    }

    /**
     * Notifica a área gestora de erros ocorridos no processamento
     * 
     * @param arquivo
     *            Arquivo enviado pelo usuário
     * @param usuario
     *            Usuário solicitante
     * @param erro
     *            Erro disparado
     */
    private void notificarErroAreaGestora(String arquivo, UsuarioLdap usuario, Throwable erro) {

        try {

            EmpresaVO empresa = this.empresaService.obterEmpresaCNPJ(usuario.getNuCnpj());

            Map<String, String> config = new HashMap<String, String>();
            config.put(MailUtils.MAIL_SETTING_HOST, this.parametroSistemaService.findByNome(MAIL_SERVER).getVlParametroSistema());
            config.put(MailUtils.MAIL_SETTING_PORT, this.parametroSistemaService.findByNome(MAIL_PORT).getVlParametroSistema());

            String from = parametroSistemaService.findByNome(MAIL_FROM).getVlParametroSistema();
            String to = parametroSistemaService.findByNome(MAIL_GESTOR).getVlParametroSistema();

            StringBuffer body = new StringBuffer();
            body.append("<p>Prezado(a) Área Gestora do SIRED,</p>");
            body.append(String.format("<p>Informamos que o processamento do arquivo em lote %s, ", org.apache.commons.io.FilenameUtils.getName(arquivo)));
            body.append(String.format("enviado em %s ", DateUtils.format(new Date(), DateUtils.DATETIME_FORMAT)));
            body.append(String.format("pelo usuário %s, foi abortado pelo motivo abaixo.</p>", empresa.getCaixaPostal()));
            body.append("<p>Favor contactar a GITEC/SA.</p>");

            body.append("****************************************************************************<br/>");
            body.append(String.format("%s BATCH DE PROCESSAMENTO DE ARQUIVO - ERRO: ERRO INESPERADO.", DateUtils.format(new Date(), DateUtils.DATETIME_FORMAT)));
            body.append(String.format("<pre>%s</pre><br/>", ExceptionUtils.getStackTrace(erro)));
            body.append("****************************************************************************<br/>");

            body.append("<p>Para acompanhamento acesse http://sired.caixa.</p>");
            body.append("SIRED – SISTEMA DE REMESSA E RECUPERAÇÃO DE DOCUMENTOS.<br/>");
            body.append("MENSAGEM GERADA AUTOMATICAMENTE. FAVOR NÃO RESPONDER.<br/>");

            MailUtils.send(from, to, "Erro ao processar arquivo", body, config);

        } catch (Exception e) {
            LogUtils.getMensagemPadraoLog(e.getMessage(), this.getClass().getSimpleName(), StringUtils.EMPTY, usuario.getEmail());
        }
    }
}
