package br.gov.caixa.gitecsa.sired.vo;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redvw004_relatorio_geral_requisicao", schema = Constantes.SCHEMADB_NAME)
public class ViewRelatorioGeralRequisicaoVO extends BaseEntity {

    private static final long serialVersionUID = -7540358834951817837L;
    
    private static final String ORDER_BY_BASE = "noBase";
    
    @Id
    @Column(name = "nu_tramite_requisicao", columnDefinition = "int4")
    private Long id;
    
    @Column(name = "nu_base", columnDefinition = "int4")
    private Long nuBase;
        
    @Column(name = "no_base")
    private String noBase;
    
    @Column(name = "ic_situacao")
    private String icSituacao;
    
    @Column(name = "dh_tramite")
    private Date data;
    
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
        return ORDER_BY_BASE;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public Long getNuBase() {
        return nuBase;
    }

    public void setNuBase(Long nuBase) {
        this.nuBase = nuBase;
    }

    public String getNoBase() {
        return noBase;
    }

    public void setNoBase(String noBase) {
        this.noBase = noBase;
    }

    public String getIcSituacao() {
        return icSituacao;
    }

    public void setIcSituacao(String icSituacao) {
        this.icSituacao = icSituacao;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

}
