package br.gov.caixa.gitecsa.sired.enumerator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum TipoLotericoEnum implements BaseEnumNumberValue {

  LOTERICO(0L, "LOT", "Lotérico"),
  
  UNIDADE_CAIXA(1L, "TF", "TF");

  private Long id;

  private String descricao;
  
  private String descricaoCompleta;

  private TipoLotericoEnum(Long id, String descricao, String descricaoCompleta) {
    this.id = id;
    this.descricao = descricao;
    this.descricaoCompleta = descricaoCompleta;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }
  
  public String getDescricaoCompleta() {
    return descricaoCompleta;
  }
  
  public void setDescricaoCompleta(String descricaoCompleta) {
    this.descricaoCompleta = descricaoCompleta;
  }

  @Override
  public Long getValue() {
    return id;
  }
}
