package br.gov.caixa.gitecsa.sired.util;

import java.util.Arrays;
import java.util.Calendar;

import javax.faces.context.FacesContext;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class LogUtils {
    private static final String CAMPO_FUNCIONALIDADE = "NOME DA FUNCIONALIDADE: ";
    private static final String CAMPO_DESCRICAO_EVENTO = "DESCRIÇÃO DO EVENTO: ";
    private static final String CAMPO_DATA_HORA = "DATA HORA: ";
    private static final String CAMPO_ENDERECO_LOGICO = "ENDEREÇO LÓGICO: ";
    private static final String CAMPO_USUARIO = "USUÁRIO: ";
    private static final String CAMPO_DESCRICAO = "DESCRIÇÃO: ";
    private static final String CAMPO_MENSAGEM = "MENSAGEM: ";
    private static final String CAMPO_CLASSE = "CLASSE: ";
    private static final String CAMPO_PILHA = "PILHA: ";

    private static final String LINE_SEPARATOR = "line.separator";
    private static final String FORMATO_DATA_HORA = "dd/MM/yyyy - HH:mm:ss";
    private static final String COMMA_SEPARATOR = ", ";
    
    public static Logger getLogger(String name) {
        
        String filename = StringUtils.EMPTY;
        
        FacesContext context = FacesContext.getCurrentInstance();
        if (!ObjectUtils.isNullOrEmpty(context)) {
            if (context.getExternalContext().getContextName().contains("extranet") ) {
                filename = System.getProperty(Constantes.EXTRANET_LOG4J);
            } else {
                filename = System.getProperty(Constantes.INTRANET_LOG4J);
            }
            
            return getLogger(name, filename);
        }
        
        throw new LogConfigurationException();
    }
    
    public static Logger getLogger(String name, String filename) {
        DOMConfigurator.configure(filename);
        return Logger.getLogger(name);
    }

    public static String getMensagemPadraoLogManutencao(String funcionalidade, String evento) {
        return getMensagemPadraoLog(StringUtils.EMPTY, funcionalidade, evento);
    }

    public static String getMensagemPadraoLog(String erro, String funcionalidade, String evento) {
        return getMensagemPadraoLog(erro, funcionalidade, evento, RequestUtils.getSessionValue("loggedMatricula"));
    }

    public static String getMensagemPadraoLog(String erro, String funcionalidade, String evento, Object oLogin) {
        StringBuilder conteudo = new StringBuilder();

        // Cabeçalho
        conteudo.append(System.getProperty(LINE_SEPARATOR));
        conteudo.append(Constantes.LOG_MARCADOR);
        conteudo.append(System.getProperty(LINE_SEPARATOR));

        // Funcionalidade
        funcionalidade = StringUtils.defaultString(funcionalidade, StringUtils.EMPTY);
        conteudo.append(CAMPO_FUNCIONALIDADE);
        conteudo.append(funcionalidade);
        conteudo.append(System.getProperty(LINE_SEPARATOR));

        // Evento
        evento = StringUtils.defaultString(evento, StringUtils.EMPTY);
        conteudo.append(CAMPO_DESCRICAO_EVENTO);
        conteudo.append(evento);
        conteudo.append(System.getProperty(LINE_SEPARATOR));

        // Data e Hora
        conteudo.append(CAMPO_DATA_HORA);
        conteudo.append(DateUtils.format(Calendar.getInstance().getTime(), FORMATO_DATA_HORA));
        conteudo.append(System.getProperty(LINE_SEPARATOR));

        // Endereço lógico
        conteudo.append(CAMPO_ENDERECO_LOGICO);
        conteudo.append(RequestUtils.getHostName());
        conteudo.append(System.getProperty(LINE_SEPARATOR));

        // login
        String login = "Login não recuperado.";
        if (oLogin != null) {
            login = StringUtils.defaultString(oLogin.toString(), login);
        }
        conteudo.append(CAMPO_USUARIO);
        conteudo.append(login);
        conteudo.append(System.getProperty(LINE_SEPARATOR));

        // Erro
        conteudo.append(CAMPO_DESCRICAO);
        erro = StringUtils.defaultString(erro, StringUtils.EMPTY);
        conteudo.append(erro);
        conteudo.append(System.getProperty(LINE_SEPARATOR));

        // Rodapé
        conteudo.append(System.getProperty(LINE_SEPARATOR));
        conteudo.append(Constantes.LOG_MARCADOR);

        return conteudo.toString();
    }

    private static String getPilhaPadraoLog(Throwable e) {

        StringBuilder exception = new StringBuilder();

        exception.append(CAMPO_CLASSE);
        exception.append(e.getClass().toString());

        if (!ObjectUtils.isNullOrEmpty(e.getMessage())) {
            exception.append(System.getProperty(LINE_SEPARATOR));
            exception.append(CAMPO_MENSAGEM);
            exception.append(e.getMessage().trim());
        }

        exception.append(System.getProperty(LINE_SEPARATOR));
        exception.append(CAMPO_PILHA);
        if (!ObjectUtils.isNullOrEmpty(e.getStackTrace())) {
            exception.append(Arrays.toString(e.getStackTrace()).replaceAll(COMMA_SEPARATOR, System.getProperty(LINE_SEPARATOR)));
        } else {
            exception.append(StringUtils.EMPTY);
        }
        exception.append(System.getProperty(LINE_SEPARATOR));

        return exception.toString();
    }

    public static String getDescricaoPadraoPilhaExcecaoLog(Throwable e) {

        StringBuilder exception = new StringBuilder();
        exception.append(getPilhaPadraoLog(e));

        Throwable priorException = e;
        Throwable cause = e.getCause();
        while (cause != null && (!cause.equals(priorException))) {
            exception.append(getPilhaPadraoLog(cause));

            priorException = cause;
            cause = cause.getCause();
        }

        return exception.toString();
    }

    public static String getNomeFuncionalidade(String nomeFuncionalidade) {
        return nomeFuncionalidade.replace("Service", "");

    }

    public static String getDescricaoOperacao(String nomeFuncionalidade) {
        return ((nomeFuncionalidade.startsWith("save") || nomeFuncionalidade.startsWith("salvar")) ? "Inserir"
                : (nomeFuncionalidade.equalsIgnoreCase("delete") ? "Excluir" : (nomeFuncionalidade.equalsIgnoreCase("update") ? "Atualizar"
                        : (nomeFuncionalidade.equalsIgnoreCase("saveOrUpdate") ? "Inserir/Atualizar" : nomeFuncionalidade))));

    }
}
