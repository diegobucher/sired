package br.gov.caixa.gitecsa.sired.siico.vo;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import br.gov.caixa.gitecsa.sired.util.Constantes;

@Entity
@Immutable
@Table(name = "icovw058_sired_funcoes_gerenciais", schema = Constantes.SCHEMA_SIICODB_NAME)
public class ViewFuncoesGerenciaisVO implements Serializable{

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "nu_funcao", columnDefinition = "int2")
  private Long nuFuncao;

  @Column(name = "no_funcao")
  private String noFuncao;

  /**
   * @return the nuFuncao
   */
  public Long getNuFuncao() {
    return nuFuncao;
  }

  /**
   * @param nuFuncao the nuFuncao to set
   */
  public void setNuFuncao(Long nuFuncao) {
    this.nuFuncao = nuFuncao;
  }

  /**
   * @return the noFuncao
   */
  public String getNoFuncao() {
    return noFuncao;
  }

  /**
   * @param noFuncao the noFuncao to set
   */
  public void setNoFuncao(String noFuncao) {
    this.noFuncao = noFuncao;
  }
  
}
