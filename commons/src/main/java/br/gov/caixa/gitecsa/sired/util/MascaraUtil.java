package br.gov.caixa.gitecsa.sired.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class MascaraUtil {

    public static final char DIGITO = '#';
    public static final char LETRA = '?';
    public static final char LETRA_CAIXA_ALTA = 'U';
    public static final char LETRA_CAIXA_BAIXA = 'L';
    public static final char ALFANUMERIFCO = 'A';
    public static final String PONTUACAO = "[\\(\\)/\\\\.,;:\\-\\+_\\s]";

    public static final String FORMAT_DD_MM_YYYY = Constantes.FORMATO_DATA;
    public static final String FORMAT_HH_MM = "HH:mm";
    public static final String FORMAT_DD_MM_YYYY_HH_MM = FORMAT_DD_MM_YYYY + StringUtil.CARACTER_ESPACO + StringUtil.CARACTER_HIFEN
            + StringUtil.CARACTER_ESPACO + FORMAT_HH_MM;
    public static final String FORMAT_MM_YYYY = "MM/yyyy";
    public static final String FORMAT_YYYY = "yyyy";
    public static final String PATTERN_MONETARIO = "¤ ###,###,##0.00";
    public static final DecimalFormatSymbols REAL_SIMBOLO = new DecimalFormatSymbols(getLocalePtBR());
    public static final DecimalFormat REAL_FORMATTER = new DecimalFormat(PATTERN_MONETARIO, REAL_SIMBOLO);

    private static final Log LOG = LogFactory.getLog(MascaraUtil.class);

    /**
     * Cria um objeto {@link MascaraUtil}.
     * 
     */
    private MascaraUtil() {
        super();
    }

    /**
     * Obtem uma instancia do Locale configurado para pt-BR
     * 
     * @return Retorna o Locale
     */
    private static Locale getLocalePtBR() {
        return new Locale("pt", "BR");
    }

    public static String removerMascaras(String numero) {
        String retorno = "";

        if (!Util.isNullOuVazio(numero)) {
            retorno = numero.replaceAll("\\p{Punct}", "").trim();
            retorno = retorno.replaceAll(" ", "").trim();
        }

        return retorno;

    }

    public static String mascararCpfCnpj(String cpfCnpj) {
        String retorno = "";
        if (cpfCnpj.length() == 11) {
            retorno = cpfCnpj.substring(0, 3) + StringUtil.CARACTER_PONTO + cpfCnpj.substring(3, 6) + StringUtil.CARACTER_PONTO + cpfCnpj.substring(6, 9)
                    + StringUtil.CARACTER_HIFEN + cpfCnpj.substring(9, 11);
        } else if (cpfCnpj.length() == 14) {
            retorno = cpfCnpj.substring(0, 2) + StringUtil.CARACTER_PONTO + cpfCnpj.substring(2, 5) + StringUtil.CARACTER_PONTO + cpfCnpj.substring(5, 8)
                    + StringUtil.CARACTER_BARRA + cpfCnpj.substring(8, 12) + StringUtil.CARACTER_HIFEN + cpfCnpj.substring(12, 14);
        } else {
            retorno = cpfCnpj;
        }

        return retorno;
    }

    public static String mascararCEP(String cep) {
        String retorno = "";
        if (cep.length() == 8) {
            retorno = cep.substring(0, 5) + StringUtil.CARACTER_HIFEN + cep.substring(5);
        }

        return retorno;
    }
 
    /**
     * 
     * Metodo para formatacao de strings
     * 
     * @param str
     *            String a ser formatada
     * @param mascara
     *            Mascara a ser utilizada
     * @return String formatada
     * @throws ParseException
     */
    public static String formatar(String str, String mascara) throws ParseException {
        if (Util.isNullOuVazio(str)) {
            return null;
        }
        /*
         * Caracteres de pontuacao da mascara (escapeados): ()/\.,;:-_ (espaco) Se for necessario, basta incluir outro caracter
         */

        StringBuffer resultado = new StringBuffer();

        String mascaraReduzida = mascara.replaceAll(MascaraUtil.PONTUACAO, "").substring(0, str.length());

        for (int i = 0; i < mascaraReduzida.length(); i++) {
            char m = mascaraReduzida.charAt(i);
            char s = str.charAt(i);

            if (m == MascaraUtil.DIGITO && Character.isDigit(s)) {
                resultado.append(s);
            } else if ((m == MascaraUtil.LETRA || m == MascaraUtil.LETRA_CAIXA_ALTA || m == MascaraUtil.LETRA_CAIXA_BAIXA) && Character.isLetter(s)) {
                if (m == MascaraUtil.LETRA_CAIXA_ALTA) {
                    resultado.append(Character.toUpperCase(s));
                } else if (m == MascaraUtil.LETRA_CAIXA_BAIXA) {
                    resultado.append(Character.toLowerCase(s));
                } else {
                    resultado.append(s);
                }
            } else if (m == MascaraUtil.ALFANUMERIFCO && Character.isLetterOrDigit(s)) {
                resultado.append(s);
            } else {
                throw new ParseException("Valor invalido para a posicao " + i + " da mascara.", i);
            }
        }

        for (int i = 0; i < resultado.length(); i++) {
            char m = mascara.charAt(i);
            if (Pattern.matches(MascaraUtil.PONTUACAO, Character.toString(m))) {
                resultado.insert(i, m);
            }
        }

        return resultado.toString();
    }

    /**
     * 
     * Converte uma string formatada para BigDecimal
     * 
     * @param valor
     *            Valor formatado
     * @return BigDecimal
     */
    public static BigDecimal converteStringToBigDecimal(String valor) {
        BigDecimal retorno;
        try {
            String valorSemFormatacao = valor.replace(StringUtil.CARACTER_PONTO, "").replace(StringUtil.CARACTER_VIRGULA, StringUtil.CARACTER_PONTO);
            retorno = new BigDecimal(valorSemFormatacao);
        } catch (NumberFormatException e) {
            retorno = null;
        } catch (NullPointerException e) {
            retorno = null;
        }
        return retorno;
    }

    /**
     * 
     * Converte um BigDecimal para uma String formatada
     * 
     * @param valor
     *            BigDecimal
     * @param formato
     *            padrao de formatacao
     * @return String formatada
     */
    public static String converteBigDecimalToString(BigDecimal valor, final String formato) {
        String retorno;
        try {
            DecimalFormat df = new DecimalFormat(formato);
            retorno = df.format(valor);
        } catch (NumberFormatException e) {
            retorno = null;
        } catch (NullPointerException e) {
            retorno = null;
        }
        return retorno;
    }

    /**
     * 
     * DOCUMENT ME!
     * 
     * @param valor
     * @return
     */
    public static String converteBigDecimalToStringReais(BigDecimal valor) {
        String retorno;
        String pattern = "R$ #,##0.00;";
        Locale locale = getLocalePtBR();
        NumberFormat numberFormatWithLocale = NumberFormat.getCurrencyInstance(locale);
        DecimalFormat decimalFormatWithLocale = (DecimalFormat) numberFormatWithLocale;
        decimalFormatWithLocale.applyPattern(pattern);
        try {
            retorno = decimalFormatWithLocale.format(valor);
        } catch (NumberFormatException e) {
            retorno = null;
        } catch (NullPointerException e) {
            retorno = null;
        }
        return retorno;
    }

    /**
     * 
     * Converte uma string formatada para BigDecimal
     * 
     * @param valor
     *            Valor formatado
     * @return BigDecimal
     */
    public static Double converteStringToDouble(String valor) {
        Double retorno;
        try {
            String valorSemFormatacao = valor.replace(StringUtil.CARACTER_PONTO, "").replace(StringUtil.CARACTER_VIRGULA, StringUtil.CARACTER_PONTO);
            BigDecimal bd2 = new BigDecimal(valorSemFormatacao, new MathContext(15, RoundingMode.FLOOR));
            retorno = bd2.doubleValue();
        } catch (NumberFormatException e) {
            retorno = null;
        } catch (NullPointerException e) {
            retorno = null;
        }
        return retorno;
    }

    public static String converteDoubleToString(Double valor) {
        String retorno;
        String pattern = "###,##0.00";
        Locale locale = getLocalePtBR();
        NumberFormat numberFormatWithLocale = NumberFormat.getCurrencyInstance(locale);
        DecimalFormat decimalFormatWithLocale = (DecimalFormat) numberFormatWithLocale;
        decimalFormatWithLocale.applyPattern(pattern);
        try {
            retorno = decimalFormatWithLocale.format(valor);
        } catch (NumberFormatException e) {
            retorno = null;
        } catch (NullPointerException e) {
            retorno = null;
        }

        return retorno;
    }

    public static String converteBigDecimalToString(BigDecimal valor) {
        String retorno;
        String pattern = "###,##0.00";
        Locale locale = getLocalePtBR();
        NumberFormat numberFormatWithLocale = NumberFormat.getCurrencyInstance(locale);
        DecimalFormat decimalFormatWithLocale = (DecimalFormat) numberFormatWithLocale;
        decimalFormatWithLocale.applyPattern(pattern);
        try {
            retorno = decimalFormatWithLocale.format(valor);
        } catch (NumberFormatException e) {
            retorno = null;
        } catch (NullPointerException e) {
            retorno = null;
        }

        return retorno;
    }

    public static String converteLongToString(Long valor) {
        if (!Util.isNullOuVazio(valor)) {
            NumberFormat f = NumberFormat.getNumberInstance();
            return f.format(valor);
        } else {
            return "";
        }
    }

    public static String dateToString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DD_MM_YYYY);
        return dateFormat.format(date);
    }

    public static Date dateToDateFormat(Date date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DD_MM_YYYY);
        return dateFormat.parse(dateFormat.format(date));
    }

    public static Date dateToTimeFormat(Date date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_HH_MM);
        return dateFormat.parse(dateFormat.format(date));
    }

    public static Date dateToDateTimeFormat(Date date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DD_MM_YYYY_HH_MM);
        return dateFormat.parse(dateFormat.format(date));
    }

    public static String dateToStringDDMMYYYY(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DD_MM_YYYY);
        return dateFormat.format(date);
    }

    public static String dateToStringDDMMYYYYHHMIN(Date date) {
        if (!Util.isNullOuVazio(date)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DD_MM_YYYY_HH_MM);
            return dateFormat.format(date);
        }
        return null;
    }

    public static String dateToStringHHMIN(Date date) {
        if (!Util.isNullOuVazio(date)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_HH_MM);
            return dateFormat.format(date);
        }
        return null;
    }

    public static String dateToStringDDMMYYYYHHMINh(Date date) {
        if (!Util.isNullOuVazio(date)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - HH:mm'h'");
            return dateFormat.format(date);
        }
        return null;
    }

    /**
     * Formata uma string colocando em upper case e substitui espaços em branco por %
     * 
     * @param data
     *            1
     * @return a nova string preparada para a consulta 'like'
     */
    public static String formataParaPesquisaLike(String oldString) {
        String newString = "%";

        if (!Util.isNullOuVazio(oldString)) {
            newString += (((oldString.trim()).replace(StringUtil.CARACTER_ESPACO, newString)).toUpperCase() + newString);
        }
        return newString;
    }

    public static String formataFloatEmPorcentagem(Float valor) {

        DecimalFormat fmt = new DecimalFormat("0.00");

        if (valor == null) {
            return fmt.format(0).replace(StringUtil.CARACTER_VIRGULA, StringUtil.CARACTER_PONTO);
        } else {
            return fmt.format(valor).replace(StringUtil.CARACTER_VIRGULA, StringUtil.CARACTER_PONTO);
        }

    }

    public static Float formataStringEmPorcentagem(String valor) {
        Float retorno = 0F;

        if (!Util.isNullOuVazio(valor)) {
            if (valor.contains(StringUtil.CARACTER_VIRGULA)) {
                retorno = new Float(valor.replace(StringUtil.CARACTER_PONTO, "").replace(StringUtil.CARACTER_VIRGULA, StringUtil.CARACTER_PONTO));
            } else {
                retorno = new Float(valor);
            }
        }

        return retorno;
    }

    public static String converteListaToString(Collection<?> lista) {
        if (Util.isNullOuVazio(lista)) {
            return "";
        }
        return lista.toString().replace("[", "").replace("]", "");

    }

    public static String cepFormatado(String cep) {
        String codigoFormatado = null;

        if (!Util.isNullOuVazio(cep)) {
            if (cep.length() <= 8) {
                String cepAux = StringUtil.leftFill(cep, '0', 8);
                Pattern pattern = Pattern.compile("(\\d{5})(\\d{3})");
                Matcher matcher = pattern.matcher(cepAux);
                codigoFormatado = matcher.replaceAll("$1-$2");
            } else {
                codigoFormatado = cep;
            }
        }

        return codigoFormatado;
    }

    public static Date stringToDate(String string) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_MM_YYYY);
        Date data = null;
        try {
            data = dateFormat.parse(string);
        } catch (ParseException e) {
            LOG.debug(e.getMessage());
        } catch (NullPointerException e) {
            LOG.debug(e.getMessage());
        }
        return data;
    }

    public static Date stringToDateDDMMYYYY(String string) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DD_MM_YYYY);
        Date data = null;
        try {
            data = dateFormat.parse(string);
        } catch (Exception e) {
            LOG.debug(e.getMessage());
        }
        return data;
    }

    public static Date stringToDateDDMMYYYYHHMIN(String string) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(FORMAT_DD_MM_YYYY_HH_MM);
        Date data = null;
        try {
            data = dateFormat.parse(string);
        } catch (Exception e) {
            LOG.debug(e.getMessage());
        }
        return data;
    }

    /**
     * Método responsável por formatar um valor para monetário
     * 
     * @param valor
     *            - valor que será formatado
     * @param moeda
     *            - DecimalFormat que será utilizada para conversão (caso seja nulo sera utilizado o MascaraUtil.REAL_FORMATTER como
     *            default)
     */
    public static String mascaraMonetaria(double valor, DecimalFormat moeda) {
        return ((moeda != null) ? moeda : REAL_FORMATTER).format(valor);
    }

    public static boolean isCnpjValido(Long cnpjEmpresa) {
        boolean retorno = false;
        String cnpjAux = null;
        String cnpj = cnpjEmpresa.toString();

        if (!Util.isNullOuVazio(cnpj.substring(0, 1))) {

            if (cnpj.equals("00000000000000") || cnpj.equals("11111111111111") || cnpj.equals("22222222222222") || cnpj.equals("33333333333333")
                    || cnpj.equals("44444444444444") || cnpj.equals("55555555555555") || cnpj.equals("66666666666666") || cnpj.equals("77777777777777")
                    || cnpj.equals("88888888888888") || cnpj.equals("99999999999999") || cnpj.length() != 14) {

                return false;
            }

            try {
                cnpjAux = cnpj.replace('.', ' ').replace('/', ' ').replace('-', ' ').replaceAll(StringUtil.CARACTER_ESPACO, "");

                int soma = 0;
                int dig = 0;

                String cnpjCalc = cnpjAux.substring(0, 12);

                if (cnpjAux.length() == 14) {
                    char[] chrCnpj = cnpjAux.toCharArray();
                    /* Primeira parte */
                    for (int i = 0; i < 4; i++) {
                        if (chrCnpj[i] - 48 >= 0 && chrCnpj[i] - 48 <= 9) {
                            soma += (chrCnpj[i] - 48) * (6 - (i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if (chrCnpj[i + 4] - 48 >= 0 && chrCnpj[i + 4] - 48 <= 9) {
                            soma += (chrCnpj[i + 4] - 48) * (10 - (i + 1));
                        }
                    }
                    dig = 11 - (soma % 11);
                    cnpjCalc += (dig == 10 || dig == 11) ? Integer.toString(0) : Integer.toString(dig);
                    /* Segunda parte */
                    soma = 0;
                    for (int i = 0; i < 5; i++) {
                        if (chrCnpj[i] - 48 >= 0 && chrCnpj[i] - 48 <= 9) {
                            soma += (chrCnpj[i] - 48) * (7 - (i + 1));
                        }
                    }
                    for (int i = 0; i < 8; i++) {
                        if (chrCnpj[i + 5] - 48 >= 0 && chrCnpj[i + 5] - 48 <= 9) {
                            soma += (chrCnpj[i + 5] - 48) * (10 - (i + 1));
                        }
                    }
                    dig = 11 - (soma % 11);
                    cnpjCalc += (dig == 10 || dig == 11) ? Integer.toString(0) : Integer.toString(dig);
                    retorno = cnpjAux.equals(cnpjCalc);
                }
            } catch (Exception e) {
                retorno = false;
            }
        }
        return retorno;
    }

    public static boolean isCpfValido(String cpfComPontos) {
        if (!Util.isNullOuVazio(cpfComPontos)) {

            String cpf = cpfComPontos.replace(StringUtil.CARACTER_PONTO, "").replace(StringUtil.CARACTER_HIFEN, "").replace(StringUtil.CARACTER_ESPACO, "");
            if (cpf.length() == 11) {

                int[] digitos = new int[2];

                for (int multiplicador = 10; multiplicador <= 11; multiplicador++) {
                    int soma = 0;
                    for (int i = 0; i < (multiplicador - 1); i++) {
                        soma += Integer.parseInt(cpf.substring(i, i + 1)) * (multiplicador - i);
                    }
                    int resto = (soma % 11);
                    if (resto < 2) {
                        digitos[multiplicador - 10] = 0;
                    } else {
                        digitos[multiplicador - 10] = 11 - resto;
                    }
                }
                return (digitos[0] == Integer.parseInt(cpf.substring(9, 10)) && digitos[1] == Integer.parseInt(cpf.substring(10, 11)));
            }
        }
        return false;
    }

}
