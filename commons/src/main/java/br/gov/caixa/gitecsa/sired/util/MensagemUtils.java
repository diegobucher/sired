package br.gov.caixa.gitecsa.sired.util;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;

public final class MensagemUtils {

    private static Properties mensagens;

    /**
     * Construtor da classe MensagemUtil.
     */
    private MensagemUtils() {
    }

    /**
     * Recupera o arquivo de propriedade das mensagens do sistema.
     * 
     * @return
     */
    private static Properties getMensagens() {
        inicializaProperties();
        return mensagens;
    }

    /**
     * Inicializa o arquivo de propriedades. Método sincronizado.
     */
    private static synchronized void inicializaProperties() {
        if (mensagens == null || mensagens.isEmpty()) {
            mensagens = new Properties();
            try {
                InputStream arquivoProperties = Thread.currentThread().getContextClassLoader().getResourceAsStream("bundle.properties");

                mensagens.load(arquivoProperties);
            } catch (Exception e) {
                Logger logger = LogUtils.getLogger(MensagemUtils.class.getName());
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Obtêm a mensagem a partir de seu código no arquivo de properties.
     * 
     * @param codigoMensagem
     *            - Código da mensagem no arquivo de properties
     * @param parametros
     *            - Parâmetros a serem substituídos no texto da mensagem
     * @return Mensagem do arquivo de properties que representa o código passado como parâmetro.
     */
    public static String obterMensagem(String codigoMensagem, Object... parametros) {
        String mensagem = getMensagens().getProperty(codigoMensagem);
        if (mensagem != null) {
            return MessageFormat.format(mensagem, transformaListaMensagem(parametros));
        }

        return codigoMensagem;
    }

    /**
     * Obtêm a mensagem a partir de seu código no arquivo de properties.
     * 
     * @param codigoMensagem
     *            - Código da mensagem no arquivo de properties
     * @return Mensagem do arquivo de properties que representa o código passado como parâmetro.
     */
    public static String obterMensagem(String codigoMensagem) {
        return obterMensagem(codigoMensagem, new Object[] {});
    }

    private static Object[] transformaListaMensagem(Object[] parametros) {
        if (!ObjectUtils.isNullOrEmpty(parametros)) {
            Object[] novoArray = new Object[parametros.length];
            for (int i = 0; i < parametros.length; i++) {
                Object codigo = parametros[i];
                //String mensagem = getMensagens().getProperty((String) codigo);
                String mensagem = getMensagens().getProperty(codigo.toString());
                if (mensagem != null) {
                    novoArray[i] = mensagem;
                } else {
                    //novoArray[i] = (String) codigo;
                    novoArray[i] = codigo.toString();
                }
            }
            return novoArray;
        }
        return parametros;
    }

    public static void setKeepMessages(Boolean value) {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(value);
    }

}
