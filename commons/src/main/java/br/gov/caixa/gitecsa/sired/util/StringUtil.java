package br.gov.caixa.gitecsa.sired.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class StringUtil {

    // CARACTERES
    public static final String CARACTER_BARRA = "/";
    public static final String CARACTER_UNDERLINE = "_";
    public static final String CARACTER_HIFEN = "-";
    public static final String CARACTER_PONTO = ".";
    public static final String CARACTER_VIRGULA = ",";
    public static final String CARACTER_VAZIO = "";
    public static final String CARACTER_ESPACO = " ";
    public static final String CARACTER_UM = "1";
    public static final String CARACTER_PARENTESE_ESQ = "(";
    public static final String CARACTER_PARENTESE_DIR = ")";
    public static final String CARACTER_COLCHETE_ESQ = "[";
    public static final String CARACTER_COLCHETE_DIR = "]";
    public static final String PATTERN_DD_MM_YYYY_HH_MM_SS = "dd/MM/yyyy HH:mm:ss";
    public static final String NUMBERS = "1234567890";

    public static String removeAccents(String str) {

        str = Normalizer.normalize(str, Normalizer.Form.NFD);

        str = str.replaceAll("[^\\p{ASCII}]", "");

        return str;

    }

    /**
     * Preenche a string com 'caracter' a esquerda.
     * 
     * @param texto
     * @param caracter
     * @param size
     * @return
     * 
     *         <code>br.gov.gitecsa.util.StringUtil#leftFill(String, char, int)</code>
     */
    public static String leftFill(String texto, char caracter, int size) {
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < (size - texto.length()); i++) {
            str.append(caracter);
        }
        str.append(texto);
        return str.toString();
    }

    public static String onlyNumber(String texto) {
        StringBuilder str = new StringBuilder(StringUtils.EMPTY);

        for (int i = 0; i < (texto.length()); i++) {
            CharSequence c = String.valueOf(texto.charAt(i));

            if (NUMBERS.contains(c)) {
                str.append(texto.charAt(i));
            }
        }

        return str.toString();
    }
    
    public static String difference(String str1, String str2) {
    	final String spaceRegExp = "\\s";
    	final String wordRegExp = "((%s\\s)|(\\s%s))"; 
    	
    	List<String> reference = Arrays.asList(str1.split(spaceRegExp));
    	List<String> pieces = Arrays.asList(str2.split(spaceRegExp));
    	
    	for (String s : pieces) {
    		if (reference.contains(s)) {
    			str2 = str2.replaceAll(String.format(wordRegExp, s, s), StringUtils.EMPTY);
    		}
    	}
    	
    	return str2.trim();
    }
}
