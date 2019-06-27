package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba09_suporte", schema = Constantes.SCHEMADB_NAME)
public class SuporteVO extends BaseEntity {

    private static final long serialVersionUID = 3805563598749509707L;
    
    private static final String ORDER_BY_NOME = "nome";

    @Id
    @Column(name = "nu_suporte", columnDefinition = "serial")
    private Long id;

    @Column(name = "no_suporte")
    private String nome;

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
        this.nome = nome;
    }

}
