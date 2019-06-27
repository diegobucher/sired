package br.gov.caixa.gitecsa.service.mail;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import br.gov.caixa.gitecsa.sired.arquitetura.exception.DataBaseException;
import br.gov.caixa.gitecsa.sired.service.ParametroSistemaService;
import br.gov.caixa.gitecsa.sired.util.Constantes;
import br.gov.caixa.gitecsa.sired.util.LogUtils;
import br.gov.caixa.gitecsa.sired.util.ObjectUtils;
import br.gov.caixa.gitecsa.sired.util.Util;

public class EnviaEmailService {
    // CONSTANTES ENVIO DE EMAIL
    private static String ENDERECO_SERVIDOR_EMAIL = "ENDERECO_SERVIDOR_EMAIL";
    private static String NO_MAIL_ORIGEM = "NO_MAIL_ORIGEM";
    private static String PORTA_SERVIDOR_EMAIL = "PORTA_SERVIDOR_EMAIL";
    public final static String SEPARADOR_EMAILS = ", ";

    // CONSTANTES HTML
    public static final String PREFIXO_SITE_EXTRANET = "http://www.arquivo.caixa.gov.br/SiredExtranet/";
    public static final String PREFIXO_SITE_INTRANET = "http://sired.caixa/sired/";
    public static final String HTML_TABELA_INICIO = "<TABLE WIDTH='100%'>";
    public static final String HTML_TABELA_FIM = "</TABLE>";
    public static final String HTML_QUEBRA_LINHA = "<BR />";

    public static boolean EhUsuarioTerceirizada(String usuario) {
        if (!ObjectUtils.isNullOrEmpty(usuario)) {
            if (usuario.contains("@")) {
                return true;
            }
        }
        return false;
    }

    private static String montarEmailLog(String mensagem) {
        return montarEmailLog("INFO", mensagem);
    }

    private static String montarEmailLog(String tipo, String mensagem) {
        return Constantes.LOG_MARCADOR + Util.getTimestamp() + " BATCH DE ENVIO DE EMAIL - " + tipo + ": " + mensagem + Constantes.LOG_MARCADOR;
    }

    // Mensagens de erro/exceção.
    public static String montarEmailLogErro(String mensagem) {
        return montarEmailLog("ERRO", mensagem);
    }

    public static String getLogErrorException(Exception e, String funcionalidade, String evento) {
        return LogUtils.getMensagemPadraoLog(LogUtils.getDescricaoPadraoPilhaExcecaoLog(e), funcionalidade, evento);
    }

    public static String getLogErroInesperado() {
        return montarEmailLogErro("ERRO INESPERADO:");
    }

    public static String getLogErroEnvioEmail() {
        return montarEmailLogErro("SERVIÇO DE CORREIO INDISPONÍVEL.");
    }

    // Mensagens de Agendamento.
    public static String getLogAgendado(Date data) {
        String agendamento = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(data);
        return montarEmailLog("AGENDADO COM SUCESSO PARA: " + agendamento);
    }

    public static String getLogInicio() {
        return montarEmailLog("INÍCIO DO ENVIO DE E-MAILS.");
    }

    // Mensagens após envio bem sucedido de e-mails.
    public static String montarEmailLogRequisicaoSolicitante(int totalEnviado) {
        return montarEmailLog("EVENTO ENVIAR EMAIL - REQUISIÇÃO SOLICITANTE - " + totalEnviado + " MENSAGENS ENVIADAS.");
    }

    public static String montarEmailLogRequisicaoBase(int totalEnviado) {
        return montarEmailLog("EVENTO ENVIAR EMAIL - REQUISIÇÃO BASE - " + totalEnviado + " MENSAGENS ENVIADAS.");
    }

    public static String montarEmailLogRequisicaoTerceirizada(int totalEnviado) {
        return montarEmailLog("EVENTO ENVIAR EMAIL - REQUISIÇÃO TERCEIRIZADA - " + totalEnviado + " MENSAGENS ENVIADAS.");
    }

    public static String montarEmailLogRemessaSolicitante(int totalEnviado) {
        return montarEmailLog("EVENTO ENVIAR EMAIL - REMESSA SOLICITANTE - " + totalEnviado + " MENSAGENS ENVIADAS.");
    }

    public static String montarEmailLogRemessaBase(int totalEnviado) {
        return montarEmailLog("EVENTO ENVIAR EMAIL - REMESSA BASE - " + totalEnviado + " MENSAGENS ENVIADAS.");
    }

    public static String montarEmailLogRemessaTerceirizada(int totalEnviado) {
        return montarEmailLog("EVENTO ENVIAR EMAIL - REMESSA TERCEIRIZADA - " + totalEnviado + " MENSAGENS ENVIADAS.");
    }

    // Mensagens de Log para Envio Desativado.
    public static String getLogSolicitanteEnvioDesativado() {
        return montarEmailLog("O envio de e-mails para o Solicitante está desativado.");
    }

    public static String getLogBaseEnvioDesativado() {
        return montarEmailLog("O envio de e-mails para a Base de Arquivo está desativado.");
    }

    public static String getLogTerceirizadaEnvioDesativado() {
        return montarEmailLog("O envio de e-mails para a Terceirizada desativado.");
    }

    // Mensagens de Log quando não existem pendências.
    public static String getLogRequisicaoSolicitanteSemPendencia() {
        return montarEmailLog("Não existem pendências de envio de Requisição para o Solicitante.");
    }

    public static String getLogRequisicaoBaseSemPendencia() {
        return montarEmailLog("Não existem pendências de envio de Requisição para a Base de Arquivo.");
    }

    public static String getLogRequisicaoTerceirizadaSemPendencia() {
        return montarEmailLog("Não existem pendências de envio de Requisição para a Terceirizada.");
    }

    public static String getLogRemessaSolicitanteSemPendencia() {
        return montarEmailLog("Não existem pendências de envio de Remessa para o Solicitante.");
    }

    public static String getLogRemessaBaseSemPendencia() {
        return montarEmailLog("Não existem pendências de envio de Remessa para a Base de Arquivo.");
    }

    public static String getLogRemessaTerceirizadaSemPendencia() {
        return montarEmailLog("Não existem pendências de envio de Remessa para a Terceirizada.");
    }

    public static String getHtmlLinha(String[] colunas) {
        StringBuilder linha = new StringBuilder();
        linha.append("<TR align='left'>");
        for (int i = 0; i < colunas.length; i++) {
            linha.append("<TD align='left'>");
            linha.append(StringUtils.defaultIfEmpty(colunas[i], StringUtils.EMPTY));
            linha.append("</TD>");
        }
        linha.append("</TR>");
        return linha.toString();
    }

    public static String getHtmlCabecalho(boolean extranet) {
        StringBuilder rodape = new StringBuilder();
        rodape.append(HTML_QUEBRA_LINHA);
		rodape.append(Constantes.LOG_MARCADOR);
		rodape.append(HTML_QUEBRA_LINHA);
        rodape.append("SIRED – SISTEMA DE REMESSA E RECUPERAÇÃO DE DOCUMENTOS");
        rodape.append(HTML_QUEBRA_LINHA);
        rodape.append("EM CASO DE DÚVIDAS, FAVOR ENTRAR EM CONTATO COM A BASE DE ARQUIVO DE VINCULAÇÃO.");
        rodape.append(HTML_QUEBRA_LINHA);
        rodape.append("MENSAGEM GERADA AUTOMATICAMENTE. FAVOR NÃO RESPONDER");
		rodape.append(HTML_QUEBRA_LINHA);
        rodape.append(Constantes.LOG_MARCADOR);
		rodape.append(HTML_QUEBRA_LINHA);
		rodape.append(HTML_QUEBRA_LINHA);
        return rodape.toString();
    }

    public static String getHtmlRodape(boolean extranet) {
        StringBuilder rodape = new StringBuilder();
        rodape.append(HTML_QUEBRA_LINHA);
        if (extranet) {
            rodape.append("Para atendimento acesse " + PREFIXO_SITE_EXTRANET);
        } else {
            rodape.append("Para acompanhamento acesse " + PREFIXO_SITE_INTRANET);
        }

        rodape.append(HTML_QUEBRA_LINHA);
        rodape.append("MENSAGEM GERADA AUTOMATICAMENTE. FAVOR NÃO RESPONDER");
        rodape.append(HTML_QUEBRA_LINHA);
        return rodape.toString();
    }

    public static String montaHtmlLink(boolean extranet, String pagina, String codigo) {
        String link = "<a href='";
        if (extranet) {
            link += PREFIXO_SITE_EXTRANET;
        } else {
            link += PREFIXO_SITE_INTRANET;
        }
        return link + pagina + codigo + "'>" + codigo + "</a>";
    }

    public static void enviaEmail(ParametroSistemaService parametroSistemaService, String titulo, Destinatario destinatario, StringBuilder mensagem, String file, String nomeArquivo)
            throws EmailException, DataBaseException {
        if (!ObjectUtils.isNullOrEmpty(destinatario) && !ObjectUtils.isNullOrEmpty(destinatario.getEnderecoEmail())) {
            HtmlEmail lEmail = new HtmlEmail();

            lEmail.setHostName(parametroSistemaService.findByNome(ENDERECO_SERVIDOR_EMAIL).getVlParametroSistema());
            lEmail.setSmtpPort(Integer.parseInt(parametroSistemaService.findByNome(PORTA_SERVIDOR_EMAIL).getVlParametroSistema()));
            lEmail.setFrom(parametroSistemaService.findByNome(NO_MAIL_ORIGEM).getVlParametroSistema());

            lEmail.addTo(destinatario.getEnderecoEmail().toLowerCase());
            
            if(file != null) {
              EmailAttachment attachment = new EmailAttachment();
              attachment.setPath(file);
              attachment.setDisposition(EmailAttachment.ATTACHMENT);
              attachment.setDescription("Descricao");
              attachment.setName(nomeArquivo);
              
              lEmail.attach(attachment);
            }
            
            Set<String> destinatariosCampoComCopia = destinatario.getDestinatariosCampoComCopia();
            if (!ObjectUtils.isNullOrEmpty(destinatariosCampoComCopia)) {
                for (String dest : destinatariosCampoComCopia) {
                    if (!ObjectUtils.isNullOrEmpty(dest)) {
                        lEmail.addCc(dest.toLowerCase());
                    }
                }
            }

            lEmail.setSubject(titulo);
            lEmail.setSSLOnConnect(false); // Para funcionar na caixa esse atributo
                                           // deve ser false
            lEmail.setStartTLSEnabled(false); // Para funcionar na caixa esse
                                              // atributo deve ser false
            lEmail.setHtmlMsg(mensagem.toString());
            lEmail.setCharset("UTF-8");
            lEmail.send();
        }
    }
}
