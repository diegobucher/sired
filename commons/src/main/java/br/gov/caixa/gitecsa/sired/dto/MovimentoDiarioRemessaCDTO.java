package br.gov.caixa.gitecsa.sired.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import br.gov.caixa.gitecsa.sired.enumerator.SituacaoAlteracaoRemessaEnum;
import br.gov.caixa.gitecsa.sired.vo.RemessaMovimentoDiarioVO;
import br.gov.caixa.gitecsa.sired.vo.RemessaVO;

public class MovimentoDiarioRemessaCDTO implements Serializable, Comparable<MovimentoDiarioRemessaCDTO>  {
  
  /** Serial. */
  private static final long serialVersionUID = -5965081223946352255L;
  
  private RemessaVO remessa;
  
  private String chave;
  
  private Date dataMovimento;
  
  private List<RemessaMovimentoDiarioVO>  remessaMovDiarioList;

  /**
   * @return the remessa
   */
  public RemessaVO getRemessa() {
    return remessa;
  }

  /**
   * @param remessa the remessa to set
   */
  public void setRemessa(RemessaVO remessa) {
    this.remessa = remessa;
  }

  /**
   * @return the dataMovimento
   */
  public Date getDataMovimento() {
    return dataMovimento;
  }

  /**
   * @param dataMovimento the dataMovimento to set
   */
  public void setDataMovimento(Date dataMovimento) {
    this.dataMovimento = dataMovimento;
  }

  /**
   * @return the remessaMovDiarioList
   */
  public List<RemessaMovimentoDiarioVO> getRemessaMovDiarioList() {
    return remessaMovDiarioList;
  }

  /**
   * @param remessaMovDiarioList the remessaMovDiarioList to set
   */
  public void setRemessaMovDiarioList(List<RemessaMovimentoDiarioVO> remessaMovDiarioList) {
    this.remessaMovDiarioList = remessaMovDiarioList;
  }
  
  
  /**
   * @return the chave
   */
  public String getChave() {
    return chave;
  }

  /**
   * @param chave the chave to set
   */
  public void setChave(String chave) {
    this.chave = chave;
  }

  /**
   * @return the unidade
   */
  public Long getUnidade() {
    return Long.valueOf(StringUtils.substringBefore(this.chave, "#"));
  }
  
  
  public String getDataFormatada() {
    this.dataMovimento = getDataMovimento();
    String data = "";
    if (this.dataMovimento != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
      data = sdf.format(this.dataMovimento);
    }    
    return data ;
  }

  public String getNomeUnidadeFormatada() {
    if (!remessaMovDiarioList.isEmpty()) {      
      RemessaMovimentoDiarioVO temp = remessaMovDiarioList.get(0);
      return temp.getUnidadeGeradora().getDescricaoCompleta();
    }
    return StringUtils.EMPTY;
  }

  public String getCodigoRemessaTipoC() {
    if (!remessaMovDiarioList.isEmpty()) {      
      RemessaMovimentoDiarioVO temp = remessaMovDiarioList.get(0);
      return temp.getRemessa().getCodigoRemessaTipoC().toString();
    }
    return StringUtils.EMPTY;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataMovimento == null) ? 0 : dataMovimento.hashCode());
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof MovimentoDiarioRemessaCDTO)) {
      return false;
    }
    MovimentoDiarioRemessaCDTO other = (MovimentoDiarioRemessaCDTO) obj;
    if (dataMovimento == null) {
      if (other.dataMovimento != null) {
        return false;
      }
    } else if (!dataMovimento.equals(other.dataMovimento)) {
      return false;
    }
    return true;
  }

  @Override
  public int compareTo(MovimentoDiarioRemessaCDTO mov) {
    return this.dataMovimento.compareTo(mov.getDataMovimento());
  }
  
  public int tamanhoRemessaTratada() {
    int counter = 0;
    for (RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovDiarioList) {
      if(remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.PADRAO) || 
         remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA) ||
         remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
        counter++;
      }
    }
    return counter;
  }
  
  public boolean remessaMovimentoAlterado() {
    for(RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovDiarioList) {
      if(remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.ALTERACAO_FINALIZADA_TERCEIRIZADA)) {
          return true;
      }
    }
    return false;
  }
  
  public boolean remessaMovimentoAdicionado() {
    for(RemessaMovimentoDiarioVO remessaMovimentoDiarioVO : remessaMovDiarioList) {
      if(remessaMovimentoDiarioVO.getIcAlteracaoValida().equals(SituacaoAlteracaoRemessaEnum.REGISTRO_ADICIONADO)) {
          return true;
      }
    }
    return false;
  }
}
