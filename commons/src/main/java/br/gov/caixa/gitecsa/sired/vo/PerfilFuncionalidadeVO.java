package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbs07_perfil_funcionalidade", schema = Constantes.SCHEMADB_NAME)
public class PerfilFuncionalidadeVO extends BaseEntity implements Comparable<PerfilFuncionalidadeVO> {

    private static final long serialVersionUID = -1320904376170710201L;

    public final static Integer PERFIL_APENAS_CONSULTA = 1;
    public final static Integer PERFIL_CADASTRO = 0;

    @EmbeddedId
    private PerfilFuncionalidadePK id;

    @Column(name = "ic_somente_consulta", columnDefinition = "int2", insertable = false, updatable = false)
    private Integer icConsultaCadastro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_perfil_s05", insertable = false, updatable = false)
    private PerfilVO perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_funcionalidade_s06", insertable = false, updatable = false)
    private FuncionalidadeVO funcionalidade;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (PerfilFuncionalidadePK) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public Integer getIcConsultaCadastro() {
        return icConsultaCadastro;
    }

    public void setIcConsultaCadastro(Integer icConsultaCadastro) {
        this.icConsultaCadastro = icConsultaCadastro;
    }

    public PerfilVO getPerfil() {
        return perfil;
    }

    public void setPerfil(PerfilVO perfil) {
        this.perfil = perfil;
    }

    public FuncionalidadeVO getFuncionalidade() {
        return funcionalidade;
    }

    public void setFuncionalidade(FuncionalidadeVO funcionalidade) {
        this.funcionalidade = funcionalidade;
    }

    @Override
    public int compareTo(PerfilFuncionalidadeVO o) {
        return this.getFuncionalidade().getNomeFuncionalidadePaiFilha().compareToIgnoreCase(o.getFuncionalidade().getNomeFuncionalidadePaiFilha());
    }

}
