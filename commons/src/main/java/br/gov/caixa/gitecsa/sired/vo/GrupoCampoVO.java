package br.gov.caixa.gitecsa.sired.vo;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.SimNaoEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtbc05_grupo_campo", schema = Constantes.SCHEMADB_NAME)
public class GrupoCampoVO extends BaseEntity implements Comparable<GrupoCampoVO> {

    private static final long serialVersionUID = -8805323625049051643L;
    
    @EmbeddedId
    private GrupoCampoPK id;

    @Column(name = "de_legenda_campo")
    private String legenda;

    @Column(name = "de_mensagem_campo")
    private String mensagem;
    
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "ic_campo_obrigatorio", columnDefinition = "int2")
    private SimNaoEnum campoObrigatorio;

    @Column(name = "nu_ordem_campo", columnDefinition = "int2")
    private Integer ordem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_campo_a01", insertable = false, updatable = false)
    private CampoVO campo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nu_grupo_c02", insertable = false, updatable = false)
    private GrupoVO grupo;

    @Transient
    private Boolean obrigatorio;

    @Transient
    private String valor;

    @Transient
    private Date valorData;
    
    @Transient
    private boolean valoresDiferentes;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public void setId(Object id) {
        this.id = (GrupoCampoPK) id;
    }

    @Override
    public String getColumnOrderBy() {
        return null;
    }

    @Override
    public String getAuditoria() {
        return null;
    }

    public String getLegenda() {
        return legenda;
    }

    public void setLegenda(String legenda) {
        this.legenda = legenda;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public SimNaoEnum getCampoObrigatorio() {
        return campoObrigatorio;
    }

    public void setCampoObrigatorio(SimNaoEnum campoObrigatorio) {
        this.campoObrigatorio = campoObrigatorio;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public CampoVO getCampo() {
        return campo;
    }

    public void setCampo(CampoVO campo) {
        this.campo = campo;
    }

    public GrupoVO getGrupo() {
        return grupo;
    }

    public void setGrupo(GrupoVO grupo) {
        this.grupo = grupo;
    }

    public Boolean getObrigatorio() {
        return obrigatorio;
    }

    public void setObrigatorio(Boolean obrigatorio) {
        this.obrigatorio = obrigatorio;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Date getValorData() {
        return valorData;
    }

    public void setValorData(Date valorData) {
        this.valorData = valorData;
    }

    @Override
    public int compareTo(GrupoCampoVO o) {
        
        if (this.ordem != null && o.ordem != null)
            return this.ordem.compareTo(o.ordem);
        
        return BigDecimal.ZERO.intValue();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        GrupoCampoVO other = (GrupoCampoVO) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    /**
     * @return the valoresDiferentes
     */
    public boolean isValoresDiferentes() {
      return valoresDiferentes;
    }

    /**
     * @param valoresDiferentes the valoresDiferentes to set
     */
    public void setValoresDiferentes(boolean valoresDiferentes) {
      this.valoresDiferentes = valoresDiferentes;
    }

    public String getValorDataFormatada() {
      String valorDataFormatada = "";
      if(valorData != null) {
        valorDataFormatada = new SimpleDateFormat("dd/MM/yyyy").format(valorData);
        return valorDataFormatada;
      } 
      return valorDataFormatada;
    }

}
