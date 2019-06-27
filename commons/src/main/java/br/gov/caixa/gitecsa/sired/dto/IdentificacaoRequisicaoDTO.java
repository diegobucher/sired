package br.gov.caixa.gitecsa.sired.dto;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

public class IdentificacaoRequisicaoDTO implements Serializable {

    private static final long serialVersionUID = -7354307610063641707L;

    public static final int NUM_CARACTERES_SEQUENCIAL = 5; 

    private Integer idUnidade;
    
    private Integer ano;
    
    private Long id;

    public IdentificacaoRequisicaoDTO(Integer IdUnidade, Integer ano, Long id) {
	this.idUnidade = IdUnidade;
	
	this.ano = ano;
	
	this.id = id;
    }

    public IdentificacaoRequisicaoDTO(String numeroRequisicao) {
	this.idUnidade = Integer.valueOf(numeroRequisicao.substring(0, 4));
	
	this.ano = Integer.valueOf(numeroRequisicao.substring(4, 6));
	
	this.id = Long.valueOf(numeroRequisicao.substring(6, 11));
    }

    public Integer getIdUnidade() {
        return idUnidade;
    }

    public void setIdUnidade(Integer idUnidade) {
        this.idUnidade = idUnidade;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return String.format("%s%s%s", idUnidade, ano,
                StringUtils.leftPad(id.toString(), NUM_CARACTERES_SEQUENCIAL, "0"));
    }
    
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((ano == null) ? 0 : ano.hashCode());
	result = prime * result + ((idUnidade == null) ? 0 : idUnidade.hashCode());
	result = prime * result + ((id == null) ? 0 : id.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	
	if (obj == null) {
	    return false;
	}
	
	if (getClass() != obj.getClass()) {
	    return false;
	}
	
	IdentificacaoRequisicaoDTO other = (IdentificacaoRequisicaoDTO) obj;
	
	if (ano == null) {
	    if (other.ano != null) {
		return false;
	    }
	} else if (!ano.equals(other.ano)) {
	    return false;
	}
	
	if (idUnidade == null) {
	    if (other.idUnidade != null) {
		return false;
	    }
	} else if (!idUnidade.equals(other.idUnidade)) {
	    return false;
	}
	
	if (id == null) {
	    if (other.id != null) {
		return false;
	    }
	} else if (!id.equals(other.id)) {
	    return false;
	}
	
	return true;
    }
    
}
