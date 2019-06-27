package br.gov.caixa.gitecsa.sired.enumerator;

import br.gov.caixa.gitecsa.sired.arquitetura.enumerator.BaseEnumNumberValue;

public enum SituacaoAlteracaoRemessaEnum implements BaseEnumNumberValue {
  
  PADRAO(0L, "Registro Ativo e Valido"),
  ALTERACAO_TERCEIRIZADA(1L, "Alteração feita pela Empresa Terceirizada,  mas ainda nao finalizou as alteracoes na Remessa"),
  ALTERACAO_FINALIZADA_TERCEIRIZADA(2L, "Alteracao feita e finalizada pela Empresa Terceirizada"),
  ALTERACAO_BASE_ARQUIVO(3L, "Alteracao desfeita pela Base de Arquivo"),
  REGISTRO_SUBSTITUIDO(4L, "Registro inserido pela Unidade Caixa, mas substituido por um registro inserido pela Empresa Terceirizada e confirmado"),
  REGISTRO_ADICIONADO(5L, "Registro inserido pela Empresa Terceirizada, considerando o caso de que a Unidade Caixa tenha enviado um documento a mais");  
  
  private Long id;
  
  private String descricao;
  
  private SituacaoAlteracaoRemessaEnum(Long id, String descricao) {
    this.id = id;
    this.descricao = descricao;
  }
  
  @Override
  public Long getValue() {
    return id;
  }

}
