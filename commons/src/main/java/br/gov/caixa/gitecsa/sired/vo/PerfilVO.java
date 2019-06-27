package br.gov.caixa.gitecsa.sired.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.hibernate.LazyInitializationException;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbs05_perfil", schema = Constantes.SCHEMADB_NAME)
public class PerfilVO extends BaseEntity implements Comparable<PerfilVO> {

    private static final long serialVersionUID = 2064857479718901212L;

    private static final String ORDER_BY_DESCRICAO = "descricao";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_perfil", columnDefinition = "serial")
    private Long id;

    @Column(name = "no_perfil", length = 20)
    private String descricao;

    @Column(name = "sg_perfil", length = 6, columnDefinition = "bpchar")
    private String sigla;

    // 0 nao padrão - 1 para padrao
    @Column(name = "ic_padrao", columnDefinition = "int2", insertable = false, updatable = false)
    private Integer indicadorPadrao;

    @OneToMany(mappedBy = "perfil", fetch = FetchType.LAZY)
    private List<PerfilFuncionalidadeVO> perfilFuncionalidade;

    @Column(name = "no_transacoes_ldap", length = 160)
    private String transacoes;

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getColumnOrderBy() {
        return ORDER_BY_DESCRICAO;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public Integer getIndicadorPadrao() {
        return indicadorPadrao;
    }

    public void setIndicadorPadrao(Integer indicadorPadrao) {
        this.indicadorPadrao = indicadorPadrao;
    }

    public List<PerfilFuncionalidadeVO> getPerfilFuncionalidade() {
        return perfilFuncionalidade;
    }

    public void setPerfilFuncionalidade(List<PerfilFuncionalidadeVO> perfilFuncionalidade) {
        this.perfilFuncionalidade = perfilFuncionalidade;
    }

    public String getTransacoes() {
        return transacoes;
    }

    public void setTransacoes(String transacoes) {
        this.transacoes = transacoes;
    }

    public String getFuncionalidadeAsString() {
        String retonoStr = StringUtils.EMPTY;
        try {
            Collections.sort(perfilFuncionalidade);
            if (perfilFuncionalidade != null && perfilFuncionalidade.size() > 0) {
                for (PerfilFuncionalidadeVO objPerf : perfilFuncionalidade) {
                    retonoStr += !retonoStr.equalsIgnoreCase(StringUtils.EMPTY) ? ", " + objPerf.getFuncionalidade().getNomeFuncionalidadePaiFilha() : objPerf
                            .getFuncionalidade().getNomeFuncionalidadePaiFilha();
                }
            }
        } catch (LazyInitializationException e) {
            return StringUtils.EMPTY;
        }

        return retonoStr;
    }

    public List<FuncionalidadeVO> getFuncionalidades() {
        List<FuncionalidadeVO> funcionalidades = new ArrayList<FuncionalidadeVO>();
        for (PerfilFuncionalidadeVO objPerf : perfilFuncionalidade) {
            funcionalidades.add(objPerf.getFuncionalidade());
        }
        return funcionalidades;
    }

    @Override
    public int compareTo(PerfilVO o) {
        return this.descricao.compareTo(o.getDescricao());
    }

}
