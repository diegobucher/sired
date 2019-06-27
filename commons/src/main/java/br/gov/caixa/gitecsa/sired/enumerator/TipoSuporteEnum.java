package br.gov.caixa.gitecsa.sired.enumerator;

import org.apache.commons.lang.StringUtils;

public enum TipoSuporteEnum {

    PAPEL(1L, "Papel"), 
    MICROFICHA(2L, "Microficha"), 
    MICROFILME(3L, "Microfilme"), 
    MIDIAOPTICA(4L, "Mídia Óptica"), 
    REPOSITORIO(5L, "Repositório");

    private Long valor;

    private String descricao;

    private TipoSuporteEnum(Long valor, String descricao) {
	this.descricao = descricao;
	this.valor = valor;
    }

    public String getDescricao() {
	return descricao;
    }

    public void setDescricao(String descricao) {
	this.descricao = descricao;
    }

    public Long getValor() {
	return valor;
    }

    public void setValor(Long valor) {
	this.valor = valor;
    }

    public Long recreateLong() {
	return valor;
    }

    public String getDescricaoByValor(String valor) {
	for (TipoSuporteEnum tipo : TipoSuporteEnum.values()) {
	    if (tipo.getValor().equals(valor)) {
		return tipo.getDescricao();
	    }
	}

	return StringUtils.EMPTY;
    }
}
