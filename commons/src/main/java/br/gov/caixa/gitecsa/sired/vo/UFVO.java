package br.gov.caixa.gitecsa.sired.vo;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba03_uf", schema = Constantes.SCHEMADB_NAME)
public class UFVO extends BaseEntity implements Comparable<UFVO> {
    private static final long serialVersionUID = -4301666237640290852L;

    private static final String ORDER_BY_ID = "id";

    @Id
    @Column(name = "sg_uf", columnDefinition = "bpchar")
    private String id;

    @Column(name = "no_uf")
    private String nome;

    @OneToMany(mappedBy = "uf", fetch = FetchType.LAZY)
    private List<FeriadoVO> feriados;

    @OneToMany(mappedBy = "uf", fetch = FetchType.LAZY)
    private List<MunicipioVO> municipios;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "redtbc07_unidade_uf", schema = Constantes.SCHEMADB_NAME, joinColumns = { @JoinColumn(name = "sg_uf_a03") }, inverseJoinColumns = { @JoinColumn(name = "nu_unidade_a02") })
    private List<UnidadeVO> unidades;

    @OneToMany(mappedBy = "uf", fetch = FetchType.LAZY)
    private List<CTDAUFVO> ctdaUfs;

    @Transient
    private Boolean possuiRestricaoCTDA;

    @Transient
    private List<UnidadeVO> unidadeRestricaoExclucao;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (String) id;
    }

    @Override
    public String getColumnOrderBy() {
        return ORDER_BY_ID;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public List<FeriadoVO> getFeriados() {
        return feriados;
    }

    public void setFeriados(List<FeriadoVO> feriados) {
        this.feriados = feriados;
    }

    public List<MunicipioVO> getMunicipios() {
        return municipios;
    }

    public void setMunicipios(List<MunicipioVO> municipios) {
        this.municipios = municipios;
    }

    public List<UnidadeVO> getUnidades() {
        return unidades;
    }

    public void setUnidades(List<UnidadeVO> unidades) {
        this.unidades = unidades;
    }

    public List<CTDAUFVO> getCtdaUfs() {
        return ctdaUfs;
    }

    public void setCtdaUfs(List<CTDAUFVO> ctdaUfs) {
        this.ctdaUfs = ctdaUfs;
    }

    public Boolean getPossuiRestricaoCTDA() {
        return possuiRestricaoCTDA;
    }

    public void setPossuiRestricaoCTDA(Boolean possuiRestricaoCTDA) {
        this.possuiRestricaoCTDA = possuiRestricaoCTDA;
    }

    public List<UnidadeVO> getUnidadeRestricaoExclucao() {
        return unidadeRestricaoExclucao;
    }

    public void setUnidadeRestricaoExclucao(List<UnidadeVO> unidadeRestricaoExclucao) {
        this.unidadeRestricaoExclucao = unidadeRestricaoExclucao;
    }

    @Override
    public int compareTo(UFVO o) {
        return this.getNome().compareToIgnoreCase(o.getNome());
    }

    @Override
    public String toString() {
        return this.getDescricao();
    }

    public String getDescricao() {
        return this.nome + " (" + this.id + ")";
    }

}
