package br.gov.caixa.gitecsa.sired.enumerator;

import java.text.Collator;
import java.util.Locale;

import br.gov.caixa.gitecsa.sired.util.Constantes;

public enum FormatoDocumentoEnum {

    COPIA_SIMPLES("0", "Cópia Simples"), 
    COPIA_AUTENTICADA("1", "Cópia Autenticada"), 
    ORIGINAL("2", "Original");

    private String valor;
    private String descricao;

    private FormatoDocumentoEnum(String valor, String descricao) {
        this.descricao = descricao;
        this.valor = valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
    
    public static FormatoDocumentoEnum fromString(final String descricao) {
        Collator c = Collator.getInstance(new Locale (Constantes.LOCALE_PT, Constantes.LOCALE_BR));
        c.setStrength(Collator.PRIMARY);
        for (FormatoDocumentoEnum formatoDocumento : FormatoDocumentoEnum.values()) {
            if (c.compare(formatoDocumento.descricao.toUpperCase(), descricao.toUpperCase()) == 0) {
                return formatoDocumento;
            }
        }
        
        throw new IllegalArgumentException(); 
    }
    
    public static String[] stringValues() {
        
        FormatoDocumentoEnum[] v = values();
        String[] s = new String[v.length];
        
        for (int i = 0; i < v.length; i++) {
            s[i] = v[i].descricao;
        }
        
        return s;
    }

}
