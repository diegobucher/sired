package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.TipoCampoEnum;
import br.gov.caixa.gitecsa.sired.enumerator.TipoSolicitacaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba01_campo", schema = Constantes.SCHEMADB_NAME)
public class CampoVO extends BaseEntity {

    private static final long serialVersionUID = -8578430698356771846L;
    
    private static final String ORDER_BY_DESCRICAO = "descricao";

    @Id
    @Column(name = "nu_campo", columnDefinition = "serial")
    private Long id;
    
    @Column(name = "no_campo")
    private String nome;

    @Column(name = "de_campo")
    private String descricao;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_tipo_campo", columnDefinition = "int2")
    private TipoCampoEnum tipo;
    
    @Column(name = "nu_tamanho_campo", columnDefinition = "int2")
    private Integer tamanho;

    @Column(name = "de_mascara_campo")
    private String mascara;
    
    @Column(name = "no_funcao_validacao", insertable = false, updatable = false)
    private String funcaoValidacao;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_tipo_solicitacao", columnDefinition = "int2")
    private TipoSolicitacaoEnum tipoSolicitacao;

    @Transient
    private Integer ordem;

    @Transient
    private boolean checkBox;

    @Transient
    private boolean obrigatorio;

    @Transient
    private String mensagem;

    @Transient
    private String legenda;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (Long) id;
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

    public String getMascara() {
        return mascara;
    }

    public void setMascara(String marcara) {
        this.mascara = marcara;
    }

    public TipoCampoEnum getTipo() {
        return tipo;
    }

    public void setTipo(TipoCampoEnum tipo) {
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFuncaoValidacao() {
        return funcaoValidacao;
    }

    public void setFuncaoValidacao(String funcaoValidacao) {
        this.funcaoValidacao = funcaoValidacao;
    }

    public Integer getTamanho() {
        return tamanho;
    }

    public void setTamanho(Integer tamanho) {
        this.tamanho = tamanho;
    }

    public TipoSolicitacaoEnum getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public void setTipoSolicitacao(TipoSolicitacaoEnum tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public boolean isCheckBox() {
        return checkBox;
    }

    public void setCheckBox(boolean checkBox) {
        this.checkBox = checkBox;
    }

    public boolean isObrigatorio() {
        return obrigatorio;
    }

    public void setObrigatorio(boolean obrigatorio) {
        this.obrigatorio = obrigatorio;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getLegenda() {
        return legenda;
    }

    public void setLegenda(String legenda) {
        this.legenda = legenda;
    }

}
