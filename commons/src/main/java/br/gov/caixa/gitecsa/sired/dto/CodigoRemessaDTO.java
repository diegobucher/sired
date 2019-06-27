package br.gov.caixa.gitecsa.sired.dto;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class CodigoRemessaDTO {

  private static final int NUM_CARACTERES_SEQUENCIAL = 5;

  private UnidadeVO unidade;

  private Integer nuAno;

  private Integer nuRemessa;
  
  /**
   * @return the unidade
   */
  public UnidadeVO getUnidade() {
    return unidade;
  }

  /**
   * @param unidade the unidade to set
   */
  public void setUnidade(UnidadeVO unidade) {
    this.unidade = unidade;
  }

  /**
   * @return the nuAno
   */
  public Integer getNuAno() {
    return nuAno;
  }

  /**
   * @param nuAno the nuAno to set
   */
  public void setNuAno(Integer nuAno) {
    this.nuAno = nuAno;
  }

  /**
   * @return the nuRemessa
   */
  public Integer getNuRemessa() {
    return nuRemessa;
  }

  /**
   * @param nuRemessa the nuRemessa to set
   */
  public void setNuRemessa(Integer nuRemessa) {
    this.nuRemessa = nuRemessa;
  }
  
  @Override
  public String toString() {

      return String.format("%s%s%s", this.getUnidade().getId(), this.getNuAno(),
              StringUtils.leftPad(this.getNuRemessa().toString(), NUM_CARACTERES_SEQUENCIAL, "0"));
  }
}
