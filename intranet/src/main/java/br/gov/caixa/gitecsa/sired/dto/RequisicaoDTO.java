package br.gov.caixa.gitecsa.sired.dto;

import java.util.ArrayList;
import java.util.List;

import br.gov.caixa.gitecsa.sired.vo.RequisicaoVO;
import br.gov.caixa.gitecsa.sired.vo.UnidadeVO;

public class RequisicaoDTO {

  private UnidadeVO unidade;
  
  private List<RequisicaoVO> requisicaoList;
  
  public RequisicaoDTO() {
    requisicaoList = new ArrayList<>();
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
   * @return the requisicao
   */
  public List<RequisicaoVO> getRequisicaoList() {
    return requisicaoList;
  }

  /**
   * @param requisicao the requisicao to set
   */
  public void setRequisicao(List<RequisicaoVO> requisicaoList) {
    this.requisicaoList = requisicaoList;
  }
}
