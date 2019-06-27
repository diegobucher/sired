package br.gov.caixa.gitecsa.sired.vo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import br.gov.caixa.gitecsa.sired.arquitetura.vo.BaseEntity;
import br.gov.caixa.gitecsa.sired.enumerator.TipoAuditoriaEnum;
import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Table(name = "redtba16_tipo_auditoria", schema = Constantes.SCHEMADB_NAME)
public class TipoAuditoriaVO extends BaseEntity {

  private static final long serialVersionUID = 3349320727352484696L;

  private static final String ORDER_BY_NOME = "nome";

  @Id
  @Column(name = "nu_tipo_auditoria", columnDefinition = "serial")
  private Integer id;

  @Column(name = "no_tipo_auditoria", length = 30)
  private String nome;

  @Column(name = "de_tipo_auditoria", length = 100)
  private String descricao;

  @Column(name = "de_texto_padrao", length = 100)
  private String texto;

  public TipoAuditoriaVO() {
    super();
  }

  public TipoAuditoriaVO(final TipoAuditoriaEnum enumerador) {
    this.id = enumerador.getId();
  }

  @Override
  public Object getId() {
    return id;
  }

  @Override
  public void setId(Object id) {
    this.id = (Integer) id;
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

  public String getTexto() {
    return texto;
  }

  public void setTexto(String texto) {
    this.texto = texto;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public String getColumnOrderBy() {
    return ORDER_BY_NOME;
  }

  @Override
  public String getAuditoria() {
    return null;
  }

  @Override
  public String toString() {
    return String.format("%s[id=%s]", getClass().getSimpleName(), getId().toString());
  }

}
