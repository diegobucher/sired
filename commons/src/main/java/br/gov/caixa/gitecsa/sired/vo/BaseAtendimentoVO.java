package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc19_base_atendimento", schema = Constantes.SCHEMADB_NAME)
public class BaseAtendimentoVO extends BaseEntity {

    private static final long serialVersionUID = 4536662625847643355L;
    
    @Id
    @Column(name = "nu_documento_c01", columnDefinition = "int4")
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_documento_c01", insertable = false, updatable = false)
    private DocumentoVO documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_base_c08", columnDefinition = "int4")
    private BaseVO base;
    
    @Temporal(TemporalType.DATE)
    @Column(name = "dt_inicio_atendimento", nullable = false)
    private Date dtInicioAtendimento;
    
    @Column(name = "co_usro_ultma_altco", nullable = false, columnDefinition = "bpchar", length = 7)
    private String coUsuario;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dh_ultma_altco", columnDefinition = "date", insertable = false, updatable = false)
    private Date ultimaAlteracao;
    
    public BaseAtendimentoVO() {
        super();
    }
    
    public BaseAtendimentoVO(DocumentoVO documento) {
        this();
        this.id = documento.getId();
        this.documento = documento;
    }
    
    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    public BaseVO getBase() {
        return base;
    }

    public void setBase(BaseVO base) {
        this.base = base;
    }

    public DocumentoVO getDocumento() {
        return documento;
    }

    public void setDocumento(DocumentoVO documento) {
        this.documento = documento;
    }

    public Date getDtInicioAtendimento() {
        return dtInicioAtendimento;
    }

    public void setDtInicioAtendimento(Date dtInicioAtendimento) {
        this.dtInicioAtendimento = dtInicioAtendimento;
    }

    public String getCoUsuario() {
        return coUsuario;
    }

    public void setCoUsuario(String coUsuario) {
        this.coUsuario = coUsuario;
    }

    public Date getUltimaAlteracao() {
        return ultimaAlteracao;
    }

    public void setUltimaAlteracao(Date ultimaAlteracao) {
        this.ultimaAlteracao = ultimaAlteracao;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }
    
}
