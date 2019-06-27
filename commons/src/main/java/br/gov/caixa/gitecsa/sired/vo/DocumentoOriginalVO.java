package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc20_doc_orgnl", schema = Constantes.SCHEMADB_NAME)
public class DocumentoOriginalVO extends BaseEntity {

    private static final long serialVersionUID = 5515619944083242083L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_doc_orgnl", columnDefinition = "serial")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_requisicao_c14", columnDefinition = "int4")
    private RequisicaoVO requisicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_base_c08", columnDefinition = "int4")
    private BaseVO base;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_empresa_contrato_c13", columnDefinition = "int4")
    private EmpresaContratoVO empresaContrato;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_envio_unidade", insertable = false, updatable = false)
    private Date dataHora;

    @OneToMany(mappedBy = "documentoOriginal", fetch = FetchType.LAZY)
    private Set<TramiteDocumentoOriginalVO> tramitesDocsOriginais;
    
    @OneToOne
    @JoinColumn(name = "nu_trmte_doc_orgnl_atual_s09", columnDefinition = "int4")
    private TramiteDocumentoOriginalVO tramiteDocOriginalAtual;

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

    public RequisicaoVO getRequisicao() {
        return requisicao;
    }

    public void setRequisicao(RequisicaoVO requisicao) {
        this.requisicao = requisicao;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public EmpresaContratoVO getEmpresaContrato() {
        return empresaContrato;
    }

    public void setEmpresaContrato(EmpresaContratoVO empresaContrato) {
        this.empresaContrato = empresaContrato;
    }

    public Date getDataHora() {
        return dataHora;
    }

    public void setDataHora(Date dataHora) {
        this.dataHora = dataHora;
    }

    public Set<TramiteDocumentoOriginalVO> getTramitesDocsOriginais() {
        return tramitesDocsOriginais;
    }

    public void setTramitesDocsOriginais(Set<TramiteDocumentoOriginalVO> tramitesDocsOriginais) {
        this.tramitesDocsOriginais = tramitesDocsOriginais;
    }

    public TramiteDocumentoOriginalVO getTramiteDocOriginalAtual() {
        return tramiteDocOriginalAtual;
    }

    public void setTramiteDocOriginalAtual(TramiteDocumentoOriginalVO tramiteDocOriginalAtual) {
        this.tramiteDocOriginalAtual = tramiteDocOriginalAtual;
    }

}
