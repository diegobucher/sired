package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba11_operacao", schema = Constantes.SCHEMADB_NAME)
public class OperacaoVO extends BaseEntity implements Comparable<OperacaoVO> {

    private static final long serialVersionUID = -437671045383684504L;
    
    private static final String ORDER_BY_ID = "id";
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_operacao", columnDefinition = "bpchar")
    private String id;

    @Column(name = "de_operacao")
    private String deOperacao;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_ativo", columnDefinition = "int2")
    private SimNaoEnum icAtivo;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_operacao_conta_corrente", columnDefinition = "int2")
    private SimNaoEnum icOperacaoContaCorrente;
    
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

    public String getDeOperacao() {
        return deOperacao;
    }
    
    // TODO #RC_SIRED Avaliar necessidade
    public String getDescricaoCompleta() {
        try {
            return this.getId() + "-" + this.getDeOperacao();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }

    }

    public void setDeOperacao(String deOperacao) {
        this.deOperacao = deOperacao;
    }

    public SimNaoEnum getIcAtivo() {
        return icAtivo;
    }

    public void setIcAtivo(SimNaoEnum icAtivo) {
        this.icAtivo = icAtivo;
    }
    
    public SimNaoEnum getIcOperacaoContaCorrente() {
        return icOperacaoContaCorrente;
    }

    public void setIcOperacaoContaCorrente(SimNaoEnum icOperacaoContaCorrente) {
        this.icOperacaoContaCorrente = icOperacaoContaCorrente;
    }

    @Override
    public int compareTo(OperacaoVO o) {
        return this.id.compareToIgnoreCase(o.id);
    }
    
    @Override
    public String toString() {
        return String.format("%s[id=%s]", getClass().getSimpleName(),getId().toString());
    }

}
