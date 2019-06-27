package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.TipoRestricaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc04_ctda_uf_restricao", schema = Constantes.SCHEMADB_NAME)
public class CTDAUFRestricaoVO extends BaseEntity {

    private static final long serialVersionUID = 4442713516742976268L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_ctda_uf_restricao", columnDefinition = "serial")
    private Long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_comando", columnDefinition = "int2")
    private TipoRestricaoEnum tipoResticao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_ctda_uf_c11", columnDefinition = "int4")
    private CTDAUFVO ctdaUF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_unidade_a02", columnDefinition = "int2")
    private UnidadeVO unidade;

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

    public TipoRestricaoEnum getTipoResticao() {
        return tipoResticao;
    }

    public void setTipoResticao(TipoRestricaoEnum tipoResticao) {
        this.tipoResticao = tipoResticao;
    }

    public CTDAUFVO getCtdaUF() {
        return ctdaUF;
    }

    public void setCtdaUF(CTDAUFVO ctdaUF) {
        this.ctdaUF = ctdaUF;
    }

    public UnidadeVO getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeVO unidade) {
        this.unidade = unidade;
    }

}
