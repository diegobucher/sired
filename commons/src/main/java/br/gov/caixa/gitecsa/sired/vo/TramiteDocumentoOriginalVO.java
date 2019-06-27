package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbs09_trmte_doc_orgnl", schema = Constantes.SCHEMADB_NAME)
public class TramiteDocumentoOriginalVO extends BaseEntity {

    private static final long serialVersionUID = -5880224212890582362L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_trmte_doc_orgnl", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_doc_orgnl_c20", columnDefinition = "int4")
    private DocumentoOriginalVO documentoOriginal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_sit_doc_orig_a15")
    private SituacaoDocumentoOriginalVO situacaoDocOriginal;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_tramite", insertable = false, updatable = false)
    private Date dataHora;

    @Column(name = "co_usuario", length = 60, columnDefinition = "bpchar")
    private String codigoUsuario;

    @Column(name = "de_observacao", length = 100, columnDefinition = "bpchar")
    private String observacao;
    
    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public DocumentoOriginalVO getDocumentoOriginal() {
        return documentoOriginal;
    }

    public void setDocumentoOriginal(DocumentoOriginalVO documentoOriginal) {
        this.documentoOriginal = documentoOriginal;
    }

    public SituacaoDocumentoOriginalVO getSituacaoDocOriginal() {
        return situacaoDocOriginal;
    }

    public void setSituacaoDocOriginal(SituacaoDocumentoOriginalVO situacaoDocOriginal) {
        this.situacaoDocOriginal = situacaoDocOriginal;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public String getCodigoUsuario() {
        return codigoUsuario;
    }

    public void setCodigoUsuario(String codigoUsuario) {
        this.codigoUsuario = codigoUsuario;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

}
