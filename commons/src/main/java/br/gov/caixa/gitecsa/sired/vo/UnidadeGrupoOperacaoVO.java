package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc03_unidade_grupo_operacao", schema = Constantes.SCHEMADB_NAME)
public class UnidadeGrupoOperacaoVO extends BaseEntity {

    private static final long serialVersionUID = 1194213217405473067L;

    @EmbeddedId
    private UnidadeGrupoOperacaoPK id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_unidade_grupo", insertable = false, updatable = false)
    private UnidadeGrupoVO unidadeGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_operacao_a11", columnDefinition = "bpchar", insertable = false, updatable = false)
    private OperacaoVO operacao;

    @Override
    public UnidadeGrupoOperacaoPK getId() {
        return this.id;
    }

    @Override
    public void setId(Object id) {
        this.id = (UnidadeGrupoOperacaoPK) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public UnidadeGrupoVO getUnidadeGrupo() {
        return unidadeGrupo;
    }

    public void setUnidadeGrupo(UnidadeGrupoVO unidadeGrupo) {
        this.unidadeGrupo = unidadeGrupo;
    }

    public OperacaoVO getOperacao() {
        return operacao;
    }

    public void setOperacao(OperacaoVO operacao) {
        this.operacao = operacao;
    }

}
