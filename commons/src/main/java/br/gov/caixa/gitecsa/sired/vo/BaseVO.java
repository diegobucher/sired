package br.gov.caixa.gitecsa.sired.vo;

import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc08_base", schema = Constantes.SCHEMADB_NAME)
public class BaseVO extends BaseEntity implements Comparable<BaseVO> {
    private static final long serialVersionUID = -2241682820534906982L;

    private static final String ORDER_BY_NOME = "nome";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_base", columnDefinition = "serial")
    private Long id;

    @Column(name = "no_base")
    private String nome;

    @Column(name = "no_caixa_postal")
    private String caixaPostal;

    @Column(name = "ic_base_centralizadora", columnDefinition = "int2")
    private Integer icBaseCentralizadora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_unidade_a02", columnDefinition = "int2")
    private UnidadeVO unidade;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "redtbc09_base_lote_seq", schema = Constantes.SCHEMADB_NAME, joinColumns = { @JoinColumn(name = "nu_base_c08") }, inverseJoinColumns = { @JoinColumn(name = "nu_lote_sequencia_a14") })
    private List<LoteSequenciaVO> loteSequencia;

    @OneToMany(mappedBy = "base", fetch = FetchType.LAZY)
    private List<CTDAVO> ctdas;

    @OneToMany(mappedBy = "base", fetch = FetchType.LAZY)
    private List<EmpresaContratoVO> empresaContratos;

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
        return ORDER_BY_NOME;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome.toUpperCase();
    }

    public String getCaixaPostal() {
        return caixaPostal;
    }

    public void setCaixaPostal(String caixaPostal) {
        this.caixaPostal = caixaPostal;
    }

    public Integer getIcBaseCentralizadora() {
        return icBaseCentralizadora;
    }

    public void setIcBaseCentralizadora(Integer icBaseCentralizadora) {
        this.icBaseCentralizadora = icBaseCentralizadora;
    }

    public UnidadeVO getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeVO unidade) {
        this.unidade = unidade;
    }

    public List<LoteSequenciaVO> getLoteSequencia() {
        return loteSequencia;
    }

    public void setLoteSequencia(List<LoteSequenciaVO> loteSequencia) {
        this.loteSequencia = loteSequencia;
    }

    public List<CTDAVO> getCtdas() {
        return ctdas;
    }

    public void setCtdas(List<CTDAVO> ctdas) {
        this.ctdas = ctdas;
    }

    public List<EmpresaContratoVO> getEmpresaContratos() {
        return empresaContratos;
    }

    public void setEmpresaContratos(List<EmpresaContratoVO> empresaContratos) {
        this.empresaContratos = empresaContratos;
    }

    public String getLoteSequenciaAsString() {
        Collections.sort(loteSequencia);
        return StringUtils.join(loteSequencia, ",");
    }

    @Override
    public int compareTo(BaseVO o) {
        return this.nome.compareToIgnoreCase(o.nome);
    }
}
