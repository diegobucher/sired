package br.gov.caixa.gitecsa.sired.enumerator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum TipoDocumentalEnum implements BaseEnumNumberValue {

  VALOR_PADRAO(0L, "Valor Padrão"),
  REMESSA_MOVIMENTO_DIARIO_TIPO_C(1L, "Remessa Movimento Diario / Tipo C");

  private Long id;

  private String descricao;

  private TipoDocumentalEnum(Long id, String descricao) {
    this.id = id;
    this.descricao = descricao;
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * @return the descricao
   */
  public String getDescricao() {
    return descricao;
  }

  /**
   * @param descricao the descricao to set
   */
  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }

  @Override
  public Long getValue() {
    // TODO Auto-generated method stub
    return null;
  }
}
