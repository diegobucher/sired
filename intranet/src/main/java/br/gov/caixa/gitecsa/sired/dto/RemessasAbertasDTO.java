package br.gov.caixa.gitecsa.sired.dto;

import java.util.ArrayList;
import java.util.List;

import br.gov.caixa.gitecsa.sired.vo.RemessaVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class RemessasAbertasDTO {
  
  private UnidadeVO unidade;
  
  private List<RemessaVO> remessasList;
  
  public RemessasAbertasDTO() {
    remessasList = new ArrayList<>();
  }

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
   * @return the remessasList
   */
  public List<RemessaVO> getRemessasList() {
    return remessasList;
  }

  /**
   * @param remessasList the remessasList to set
   */
  public void setRemessasList(List<RemessaVO> remessasList) {
    this.remessasList = remessasList;
  }

}
