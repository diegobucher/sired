package br.gov.caixa.gitecsa.sired.vo;

import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba07_feriado", schema = Constantes.SCHEMADB_NAME)
public class FeriadoVO extends BaseEntity {

    private static final long serialVersionUID = -847526211527386149L;

    @Id
    @Column(name = "nu_feriado", columnDefinition = "int4")
    private Long id;

    @Temporal(TemporalType.DATE)
    @Column(name = "dt_feriado")
    private Date data;

    @Column(name = "ic_modalidade", columnDefinition = "int2")
    private Integer modalidade;

    @Column(name = "ic_origem", columnDefinition = "bpchar")
    private String origem;

    @Column(name = "no_feriado")
    private String nome;

    @Column(name = "ts_inclusao")
    private Timestamp tsInclusao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sg_uf_a03")
    private UFVO uf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_municipio_a08", columnDefinition = "int2")
    private MunicipioVO municipio;

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

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public Integer getModalidade() {
        return modalidade;
    }

    public void setModalidade(Integer modalidade) {
        this.modalidade = modalidade;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Timestamp getTsInclusao() {
        return tsInclusao;
    }

    public void setTsInclusao(Timestamp tsInclusao) {
        this.tsInclusao = tsInclusao;
    }

    public UFVO getUf() {
        return uf;
    }

    public void setUf(UFVO uf) {
        this.uf = uf;
    }

    public MunicipioVO getMunicipio() {
        return municipio;
    }

    public void setMunicipio(MunicipioVO municipio) {
        this.municipio = municipio;
    }

    public boolean equalsViewSiico(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FeriadoVO other = (FeriadoVO) obj;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (modalidade == null) {
            if (other.modalidade != null)
                return false;
        } else if (!modalidade.equals(other.modalidade))
            return false;
        if (municipio == null) {
            if (other.municipio != null)
                return false;
        } else if (other.municipio == null) {
            return false;
        } else if (!municipio.getId().equals(other.municipio.getId()))
            return false;
        if (origem == null) {
            if (other.origem != null)
                return false;
        } else if (other.origem == null) {
            return false;
        } else if (!origem.trim().equals(other.origem.trim()))
            return false;
        if (tsInclusao == null) {
            if (other.tsInclusao != null)
                return false;
        } else if (!tsInclusao.equals(other.tsInclusao))
            return false;
        if (uf == null) {
            if (other.uf != null)
                return false;
        } else if (other.uf == null) {
            return false;
        } else if (!uf.getId().equals(other.uf.getId()))
            return false;
        return true;
    }
}
