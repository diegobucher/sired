package br.gov.caixa.gitecsa.sired.dto;

import org.apache.commons.csv.CSVRecord;

import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;

public class RequisicaoLoteDTO {
    
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequisicaoLoteDTO other = (RequisicaoLoteDTO) obj;
		return this.requisicao.getRequisicaoDocumento().equals(other.requisicao.getRequisicaoDocumento());
	}

	private RequisicaoVO requisicao;
    
    private CSVRecord registro;

    public RequisicaoVO getRequisicao() {
        return requisicao;
    }

    public void setRequisicao(RequisicaoVO requisicao) {
        this.requisicao = requisicao;
    }

    public CSVRecord getRegistro() {
        return registro;
    }

    public void setRegistro(CSVRecord registro) {
        this.registro = registro;
    }
}
