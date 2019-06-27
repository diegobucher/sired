package br.gov.caixa.gitecsa.sired.util;

import java.text.Normalizer;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.vo.CampoVO;

public class SiredUtils {

    public static final int NUM_CHARS_CPF_SEM_DV = 9;
    public static final int NUM_CHARS_CPF = 11;
    public static final int NUM_CHARS_CNPJ = 14;
    public static final int NUM_CHARS_CGC = 4;
    public static final int NUM_CHARS_AGENCIA = 8;
    public static final int NUM_CHARS_PAB = 9;
	public static final int NUM_CHARS_PA = 56;
    public static final int NUM_SUPERINTENDENCIA = 42;

    public static final String ABREV_SUPERINTENDENCIA = "SR";
    public static final String ABREV_AGENCIA = "A";
    public static final String ABREV_PAB = "B";
	public static final String ABREV_PA = "B";
    public static final String ABREV_PONTO_VENDA = "PV";

    public static final String DOMINIO_EMAIL = "@mail.caixa";

    private static final String CAMPO_OPERACAO = "NU_OPERACAO_A11";

    public static boolean isDigitoVerificadorContaCaixa(final String numeroConta, final String digitoVerificador) {

        int peso = 8, soma = 0;

        for (char c : numeroConta.toCharArray()) {
            int n = Integer.parseInt(String.valueOf(c));
            soma += n * peso;
            peso--;
            if (peso < 2) {
                peso = 9;
            }
        }

        int modulo = (soma * 10) % 11;

        if (modulo == 10) {
            modulo = 0;
        }

        return (modulo == Integer.parseInt(digitoVerificador)) ? Boolean.TRUE : Boolean.FALSE;
    }

    public static boolean isCpfValido(String cpf) {
        if (cpf == null || cpf.length() != NUM_CHARS_CPF || isCpfPadrao(cpf))
            return false;

        try {
            Long.parseLong(cpf);
        } catch (NumberFormatException e) {
            return false;
        }

        if (!calcularDigitoVerificadorCpf(cpf.substring(0, NUM_CHARS_CPF_SEM_DV)).equals(cpf.substring(NUM_CHARS_CPF_SEM_DV, NUM_CHARS_CPF)))
            return false;

        return true;
    }

    public static Boolean isCpfPadrao(String cpf) {
        if (cpf.equals("00000000000") || cpf.equals("11111111111") || cpf.equals("22222222222") || cpf.equals("33333333333") || cpf.equals("44444444444")
                || cpf.equals("55555555555") || cpf.equals("66666666666") || cpf.equals("77777777777") || cpf.equals("88888888888")
                || cpf.equals("99999999999")) {
            return true;
        }

        return false;
    }

    public static String calcularDigitoVerificadorCpf(String numero) {

        Integer primDig, segDig;

        int soma = 0, peso = 10;
        for (int i = 0; i < numero.length(); i++)
            soma += Integer.parseInt(numero.substring(i, i + 1)) * peso--;

        if (soma % 11 == 0 | soma % 11 == 1)
            primDig = new Integer(0);
        else
            primDig = new Integer(11 - (soma % 11));

        soma = 0;
        peso = 11;
        for (int i = 0; i < numero.length(); i++)
            soma += Integer.parseInt(numero.substring(i, i + 1)) * peso--;

        soma += primDig.intValue() * 2;
        if (soma % 11 == 0 | soma % 11 == 1)
            segDig = new Integer(0);
        else
            segDig = new Integer(11 - (soma % 11));

        return primDig.toString() + segDig.toString();
    }

    public static Boolean isCnpjValido(String cnpj) {

        if (cnpj == null || cnpj.length() != NUM_CHARS_CNPJ) {
            return false;
        }

        try {
            Long.parseLong(cnpj);
        } catch (NumberFormatException e) {
            return false;
        }

        int soma = 0;
        String cnpj_calc = cnpj.substring(0, 12);

        char chr_cnpj[] = cnpj.toCharArray();

        for (int i = 0; i < 4; i++)
            if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9)
                soma += (chr_cnpj[i] - 48) * (6 - (i + 1));

        for (int i = 0; i < 8; i++)
            if (chr_cnpj[i + 4] - 48 >= 0 && chr_cnpj[i + 4] - 48 <= 9)
                soma += (chr_cnpj[i + 4] - 48) * (10 - (i + 1));

        int dig = 11 - soma % 11;
        cnpj_calc = (new StringBuilder(String.valueOf(cnpj_calc))).append(dig != 10 && dig != 11 ? Integer.toString(dig) : "0").toString();
        soma = 0;

        for (int i = 0; i < 5; i++)
            if (chr_cnpj[i] - 48 >= 0 && chr_cnpj[i] - 48 <= 9)
                soma += (chr_cnpj[i] - 48) * (7 - (i + 1));

        for (int i = 0; i < 8; i++)
            if (chr_cnpj[i + 5] - 48 >= 0 && chr_cnpj[i + 5] - 48 <= 9)
                soma += (chr_cnpj[i + 5] - 48) * (10 - (i + 1));

        dig = 11 - soma % 11;
        cnpj_calc = (new StringBuilder(String.valueOf(cnpj_calc))).append(dig != 10 && dig != 11 ? Integer.toString(dig) : "0").toString();

        if (!cnpj.equals(cnpj_calc) || cnpj.equals("00000000000000")) {
            return false;
        }

        return true;
    }

    public static String formatarCpf(String valor) {
        String resultado = StringUtils.leftPad(valor, NUM_CHARS_CPF, '0');

        if (!ObjectUtils.isNullOrEmpty(resultado)) {
            resultado = resultado.substring(0, 3) + "." + resultado.substring(3, 6) + "." + resultado.substring(6, 9) + "-" + resultado.substring(9, 11);
        }

        return resultado;
    }

    public static String formatarCnpj(String valor) {
        String resultado = StringUtils.leftPad(valor, NUM_CHARS_CNPJ, '0');

        if (!ObjectUtils.isNullOrEmpty(resultado)) {
            resultado = resultado.substring(0, 2) + "." + resultado.substring(2, 5) + "." + resultado.substring(5, 8) + "/" + resultado.substring(8, 12) + "-"
                    + resultado.substring(12, 14);
        }

        return resultado;
    }

    /**
     * Verifica se o campo é do tipo InputMask tags: #requisicao #form #campos #dinamicos
     * 
     * @param campo
     * @return <b>True</b> se o campo é do tipo InputMask e <b>False</b> caso contrário
     */
    public static Boolean isCampoInputMask(CampoVO campo) {
        if ((campo.getTipo().equals(TipoCampoEnum.NUMERICO) || campo.getTipo().equals(TipoCampoEnum.TEXTO)) && StringUtils.isNotBlank(campo.getMascara())
                && !campo.getMascara().contains("0,00") && !campo.getNome().equals(CAMPO_OPERACAO)) {
            return true;
        }

        return false;
    }

    public static String removeCaracteresInvalidos(Object texto) {
        if (ObjectUtils.isNullOrEmpty(texto)) {
            return null;
        }
        String novoTexto = texto.toString();
        novoTexto = novoTexto.replace(":", "-");
        novoTexto = novoTexto.replace(";", ".");
        novoTexto = removeAccents(novoTexto.trim());

        return novoTexto;
    }

    public static String removeAccents(String str) {

        str = Normalizer.normalize(str, Normalizer.Form.NFD);

        str = str.replaceAll("[^\\p{ASCII}]", "");

        return str;

    }
}
