package br.gov.caixa.gitecsa.sired.enumerator;

import java.text.Collator;
import java.util.Locale;

import br.gov.caixa.gitecsa.sired.util.Constantes;



public enum TipoDemandaEnum  {
		
	AUDITORIA("AUDITORIA", "Auditoria"),
	BACEN("BACEN", "Bacen"),
	CONFORMIDADE("CONFORMIDADE", "Conformidade"),
	JURIDICO("JURIDICO", "Jurídico"),
	NORMAL("NORMAL", "Normal"),
	OUVIDORIA("OUVIDORIA", "Ouvidoria"),
	PROCON("PROCON", "Procon");
	
	private String id;
	private String descricao;
	
	private TipoDemandaEnum(String id, String descricao){
		this.descricao = descricao;
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
    
    public String recreateString() {  
        return id;  
    }
	
    public static TipoDemandaEnum fromString(final String descricao) {
        Collator c = Collator.getInstance(new Locale (Constantes.LOCALE_PT, Constantes.LOCALE_BR));
        c.setStrength(Collator.PRIMARY);
        for (TipoDemandaEnum tipoDemanda : TipoDemandaEnum.values()) {
            if (c.compare(tipoDemanda.descricao.toUpperCase(), descricao.toUpperCase()) == 0) {
                return tipoDemanda;
            }
        }
        
        throw new IllegalArgumentException(); 
    }
    
    public static String[] stringValues() {
        
        TipoDemandaEnum[] v = values();
        String[] s = new String[v.length];
        
        for (int i = 0; i < v.length; i++) {
            s[i] = v[i].id;
        }
        
        return s;
    }
}
