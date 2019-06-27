package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba06_ocrna_atnto", schema = Constantes.SCHEMADB_NAME)
public class OcorrenciaAtendimentoVO extends BaseEntity {

    private static final long serialVersionUID = -684236704060834590L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nu_ocrna_atnto", columnDefinition = "int2")
    private Long id;

    @Column(name = "no_ocrna_atnto")
    private String nome;

    @Column(name = "de_ocrna_atnto")
    private String descricao;

    public OcorrenciaAtendimentoVO() { }
    
    public OcorrenciaAtendimentoVO(Long id) { 
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
    
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}
