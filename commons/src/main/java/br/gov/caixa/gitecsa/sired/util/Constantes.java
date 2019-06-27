package br.gov.caixa.gitecsa.sired.util;

/**
 * Classse utilizada para armazenar as constantes do sistema.
 * 
 * @author esouzaa
 * @author jteixeira
 * @author jsouzasa
 * @author rmotal
 * 
 */
public class Constantes {

    public static final String LOG_MARCADOR = FileUtils.SYSTEM_EOL + "*****************************************************************************************************************************" + FileUtils.SYSTEM_EOL;

    public static final String USUARIOS_CN_LDAP = "configuracao.ldap.usuarios.cn";
    public static final String DOMINIO_DC_LDAP = "configuracao.ldap.dominio.dc";
    public static final String DOMINIO_LDAP = "configuracao.ldap.dominio";
    public static final String USUARIO_ADMIN_LDAP = "configuracao.ldap.usuario.admin";
    public static final String USUARIO_ADMIN_PWD_LDAP = "configuracao.ldap.usuario.pwd";
    public static final String CHAVE_USUARIO_LDAP = "configuracao.ldap.chave.usuario";
    public static final String DIRETORIO_RAIZ_USUARIOS_LDAP = "configuracao.ldap.diretorio.usuarios";
    public static final String FILTRO_USUARIO_LDAP = "configuracao.ldap.filtro.usuario";
    public static final String SECURITY_DOMAIN = "sired.intranet.security.domain";
    public static final String NOME_SISTEMA = "sired.intranet.grupo.sistema";
	public static final String PARAMETRO_DESENVOLVIMENTO = "DESENVOLVIMENTO";

    // RELATÓRIOS
    public static final String CAMINHO_JASPER = "/reports/";
    public static final String CAMINHO_IMG = "/reports/images/";

    public static final char SEPARADOR_CSV = ';';

    public static final String PADRAO_MATRICULA_CAIXA = "((([cpemtCPEMT]){1})([0-9]*))";
    public static final String FORMATO_DATA = "dd/MM/yyyy";
    public static final String PADRAO_FORMATO_DATA = "__/__/____";
    public static final String PADRAO_FORMATO_CNPJ = "__.___.___/____-__";
    public static final String VAZIO = "";

    // BANCO DE DADOS
    public static final String SCHEMADB_NAME = "redsm001";
    public static final String SCHEMA_SIICODB_NAME = "icosm001";

    public static final String PARTIAL_AJAX = "partial/ajax";

    public static final String FACES_REQUEST = "Faces-Request";

    public static final String OMNI_PARTIAL_VIEW_CONTEXT = "org.omnifaces.context.OmniPartialViewContext";

    /**
     * É necessário haver um documento com o nome abaixo para o atalho para abertura de Movimento Diário funcionar. Caso seja necessário alterar este nome,
     * atentar que existem views que também fazem referência à este nome.
     */
    public static final String DOCUMENTO_REMESSA_MOVIMENTO_DIARIO = "REMESSA MOVIMENTO DIARIO";

    public static final String CAMINHO_UPLOAD = "sired.intranet.documentos";
    
    public static final String CAMINHO_UPLOAD_JUSTIFICATIVA = "sired.intranet.justificativas";

    public static final String PERFIL_GESTOR = "GEST";

    public static final String PERFIL_AUDITOR = "ADT";
    
    public static final String PERFIL_GERENTE_LOGIN = "GERENTE";
    
    public static final String PATH_ARQUIVOS_SERVIDOR_EXTRA = "sired.extranet.documentos";

    public static final String PARAMETRO_SISTEMA_QTD_MINHAS_REQUISICOES = "QUANTIDADE_MINHAS_REQUISICOES";

    public static final String PARAMETRO_SISTEMA_PZ_MANUTENCAO_ARQUIVOS = "PZ_MANUTENCAO_ARQUIVOS";

    public static final String PARAMETRO_SISTEMA_PZ_REABERTURA = "PZ_REABERTURA";

    public static final String PARAMETRO_SISTEMA_NU_INTERVALO_FECHAMENTO = "NU_INTERVALO_FECHAMENTO";
    
    public static final String PARAMETRO_SISTEMA_NU_INTERVALO_CONFIRMACAO = "NU_INTERVALO_CONFIRMACAO";
    
    public static final String PARAMETRO_SISTEMA_PZ_MOVIMENTO_DIARIO = "PZ_MOVIMENTO_DIARIO";
    
    public static final String PARAMETRO_SISTEMA_PZ_CONFIRMACAO_AUTOMATICA_REMESSA = "PZ_CONFIRM_AUTOMATICA_REMESSA";

    public static final String PARAMETRO_SISTEMA_NU_HORA_ATUALIZA_TABELA = "NU_HORA_ATUALIZA_TABELA";
    
    public static final String PARAMETRO_SISTEMA_NU_HOR_JOB_REQ_REM_ABERTA = "NU_HOR_JOB_REQ_REM_ABERTA";

    public static final String PARAMETRO_SISTEMA_NU_INTERVALO_JOB_REQ_REM_ABERTA = "NU_INTERVALO_JOB_REQ_REM_ABERT";

    public static final String PARAMETRO_SISTEMA_PZ_DOCUMENTOS_TIPO_C = "PZ_DOCUMENTOS_TIPO_C";
    
    public static final String PARAMETRO_SISTEMA_NU_INTERVALO_ATUALIZA_TABELA = "NU_INTERVALO_ATUALIZA_TABELA";
    
    public static final String PARAMETRO_SISTEMA_QTD_LINHAS_MODELO_DOCUMENTO = "QTD_LINHAS_MODELO_DOCUMENTO";
    
    public static final int PADRAO_QDT_LINHAS_MODELO_DOCUMENTO = 20;
    
    // Constantes para Envio de Email.
    public static final String PARAMETRO_SISTEMA_NU_HORA_ENVIO_EMAIL = "NU_HORA_ENVIO_EMAIL";
    public static final String PARAMETRO_SISTEMA_INTERVALO_ENVIO_EMAIL = "NU_INTERVALO_ENVIO_EMAIL";
    public static final String PARAMETRO_SISTEMA_ENVIAR_EMAIL_BASE = "ENVIAR_EMAIL_BASE";
    public static final String PARAMETRO_SISTEMA_ENVIAR_EMAIL_SOLICITANTE = "ENVIAR_EMAIL_SOLICITANTE";
    public static final String PARAMETRO_SISTEMA_ENVIAR_EMAIL_TERCEIRIZADA = "ENVIAR_EMAIL_TERCEIRIZADA";
    public static final String PARAMETRO_SISTEMA_UNIDADE_MATRICULA_GESTOR = "UNIDADE_MATRICULA_GESTOR";
    public static final String PARAMETRO_SISTEMA_UNIDADE_MATRICULA_ADM = "UNIDADE_MATRICULA_ADM";

    public static final String INTRANET_LOG4J = "sired.intranet.log.localizacao";
    public static final String INTRANET_URL_LDAP = "sired.intranet.ldap.url";
    public static final String INTRANET_PORTA_LDAP_GLOBAL = "configuracao.ldap.porta.global";
    public static final String INTRANET_IP_SERVIDOR_LDAP = "configuracao.ldap.ip.servidor";
    public static final String INTRANET_SECURITY_DOMAIN = "sired.intranet.security.domain";
    public static final String INTRANET_NOME_SISTEMA = "sired.intranet.grupo.sistema";

    public static final String EXTRANET_LOG4J = "sired.extranet.log.localizacao";
    public static final String EXTRANET_DIRETORIO_TEMP = "sired.extranet.temp";
    public static final String EXTRANET_DIRETORIO_ATENDIMENTOS = "sired.extranet.atendimentos";
    public static final String EXTRANET_DIRETORIO_ARQUIVOS_LOTES = "sired.extranet.arquivos_lotes";
    public static final String EXTRANET_DIRETORIO_DOCUMENTOS = "sired.extranet.documentos";
    public static final String EXTRANET_DIRETORIO_RELATORIOS = "sired.extranet.relatorios";

    public static final String EXTRANET_SESSAO_CONTRATOS = "EXTRANET_SESSAO_CONTRATOS";

    public static final String URL_LDAP = "sired.extranet.ldap.url";

    public static final String PORT_SERVIDOR_LDAP = "sired.extranet.ldap.port";

    public static final String USER_SERVIDOR_LDAP = "sired.extranet.ldap.user";

    public static final String APLICATION_SERVIDOR_LDAP = "sired.extranet.ldap.aplication";

    public static final String SERVICE_SERVIDOR_LDAP = "sired.extranet.ldap.user.service";

    public static final String PASSWORD_SERVIDOR_LDAP = "sired.extranet.ldap.password";

    public static final String SIGLA_SISTEMA = "RED";

    public static final String SIGLA_DEMANDA_REQ = "REQ";

    public static final String CONTENT_TYPE_PDF = "application/pdf";

    public static final String CONTENT_TYPE_ZIP = "application/x-zip-compressed";
    
    public static final String CONTENT_TYPE_APP_ZIP = "application/zip";
    
    public static final String CONTENT_TYPE_APP_OCTET = "application/octet-stream";
    
    public static final String CONTENT_TYPE_APP_XZIP = "application/x-zip";

    public static final String CONTENT_TYPE_TXT = "text/plain";

    public static final String CONTENT_TYPE_CSV = "application/vnd.ms-excel";

    public static final String RECEBER = "Receber";

    public static final String REAGENDAR = "Reagendar";

    public static final String CONFERIR = "Conferir";
    
    public static final String CONFIRMAR_ALTERACAO = "Confirmar Alteração";
    
    public static final String EM_DISPUTA = "Em Disputa";
    
    public static final String RECEBER_CONFIRMAR_FECHAR = "Receber, Confirmar e Fechar";
    
    public static final String DESFAZER_ALTERACAO = "Desfazer Alteração";
    
    public static final String INVALIDAR_REMESSA = "Invalidar Remessa";

    public static final String DEVOLVER_CORRECAO = "Devolver para Correção";

    public static final String INVALIDAR = "Invalidar";

    public static final String DIRETORIO_ARQUIVOS_LOTES = "sired.extranet.arquivos_lotes";

    public static final String PERFIL_GERAL_SG = "G";

    public static final String PERFIL_GERENTE_SG = "GER";
    
    public static final int NUM_CHARS_OBSERVACAO_TRAMITE = 200;
    
    public static final String AGRUPAMENTO_SITUACAO_REQUISICAO = "AGRUPAMENTO_SITUACAO_REQUISICAO";
    
    public static final String PEND_CAIXA = "PEND_CAIXA";
    
    public static final String PEND_ATENDIMENTO = "PEND_ATENDIMENTO";
    
    public static final String VERSAO_DOCUMENTO = "VERSÃO";
    
    public static final String OBSERVACAO = "OBSERVACAO";

    public static final String DEMANDA = "DEMANDA";

    public static final String FORMATO = "FORMATO";

    public static final String UNIDADE_GERADORA = "UNIDADE GERADORA";

    public static final String NUMERO_PROCESSO = "NÚMERO DO PROCESSO";

    public static final int MAX_CHARS_OBSERVACAO = 500;
    
    public static final int MAX_CHARS_NUMERO_PROCESSO = 30;
    
    public static final String ENCODING_ISO88591 = "ISO-8859-1";
    
    public static final String ENCODING_WINDOWS_1252 = "Windows-1252";
    
    public static final String LOCALE_PT = "pt";
    
    public static final String LOCALE_BR = "BR";
    
    public static final String NOME_CAMPO_OPERACAO = "NU_OPERACAO_A11";
    
    public static final String NOME_CAMPO_CPF = "NU_CPF";
    
    public static final String NOME_CAMPO_CNPJ = "NU_CNPJ";
    
    // LOCAL DO REPORT
    public static final String REPORT_BASE_DIR = "/reports/";
    public static final String REPORT_IMG_DIR = "/reports/images/";
    
}
