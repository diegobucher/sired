package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.SituacaoRequisicaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba05_situacao_requisicao", schema = Constantes.SCHEMADB_NAME)
public class SituacaoRequisicaoVO extends BaseEntity {

    private static final long serialVersionUID = 7354653757859052719L;

    @Id
    @Column(name = "nu_situacao_requisicao", columnDefinition = "int2")
    private Long id;

    @Column(name = "de_situacao_requisicao")
    private String descricao;

    @Column(name = "no_situacao_requisicao")
    private String nome;

    public SituacaoRequisicaoVO() {
    }

    public SituacaoRequisicaoVO(Long id) {
        this.id = id;
    }

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SituacaoRequisicaoEnum) {
            return this.getId().equals(((SituacaoRequisicaoEnum) other).getId());
        }
        return super.equals(other);
    }
}
