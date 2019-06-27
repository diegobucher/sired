package br.gov.caixa.gitecsa.sired.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class RequisicaoDocumentoPK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "nu_requisicao_c14")
    private Integer idRequisicao;

    @Column(name = "nu_requisicao_documento")
    private Integer idRequisicaoDocumento;

    public Integer getIdRequisicao() {
        return idRequisicao;
    }

    public void setIdRequisicao(Integer idRequisicao) {
        this.idRequisicao = idRequisicao;
    }

    public Integer getIdRequisicaoDocumento() {
        return idRequisicaoDocumento;
    }

    public void setIdRequisicaoDocumento(Integer idRequisicaoDocumento) {
        this.idRequisicaoDocumento = idRequisicaoDocumento;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RequisicaoDocumentoPK)) {
            return false;
        }
        RequisicaoDocumentoPK castOther = (RequisicaoDocumentoPK) other;
        return this.idRequisicao.equals(castOther.idRequisicao) && this.idRequisicaoDocumento.equals(castOther.idRequisicaoDocumento);
    }

    public int hashCode() {
        final int prime = 31;
        int hash = 17;
        hash = hash * prime + this.idRequisicao.hashCode();
        hash = hash * prime + this.idRequisicaoDocumento.hashCode();

        return hash;
    }
}
